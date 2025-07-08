package publicdata.hackathon.diplomats.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import publicdata.hackathon.diplomats.domain.dto.request.DiscussBoardUpdateRequest;
import publicdata.hackathon.diplomats.domain.dto.response.DiscussBoardCommentResponse;
import publicdata.hackathon.diplomats.domain.dto.response.DiscussBoardDetailResponse;
import publicdata.hackathon.diplomats.domain.dto.response.DiscussBoardImageResponse;
import publicdata.hackathon.diplomats.domain.dto.response.DiscussBoardResponse;
import publicdata.hackathon.diplomats.domain.entity.DiscussBoard;
import publicdata.hackathon.diplomats.domain.entity.DiscussBoardImage;
import publicdata.hackathon.diplomats.domain.entity.User;
import publicdata.hackathon.diplomats.domain.enums.DiscussType;
import publicdata.hackathon.diplomats.repository.DiscussBoardCommentRepository;
import publicdata.hackathon.diplomats.repository.DiscussBoardImageRepository;
import publicdata.hackathon.diplomats.repository.DiscussBoardRepository;
import publicdata.hackathon.diplomats.repository.UserRepository;
import publicdata.hackathon.diplomats.utils.FileStorageUtil;
import publicdata.hackathon.diplomats.utils.ImageUtil;

@Service
@RequiredArgsConstructor
public class DiscussBoardService {

	private final DiscussBoardRepository discussBoardRepository;
	private final DiscussBoardCommentRepository discussBoardCommentRepository;
	private final DiscussBoardImageRepository discussBoardImageRepository;
	private final UserRepository userRepository;
	private final FileStorageUtil fileStorageUtil;
	private final ImageUtil imageUtil;

	public void createDiscussBoard(String username, String title, String content, DiscussType discussType,
		List<MultipartFile> images) {
		User user = userRepository.findByUserId(username)
			.orElseThrow(() -> new EntityNotFoundException("User not found"));
		DiscussBoard discussBoard = DiscussBoard.builder()
			.discussType(discussType)
			.content(content)
			.title(title)
			.user(user)
			.build();

		discussBoardRepository.save(discussBoard);

		if (images != null && !images.isEmpty()) {
			for (int i = 0; i < images.size(); i++) {
				MultipartFile image = images.get(i);
				if (!image.isEmpty()) {
					String savedFileName = fileStorageUtil.saveDiscussBoardFile(image);

					DiscussBoardImage discussBoardImage = DiscussBoardImage.builder()
						.discussBoard(discussBoard)
						.imagePath("/uploads/discussboard/" + savedFileName)
						.originalFileName(image.getOriginalFilename())
						.savedFileName(savedFileName)
						.fileSize(image.getSize())
						.contentType(image.getContentType())
						.imageOrder(i + 1) // 1부터 시작하는 순서
						.uploadedAt(LocalDateTime.now())
						.build();

					discussBoardImageRepository.save(discussBoardImage);
				}
			}
		}
	}

	public List<DiscussBoardResponse> getDiscussBoards(String username, Pageable pageable, String sortBy) {
		Page<DiscussBoard> discussBoardPage;
		
		// 정렬 기준에 따라 다른 메서드 호출
		switch (sortBy.toLowerCase()) {
			case "views":
			case "viewcount":
				discussBoardPage = discussBoardRepository.findAllByOrderByViewCountDesc(pageable);
				break;
			case "likes":
				discussBoardPage = discussBoardRepository.findAllByOrderByLikesDesc(pageable);
				break;
			case "latest":
			case "created":
			default:
				discussBoardPage = discussBoardRepository.findAllByOrderByCreatedAtDesc(pageable);
				break;
		}
		
		return discussBoardPage.stream()
			.map(discussBoard -> DiscussBoardResponse.builder()
				.id(discussBoard.getId())
				.title(discussBoard.getTitle())
				.content(discussBoard.getContent())
				.discussType(discussBoard.getDiscussType())
				.likes(discussBoard.getLikes())
				.viewCount(discussBoard.getViewCount())
				.createdAt(discussBoard.getCreatedAt())
				.updatedAt(discussBoard.getUpdatedAt())
				.userId(discussBoard.getUser().getUserId())
				.build())
			.toList();
	}

	public DiscussBoardDetailResponse getDiscussBoardDetails(String username, Long id) {
		DiscussBoard discussBoard = discussBoardRepository.findById(id)
			.orElseThrow(() -> new EntityNotFoundException("DiscussBoard not found"));
		
		List<DiscussBoardCommentResponse> discussBoardComments = discussBoardCommentRepository.findAllByDiscussBoard(discussBoard)
			.stream()
			.map(discussBoardComment -> DiscussBoardCommentResponse.builder()
				.id(discussBoardComment.getId())
				.content(discussBoardComment.getContent())
				.userId(discussBoardComment.getUser().getUserId())
				.commentType(discussBoardComment.getCommentType())
				.createdAt(discussBoardComment.getCreatedAt())
				.updatedAt(discussBoardComment.getUpdatedAt())
				.build())
			.toList();

		List<DiscussBoardImageResponse> images = discussBoardImageRepository.findAllByDiscussBoard(discussBoard)
			.stream()
			.sorted((img1, img2) -> img1.getImageOrder().compareTo(img2.getImageOrder()))
			.map(image -> {
				String fullPath = "uploads/discussboard/" + image.getSavedFileName();
				String base64Data = imageUtil.encodeImageToBase64(fullPath);
				String mimeType = imageUtil.getImageMimeType(image.getOriginalFileName());

				return DiscussBoardImageResponse.builder()
					.id(image.getId())
					.originalFileName(image.getOriginalFileName())
					.base64Data(base64Data)
					.mimeType(mimeType)
					.imageOrder(image.getImageOrder())
					.build();
			})
			.toList();

		return DiscussBoardDetailResponse.builder()
			.title(discussBoard.getTitle())
			.content(discussBoard.getContent())
			.discussType(discussBoard.getDiscussType())
			.likes(discussBoard.getLikes())
			.viewCount(discussBoard.getViewCount())
			.userId(discussBoard.getUser().getUserId())
			.createdAt(discussBoard.getCreatedAt())
			.updatedAt(discussBoard.getUpdatedAt())
			.discussBoardComments(discussBoardComments)
			.discussBoardImages(images)
			.build();
	}

	public void updateDiscussBoard(String username, Long id, DiscussBoardUpdateRequest request) {
		DiscussBoard discussBoard = discussBoardRepository.findById(id)
			.orElseThrow(() -> new EntityNotFoundException("게시글을 찾을 수 없습니다."));

		// 작성자 확인
		if (!discussBoard.getUser().getUserId().equals(username)) {
			throw new RuntimeException("게시글 수정 권한이 없습니다.");
		}

		// 게시글 수정
		discussBoard.setTitle(request.getTitle());
		discussBoard.setContent(request.getContent());
		discussBoard.setDiscussType(request.getDiscussType());
		discussBoard.setUpdatedAt(LocalDateTime.now());

		discussBoardRepository.save(discussBoard);
	}

	public void deleteDiscussBoard(String username, Long id) {
		DiscussBoard discussBoard = discussBoardRepository.findById(id)
			.orElseThrow(() -> new EntityNotFoundException("게시글을 찾을 수 없습니다."));

		// 작성자 확인
		if (!discussBoard.getUser().getUserId().equals(username)) {
			throw new RuntimeException("게시글 삭제 권한이 없습니다.");
		}

		// 연관된 이미지들도 함께 삭제 (Cascade로 처리되지만 파일도 삭제)
		List<DiscussBoardImage> images = discussBoardImageRepository.findAllByDiscussBoard(discussBoard);
		for (DiscussBoardImage image : images) {
			// 실제 파일 삭제 (optional)
			try {
				fileStorageUtil.deleteDiscussBoardFile(image.getSavedFileName());
			} catch (Exception e) {
				// 파일 삭제 실패해도 DB는 삭제 진행
			}
		}

		discussBoardRepository.delete(discussBoard);
	}
}
