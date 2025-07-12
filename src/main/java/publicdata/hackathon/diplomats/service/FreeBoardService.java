package publicdata.hackathon.diplomats.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import publicdata.hackathon.diplomats.domain.dto.request.FreeBoardUpdateRequest;
import publicdata.hackathon.diplomats.domain.dto.response.FreeBoardCommentResponse;
import publicdata.hackathon.diplomats.domain.dto.response.FreeBoardDetailResponse;
import publicdata.hackathon.diplomats.domain.dto.response.FreeBoardImageResponse;
import publicdata.hackathon.diplomats.domain.dto.response.FreeBoardResponse;
import publicdata.hackathon.diplomats.domain.dto.response.PagedResponse;
import publicdata.hackathon.diplomats.domain.entity.FreeBoard;
import publicdata.hackathon.diplomats.domain.entity.FreeBoardImage;
import publicdata.hackathon.diplomats.domain.entity.User;
import publicdata.hackathon.diplomats.repository.FreeBoardCommentRepository;
import publicdata.hackathon.diplomats.repository.FreeBoardImageRepository;
import publicdata.hackathon.diplomats.repository.FreeBoardRepository;
import publicdata.hackathon.diplomats.repository.UserRepository;
import publicdata.hackathon.diplomats.repository.LikeRepository;
import publicdata.hackathon.diplomats.utils.FileStorageUtil;
import publicdata.hackathon.diplomats.utils.ImageUtil;
import publicdata.hackathon.diplomats.utils.ResponseUtil;

@Service
@RequiredArgsConstructor
@Slf4j
public class FreeBoardService {

	private final FreeBoardRepository freeBoardRepository;
	private final FreeBoardCommentRepository freeBoardCommentRepository;
	private final FreeBoardImageRepository freeBoardImageRepository;
	private final UserRepository userRepository;
	private final LikeRepository likeRepository;
	private final FileStorageUtil fileStorageUtil;
	private final ImageUtil imageUtil;

	public Long createFreeBoard(String username, String title, String content, List<MultipartFile> images) {
		User user = userRepository.findByUserId(username)
			.orElseThrow(() -> new EntityNotFoundException("User not found"));

		FreeBoard freeBoard = FreeBoard.builder()
			.title(title)
			.content(content)
			.user(user)
			.build();
		freeBoardRepository.save(freeBoard);

		if (images != null && !images.isEmpty()) {
			for (int i = 0; i < images.size(); i++) {
				MultipartFile image = images.get(i);
				if (!image.isEmpty()) {
					String savedFileName = fileStorageUtil.saveFreeBoardFile(image);

					FreeBoardImage freeBoardImage = FreeBoardImage.builder()
						.freeBoard(freeBoard)
						.imagePath("/uploads/freeboard/" + savedFileName)
						.originalFileName(image.getOriginalFilename())
						.savedFileName(savedFileName)
						.fileSize(image.getSize())
						.contentType(image.getContentType())
						.imageOrder(i + 1) // 1부터 시작하는 순서
						.uploadedAt(LocalDateTime.now())
						.build();

					freeBoardImageRepository.save(freeBoardImage);
				}
			}
		}
		
		return freeBoard.getId();
	}

	public PagedResponse<FreeBoardResponse> getFreeBoards(String username, Pageable pageable, String sortBy) {
		Page<FreeBoard> freeBoardPage;
		
		// 정렬 기준에 따라 다른 메서드 호출
		switch (sortBy.toLowerCase()) {
			case "views":
			case "viewcount":
				freeBoardPage = freeBoardRepository.findAllByOrderByViewCountDesc(pageable);
				break;
			case "likes":
				freeBoardPage = freeBoardRepository.findAllByOrderByLikesDesc(pageable);
				break;
			case "latest":
			case "created":
			default:
				freeBoardPage = freeBoardRepository.findAllByOrderByCreatedAtDesc(pageable);
				break;
		}
		
		List<FreeBoardResponse> content = freeBoardPage.stream()
			.map(freeBoard -> FreeBoardResponse.builder()
				.id(freeBoard.getId())
				.title(freeBoard.getTitle())
				.likes(freeBoard.getLikes())
				.liked(ResponseUtil.isLiked(username, "FreeBoard", freeBoard.getId(), likeRepository, userRepository))
				.content(freeBoard.getContent())
				.createdAt(freeBoard.getCreatedAt())
				.updatedAt(freeBoard.getUpdatedAt())
				.userId(freeBoard.getUser().getUserId())
				.isOwner(username != null && username.equals(freeBoard.getUser().getUserId()))
				.build())
			.toList();
			
		return PagedResponse.of(content, freeBoardPage);
	}

	public FreeBoardDetailResponse getFreeBoardDetails(String username, Long id) {
		FreeBoard freeBoard = freeBoardRepository.findById(id)
			.orElseThrow(() -> new EntityNotFoundException("FreeBoard not found"));
		List<FreeBoardCommentResponse> freeBoardComments = freeBoardCommentRepository.findAllByFreeBoard(freeBoard)
			.stream()
			.map(freeBoardComment -> FreeBoardCommentResponse.builder()
				.id(freeBoardComment.getId())
				.content(freeBoardComment.getContent())
				.userId(freeBoardComment.getUser().getUserId())
				.isOwner(username != null && username.equals(freeBoardComment.getUser().getUserId()))
				.updatedAt(freeBoardComment.getUpdatedAt())
				.createdAt(freeBoardComment.getCreatedAt())
				.build())
			.toList();

		List<FreeBoardImageResponse> images = freeBoardImageRepository.findAllByFreeBoard(freeBoard)
			.stream()
			.sorted((img1, img2) -> img1.getImageOrder().compareTo(img2.getImageOrder()))
			.map(image -> {
				String imageUrl = imageUtil.generateImageUrl(image.getSavedFileName(), "freeboard");
				String mimeType = imageUtil.getImageMimeType(image.getOriginalFileName());

				// 이미지 파일 존재 여부 확인
				String fullPath = "uploads/freeboard/" + image.getSavedFileName();
				if (!imageUtil.imageExists(fullPath)) {
					log.warn("이미지 파일이 존재하지 않음, 기본 URL 사용: imageId={}, path={}", image.getId(), fullPath);
					imageUrl = imageUtil.getDefaultImageUrl();
					mimeType = "image/png";
				}

				return FreeBoardImageResponse.builder()
					.id(image.getId())
					.originalFileName(image.getOriginalFileName())
					.imageUrl(imageUrl)
					.mimeType(mimeType)
					.imageOrder(image.getImageOrder())
					.fileSize(image.getFileSize())
					.build();
			})
			.toList();

		return FreeBoardDetailResponse.builder()
			.freeBoardComments(freeBoardComments)
			.freeBoardImages(images)
			.title(freeBoard.getTitle())
			.content(freeBoard.getContent())
			.likes(freeBoard.getLikes())
			.liked(ResponseUtil.isLiked(username, "FreeBoard", freeBoard.getId(), likeRepository, userRepository))
			.viewCount(freeBoard.getViewCount())
			.userId(freeBoard.getUser().getUserId())
			.isOwner(username != null && username.equals(freeBoard.getUser().getUserId()))
			.createdAt(freeBoard.getCreatedAt())
			.updatedAt(freeBoard.getUpdatedAt())
			.build();
	}

	public void updateFreeBoard(String username, Long id, FreeBoardUpdateRequest request) {
		FreeBoard freeBoard = freeBoardRepository.findById(id)
			.orElseThrow(() -> new EntityNotFoundException("게시글을 찾을 수 없습니다."));

		// 작성자 확인
		if (!freeBoard.getUser().getUserId().equals(username)) {
			throw new RuntimeException("게시글 수정 권한이 없습니다.");
		}

		// 게시글 수정
		freeBoard.setTitle(request.getTitle());
		freeBoard.setContent(request.getContent());
		freeBoard.setUpdatedAt(LocalDateTime.now());

		freeBoardRepository.save(freeBoard);
	}

	public void deleteFreeBoard(String username, Long id) {
		FreeBoard freeBoard = freeBoardRepository.findById(id)
			.orElseThrow(() -> new EntityNotFoundException("게시글을 찾을 수 없습니다."));

		// 작성자 확인
		if (!freeBoard.getUser().getUserId().equals(username)) {
			throw new RuntimeException("게시글 삭제 권한이 없습니다.");
		}

		// 연관된 이미지들도 함께 삭제 (Cascade로 처리되지만 파일도 삭제)
		List<FreeBoardImage> images = freeBoardImageRepository.findAllByFreeBoard(freeBoard);
		for (FreeBoardImage image : images) {
			// 실제 파일 삭제 (optional)
			try {
				fileStorageUtil.deleteFreeBoardFile(image.getSavedFileName());
			} catch (Exception e) {
				// 파일 삭제 실패해도 DB는 삭제 진행
			}
		}

		freeBoardRepository.delete(freeBoard);
	}
}
