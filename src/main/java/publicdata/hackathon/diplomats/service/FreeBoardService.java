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
		
		// 🔧 조회수 증가 (본인이 아닌 경우에만)
		if (username == null || !freeBoard.getUser().getUserId().equals(username)) {
			freeBoard.incrementViewCount();
			freeBoardRepository.save(freeBoard);
			log.debug("자유게시글 조회수 증가: boardId={}, newViewCount={}", id, freeBoard.getViewCount());
		}
		
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
			.viewCount(freeBoard.getViewCount()) // 🔧 업데이트된 조회수 반영
			.userId(freeBoard.getUser().getUserId())
			.isOwner(username != null && username.equals(freeBoard.getUser().getUserId()))
			.createdAt(freeBoard.getCreatedAt())
			.updatedAt(freeBoard.getUpdatedAt())
			.build();
	}

	public void updateFreeBoard(String username, Long id, String title, String content, List<MultipartFile> images) {
		FreeBoard freeBoard = freeBoardRepository.findById(id)
			.orElseThrow(() -> new EntityNotFoundException("게시글을 찾을 수 없습니다."));

		// 작성자 확인
		if (!freeBoard.getUser().getUserId().equals(username)) {
			throw new RuntimeException("게시글 수정 권한이 없습니다.");
		}

		// 기본 정보 수정
		freeBoard.setTitle(title);
		freeBoard.setContent(content);
		freeBoard.setUpdatedAt(LocalDateTime.now());

		// 기존 이미지들 삭제
		List<FreeBoardImage> existingImages = freeBoardImageRepository.findAllByFreeBoard(freeBoard);
		for (FreeBoardImage image : existingImages) {
			try {
				fileStorageUtil.deleteFreeBoardFile(image.getSavedFileName());
			} catch (Exception e) {
				log.warn("기존 이미지 파일 삭제 실패: {}", image.getSavedFileName(), e);
			}
		}
		freeBoardImageRepository.deleteAll(existingImages);

		// 새 이미지들 추가
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
						.imageOrder(i + 1)
						.uploadedAt(LocalDateTime.now())
						.build();

					freeBoardImageRepository.save(freeBoardImage);
				}
			}
		}

		freeBoardRepository.save(freeBoard);
		log.info("자유게시글 수정 완료: freeBoardId={}, userId={}", id, username);
	}

	public void deleteFreeBoard(String username, Long id) {
		FreeBoard freeBoard = freeBoardRepository.findById(id)
			.orElseThrow(() -> new EntityNotFoundException("게시글을 찾을 수 없습니다."));

		// 작성자 확인
		if (!freeBoard.getUser().getUserId().equals(username)) {
			throw new RuntimeException("게시글 삭제 권한이 없습니다.");
		}

		// 연관된 이미지 파일들을 삭제하기 위해 미리 조회
		List<FreeBoardImage> images = freeBoardImageRepository.findAllByFreeBoard(freeBoard);
		
		// 실제 파일들을 먼저 삭제
		for (FreeBoardImage image : images) {
			try {
				fileStorageUtil.deleteFreeBoardFile(image.getSavedFileName());
				log.info("이미지 파일 삭제 완료: {}", image.getSavedFileName());
			} catch (Exception e) {
				log.warn("이미지 파일 삭제 실패: {}, 에러: {}", image.getSavedFileName(), e.getMessage());
				// 파일 삭제 실패해도 DB는 삭제 진행
			}
		}

		// 관련된 좋아요들 삭제
		try {
			likeRepository.deleteByTargetTypeAndTargetId("FreeBoard", id);
			log.info("자유게시글 관련 좋아요 삭제 완료: freeBoardId={}", id);
		} catch (Exception e) {
			log.warn("자유게시글 관련 좋아요 삭제 실패: freeBoardId={}, 에러: {}", id, e.getMessage());
		}

		// FreeBoard 삭제 (CASCADE로 연관된 이미지와 댓글들이 자동 삭제됨)
		freeBoardRepository.delete(freeBoard);
		log.info("자유게시글 삭제 완료: freeBoardId={}, userId={}", id, username);
	}
}
