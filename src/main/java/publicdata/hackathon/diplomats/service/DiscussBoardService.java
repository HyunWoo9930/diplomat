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
import publicdata.hackathon.diplomats.domain.dto.request.DiscussBoardUpdateRequest;
import publicdata.hackathon.diplomats.domain.dto.response.DiscussBoardCommentResponse;
import publicdata.hackathon.diplomats.domain.dto.response.DiscussBoardDetailResponse;
import publicdata.hackathon.diplomats.domain.dto.response.DiscussBoardImageResponse;
import publicdata.hackathon.diplomats.domain.dto.response.DiscussBoardResponse;
import publicdata.hackathon.diplomats.domain.dto.response.PagedResponse;
import publicdata.hackathon.diplomats.domain.entity.DiscussBoard;
import publicdata.hackathon.diplomats.domain.entity.DiscussBoardImage;
import publicdata.hackathon.diplomats.domain.entity.User;
import publicdata.hackathon.diplomats.domain.enums.DiscussType;
import publicdata.hackathon.diplomats.repository.DiscussBoardCommentRepository;
import publicdata.hackathon.diplomats.repository.DiscussBoardImageRepository;
import publicdata.hackathon.diplomats.repository.DiscussBoardRepository;
import publicdata.hackathon.diplomats.repository.UserRepository;
import publicdata.hackathon.diplomats.repository.LikeRepository;
import publicdata.hackathon.diplomats.utils.FileStorageUtil;
import publicdata.hackathon.diplomats.utils.ImageUtil;
import publicdata.hackathon.diplomats.utils.ResponseUtil;

@Service
@RequiredArgsConstructor
@Slf4j
public class DiscussBoardService {

	private final DiscussBoardRepository discussBoardRepository;
	private final DiscussBoardCommentRepository discussBoardCommentRepository;
	private final DiscussBoardImageRepository discussBoardImageRepository;
	private final UserRepository userRepository;
	private final LikeRepository likeRepository;
	private final FileStorageUtil fileStorageUtil;
	private final ImageUtil imageUtil;

	public Long createDiscussBoard(String username, String title, String content, DiscussType discussType,
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
		
		return discussBoard.getId();
	}

	public PagedResponse<DiscussBoardResponse> getDiscussBoards(String username, Pageable pageable, String sortBy) {
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
		
		List<DiscussBoardResponse> content = discussBoardPage.stream()
			.map(discussBoard -> DiscussBoardResponse.builder()
				.id(discussBoard.getId())
				.title(discussBoard.getTitle())
				.content(discussBoard.getContent())
				.discussType(discussBoard.getDiscussType())
				.discussTypeDisplay(discussBoard.getDiscussType().getDisplayName())
				.likes(discussBoard.getLikes())
				.liked(ResponseUtil.isLiked(username, "DiscussBoard", discussBoard.getId(), likeRepository, userRepository))
				.viewCount(discussBoard.getViewCount())
				.createdAt(discussBoard.getCreatedAt())
				.updatedAt(discussBoard.getUpdatedAt())
				.userId(discussBoard.getUser().getUserId())
				.isOwner(username != null && username.equals(discussBoard.getUser().getUserId()))
				.build())
			.toList();
			
		return PagedResponse.of(content, discussBoardPage);
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
				.isOwner(username != null && username.equals(discussBoardComment.getUser().getUserId()))
				.commentType(discussBoardComment.getCommentType())
				.createdAt(discussBoardComment.getCreatedAt())
				.updatedAt(discussBoardComment.getUpdatedAt())
				.build())
			.toList();

		List<DiscussBoardImageResponse> images = discussBoardImageRepository.findAllByDiscussBoard(discussBoard)
			.stream()
			.sorted((img1, img2) -> img1.getImageOrder().compareTo(img2.getImageOrder()))
			.map(image -> {
				String imageUrl = imageUtil.generateImageUrl(image.getSavedFileName(), "discussboard");
				String mimeType = imageUtil.getImageMimeType(image.getOriginalFileName());

				// 이미지 파일 존재 여부 확인
				String fullPath = "uploads/discussboard/" + image.getSavedFileName();
				if (!imageUtil.imageExists(fullPath)) {
					log.warn("이미지 파일이 존재하지 않음, 기본 URL 사용: imageId={}, path={}", image.getId(), fullPath);
					imageUrl = imageUtil.getDefaultImageUrl();
					mimeType = "image/png";
				}

				return DiscussBoardImageResponse.builder()
					.id(image.getId())
					.originalFileName(image.getOriginalFileName())
					.imageUrl(imageUrl)
					.mimeType(mimeType)
					.imageOrder(image.getImageOrder())
					.fileSize(image.getFileSize())
					.build();
			})
			.toList();

		return DiscussBoardDetailResponse.builder()
			.title(discussBoard.getTitle())
			.content(discussBoard.getContent())
			.discussType(discussBoard.getDiscussType())
			.discussTypeDisplay(discussBoard.getDiscussType().getDisplayName())
			.likes(discussBoard.getLikes())
			.viewCount(discussBoard.getViewCount())
			.userId(discussBoard.getUser().getUserId())
			.owner(username != null && username.equals(discussBoard.getUser().getUserId()))
			.createdAt(discussBoard.getCreatedAt())
			.updatedAt(discussBoard.getUpdatedAt())
			.discussBoardComments(discussBoardComments)
			.discussBoardImages(images)
			.build();
	}

	public void updateDiscussBoard(String username, Long id, String title, String content, DiscussType discussType, List<MultipartFile> images) {
		DiscussBoard discussBoard = discussBoardRepository.findById(id)
			.orElseThrow(() -> new EntityNotFoundException("게시글을 찾을 수 없습니다."));

		// 작성자 확인
		if (!discussBoard.getUser().getUserId().equals(username)) {
			throw new RuntimeException("게시글 수정 권한이 없습니다.");
		}

		// 기본 정보 수정
		discussBoard.setTitle(title);
		discussBoard.setContent(content);
		discussBoard.setDiscussType(discussType);
		discussBoard.setUpdatedAt(LocalDateTime.now());

		// 기존 이미지들 삭제
		List<DiscussBoardImage> existingImages = discussBoardImageRepository.findAllByDiscussBoard(discussBoard);
		for (DiscussBoardImage image : existingImages) {
			try {
				fileStorageUtil.deleteDiscussBoardFile(image.getSavedFileName());
			} catch (Exception e) {
				log.warn("기존 이미지 파일 삭제 실패: {}", image.getSavedFileName(), e);
			}
		}
		discussBoardImageRepository.deleteAll(existingImages);

		// 새 이미지들 추가
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
						.imageOrder(i + 1)
						.uploadedAt(LocalDateTime.now())
						.build();

					discussBoardImageRepository.save(discussBoardImage);
				}
			}
		}

		discussBoardRepository.save(discussBoard);
		log.info("토론게시글 수정 완료: discussBoardId={}, userId={}", id, username);
	}

	public void deleteDiscussBoard(String username, Long id) {
		DiscussBoard discussBoard = discussBoardRepository.findById(id)
			.orElseThrow(() -> new EntityNotFoundException("게시글을 찾을 수 없습니다."));

		// 작성자 확인
		if (!discussBoard.getUser().getUserId().equals(username)) {
			throw new RuntimeException("게시글 삭제 권한이 없습니다.");
		}

		// 연관된 이미지 파일들을 삭제하기 위해 미리 조회
		List<DiscussBoardImage> images = discussBoardImageRepository.findAllByDiscussBoard(discussBoard);
		
		// 실제 파일들을 먼저 삭제
		for (DiscussBoardImage image : images) {
			try {
				fileStorageUtil.deleteDiscussBoardFile(image.getSavedFileName());
				log.info("이미지 파일 삭제 완료: {}", image.getSavedFileName());
			} catch (Exception e) {
				log.warn("이미지 파일 삭제 실패: {}, 에러: {}", image.getSavedFileName(), e.getMessage());
				// 파일 삭제 실패해도 DB는 삭제 진행
			}
		}

		// 관련된 좋아요들 삭제
		try {
			likeRepository.deleteByTargetTypeAndTargetId("DiscussBoard", id);
			log.info("토론게시글 관련 좋아요 삭제 완료: discussBoardId={}", id);
		} catch (Exception e) {
			log.warn("토론게시글 관련 좋아요 삭제 실패: discussBoardId={}, 에러: {}", id, e.getMessage());
		}

		// DiscussBoard 삭제 (CASCADE로 연관된 이미지와 댓글들이 자동 삭제됨)
		discussBoardRepository.delete(discussBoard);
		log.info("토론게시글 삭제 완료: discussBoardId={}, userId={}", id, username);
	}
}
