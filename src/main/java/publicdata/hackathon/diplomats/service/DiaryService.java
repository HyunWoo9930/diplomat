package publicdata.hackathon.diplomats.service;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import publicdata.hackathon.diplomats.domain.dto.response.DiaryCommentResponse;
import publicdata.hackathon.diplomats.domain.dto.response.DiaryDetailResponse;
import publicdata.hackathon.diplomats.domain.dto.response.DiaryImageResponse;
import publicdata.hackathon.diplomats.domain.dto.response.DiaryResponse;
import publicdata.hackathon.diplomats.domain.dto.response.PagedResponse;
import publicdata.hackathon.diplomats.domain.dto.response.StampEarnedResponse;
import publicdata.hackathon.diplomats.domain.entity.Diary;
import publicdata.hackathon.diplomats.domain.entity.DiaryComment;
import publicdata.hackathon.diplomats.domain.entity.DiaryImage;
import publicdata.hackathon.diplomats.domain.entity.Like;
import publicdata.hackathon.diplomats.domain.entity.User;
import publicdata.hackathon.diplomats.domain.entity.UserStamp;
import publicdata.hackathon.diplomats.domain.entity.VoteCandidate;
import publicdata.hackathon.diplomats.exception.CustomException;
import publicdata.hackathon.diplomats.exception.ErrorCode;
import publicdata.hackathon.diplomats.repository.DiaryCommentRepository;
import publicdata.hackathon.diplomats.repository.DiaryImageRepository;
import publicdata.hackathon.diplomats.repository.DiaryRepository;
import publicdata.hackathon.diplomats.repository.UserRepository;
import publicdata.hackathon.diplomats.repository.LikeRepository;
import publicdata.hackathon.diplomats.repository.VoteCandidateRepository;
import publicdata.hackathon.diplomats.repository.UserStampRepository;
import publicdata.hackathon.diplomats.utils.FileStorageUtil;
import publicdata.hackathon.diplomats.utils.ImageUtil;
import publicdata.hackathon.diplomats.utils.SecurityUtils;
import publicdata.hackathon.diplomats.utils.ResponseUtil;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class DiaryService {

	private final DiaryRepository diaryRepository;
	private final DiaryCommentRepository diaryCommentRepository;
	private final DiaryImageRepository diaryImageRepository;
	private final UserRepository userRepository;
	private final LikeRepository likeRepository;
	private final VoteCandidateRepository voteCandidateRepository;
	private final UserStampRepository userStampRepository;
	private final FileStorageUtil fileStorageUtil;
	private final ImageUtil imageUtil;
	private final StampService stampService;

	public Long createDiary(String username, String title, String content, String action,
		List<MultipartFile> images) {
		
		// 입력값 유효성 검사
		validateDiaryInput(title, content, action);
		
		// 현재 인증된 사용자 확인
		User currentUser = SecurityUtils.getCurrentUser();
		if (!currentUser.getUserId().equals(username)) {
			throw new CustomException(ErrorCode.ACCESS_DENIED);
		}

		try {
			Diary diary = Diary.builder()
				.action(action)
				.description(content)
				.title(title)
				.writer(currentUser)
				.build();

			diaryRepository.save(diary);
			log.info("일지 생성 완료: userId={}, diaryId={}", username, diary.getId());

			// 실천일기 작성 스탬프 지급
			try {
				StampEarnedResponse stampResponse = stampService.earnDiaryWriteStamp(currentUser, diary.getId());
				if (stampResponse.isSuccess()) {
					log.info("실천일기 작성 스탬프 지급 완료: userId={}, diaryId={}, leveledUp={}", 
							username, diary.getId(), stampResponse.isLeveledUp());
				}
			} catch (Exception e) {
				log.error("실천일기 작성 스탬프 지급 실패: userId={}, diaryId={}", username, diary.getId(), e);
			}

			// 이미지 처리
			if (images != null && !images.isEmpty()) {
				processImages(diary, images);
			}
			
			return diary.getId();
			
		} catch (CustomException e) {
			throw e;
		} catch (Exception e) {
			log.error("일지 생성 실패: userId={}, error={}", username, e.getMessage(), e);
			throw new CustomException(ErrorCode.DATABASE_ERROR, "일지 생성 중 오류가 발생했습니다.");
		}
	}

	public PagedResponse<DiaryResponse> getDiaries(String username, Pageable pageable, String sortBy) {
		try {
			Page<Diary> diaryPage;

			// 정렬 기준에 따라 다른 메서드 호출
			switch (sortBy.toLowerCase()) {
				case "views":
				case "viewcount":
					diaryPage = diaryRepository.findAllByOrderByViewCountDesc(pageable);
					break;
				case "likes":
					diaryPage = diaryRepository.findAllByOrderByLikesDesc(pageable);
					break;
				case "latest":
				case "created":
				default:
					diaryPage = diaryRepository.findAllByOrderByCreatedAtDesc(pageable);
					break;
			}

			List<DiaryResponse> content = diaryPage.stream()
				.map(diary -> DiaryResponse.builder()
					.id(diary.getId())
					.title(diary.getTitle())
					.description(diary.getDescription())
					.action(diary.getAction())
					.likes(diary.getLikes())
					.liked(ResponseUtil.isLiked(username, "Diary", diary.getId(), likeRepository, userRepository))
					.createdAt(diary.getCreatedAt())
					.updatedAt(diary.getUpdatedAt())
					.userId(diary.getWriter().getUserId())
					.isOwner(username != null && username.equals(diary.getWriter().getUserId()))
					.build())
				.toList();
				
			return PagedResponse.of(content, diaryPage);
				
		} catch (Exception e) {
			log.error("일지 목록 조회 실패: username={}, error={}", username, e.getMessage(), e);
			throw new CustomException(ErrorCode.DATABASE_ERROR, "일지 목록 조회 중 오류가 발생했습니다.");
		}
	}

	public DiaryDetailResponse getDiaryDetails(String username, Long id) {
		if (id == null || id <= 0) {
			throw new CustomException(ErrorCode.INVALID_INPUT, "유효하지 않은 일지 ID입니다.");
		}

		try {
			Diary diary = diaryRepository.findById(id)
				.orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND, "일지를 찾을 수 없습니다."));

			List<DiaryCommentResponse> diaryComments = diaryCommentRepository.findAllByDiary(diary)
				.stream()
				.map(diaryComment -> DiaryCommentResponse.builder()
					.id(diaryComment.getId())
					.content(diaryComment.getContent())
					.userId(diaryComment.getCommenter().getUserId())
					.isOwner(username != null && username.equals(diaryComment.getCommenter().getUserId()))
					.createdAt(diaryComment.getCreatedAt())
					.updatedAt(diaryComment.getUpdatedAt())
					.build())
				.toList();

			List<DiaryImageResponse> images = processDiaryImages(diary);

			return DiaryDetailResponse.builder()
				.title(diary.getTitle())
				.description(diary.getDescription())
				.action(diary.getAction())
				.likes(diary.getLikes())
				.liked(ResponseUtil.isLiked(username, "Diary", diary.getId(), likeRepository, userRepository))
				.userId(diary.getWriter().getUserId())
				.isOwner(username != null && username.equals(diary.getWriter().getUserId()))
				.createdAt(diary.getCreatedAt())
				.updatedAt(diary.getUpdatedAt())
				.diaryComments(diaryComments)
				.diaryImages(images)
				.build();
				
		} catch (CustomException e) {
			throw e;
		} catch (Exception e) {
			log.error("일지 상세 조회 실패: id={}, error={}", id, e.getMessage(), e);
			throw new CustomException(ErrorCode.DATABASE_ERROR, "일지 상세 조회 중 오류가 발생했습니다.");
		}
	}

	/**
	 * 이번 달 인기 일지 상위 10개 조회
	 */
	public List<DiaryResponse> getTopMonthlyDiaries() {
		try {
			YearMonth currentMonth = YearMonth.now();
			LocalDateTime startOfMonth = currentMonth.atDay(1).atStartOfDay();
			LocalDateTime endOfMonth = currentMonth.atEndOfMonth().atTime(23, 59, 59);

			Pageable top10 = PageRequest.of(0, 10);
			List<Diary> topDiaries = diaryRepository.findTopDiariesByMonth(startOfMonth, endOfMonth, top10);

			return topDiaries.stream()
				.map(diary -> DiaryResponse.builder()
					.id(diary.getId())
					.title(diary.getTitle())
					.description(diary.getDescription())
					.action(diary.getAction())
					.likes(diary.getLikes())
					.createdAt(diary.getCreatedAt())
					.updatedAt(diary.getUpdatedAt())
					.userId(diary.getWriter().getUserId())
					.build())
				.toList();
				
		} catch (Exception e) {
			log.error("월간 인기 일지 조회 실패: error={}", e.getMessage(), e);
			throw new CustomException(ErrorCode.DATABASE_ERROR, "월간 인기 일지 조회 중 오류가 발생했습니다.");
		}
	}

	/**
	 * 실천일지 수정
	 */
	public void updateDiary(String username, Long id, String title, String content, String action, List<MultipartFile> images) {
		if (id == null || id <= 0) {
			throw new CustomException(ErrorCode.INVALID_INPUT, "유효하지 않은 일지 ID입니다.");
		}

		try {
			Diary diary = diaryRepository.findById(id)
				.orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND, "일지를 찾을 수 없습니다."));

			// 작성자 확인
			if (!diary.getWriter().getUserId().equals(username)) {
				throw new CustomException(ErrorCode.ACCESS_DENIED, "일지 수정 권한이 없습니다.");
			}

			// 입력값 유효성 검사
			validateDiaryInput(title, content, action);

			// 기본 정보 수정
			diary.setTitle(title);
			diary.setDescription(content);
			diary.setAction(action);
			diary.setUpdatedAt(LocalDateTime.now());

			// 기존 이미지들 삭제
			List<DiaryImage> existingImages = diaryImageRepository.findAllByDiary(diary);
			for (DiaryImage image : existingImages) {
				try {
					fileStorageUtil.deleteDiaryFile(image.getSavedFileName());
				} catch (Exception e) {
					log.warn("기존 이미지 파일 삭제 실패: {}", image.getSavedFileName(), e);
				}
			}
			diaryImageRepository.deleteAll(existingImages);

			// 새 이미지들 추가
			if (images != null && !images.isEmpty()) {
				processImages(diary, images);
			}

			diaryRepository.save(diary);
			log.info("일지 수정 완료: userId={}, diaryId={}", username, id);
			
		} catch (CustomException e) {
			throw e;
		} catch (Exception e) {
			log.error("일지 수정 실패: userId={}, diaryId={}, error={}", username, id, e.getMessage(), e);
			throw new CustomException(ErrorCode.DATABASE_ERROR, "일지 수정 중 오류가 발생했습니다.");
		}
	}

	/**
	 * 실천일지 삭제
	 */
	public void deleteDiary(String username, Long id) {
		if (id == null || id <= 0) {
			throw new CustomException(ErrorCode.INVALID_INPUT, "유효하지 않은 일지 ID입니다.");
		}

		try {
			Diary diary = diaryRepository.findById(id)
				.orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND, "일지를 찾을 수 없습니다."));

			// 작성자 확인
			if (!diary.getWriter().getUserId().equals(username)) {
				throw new CustomException(ErrorCode.ACCESS_DENIED, "일지 삭제 권한이 없습니다.");
			}

			log.info("일지 삭제 시작: userId={}, diaryId={}", username, id);

			// 🔧 1. 일지 관련 모든 연관 데이터 삭제 (순서 중요!)
			deleteAllRelatedData(diary);

			// 🔧 2. 일지 자체 삭제
			diaryRepository.delete(diary);
			log.info("일지 삭제 완료: userId={}, diaryId={}", username, id);
			
		} catch (CustomException e) {
			throw e;
		} catch (Exception e) {
			log.error("일지 삭제 실패: userId={}, diaryId={}, error={}", username, id, e.getMessage(), e);
			throw new CustomException(ErrorCode.DATABASE_ERROR, "일지 삭제 중 오류가 발생했습니다.");
		}
	}

	/**
	 * 일지와 관련된 모든 데이터를 삭제
	 */
	private void deleteAllRelatedData(Diary diary) {
		Long diaryId = diary.getId();
		
		try {
			// 1. 일지 댓글 삭제
			List<DiaryComment> comments = diaryCommentRepository.findAllByDiary(diary);
			if (!comments.isEmpty()) {
				diaryCommentRepository.deleteAll(comments);
				log.info("일지 댓글 삭제 완료: diaryId={}, count={}", diaryId, comments.size());
			}

			// 2. 일지 좋아요 삭제 (더 효율적인 방법 사용)
			likeRepository.deleteByTargetTypeAndTargetId("Diary", diaryId);
			log.info("일지 좋아요 삭제 완료: diaryId={}", diaryId);

			// 3. 월간 투표 후보에서 삭제 (VoteCandidate)
			voteCandidateRepository.deleteByDiary(diary);
			log.info("일지 투표 후보 삭제 완료: diaryId={}", diaryId);

			// 4. 일지 관련 스탬프 삭제 (UserStamp)
			userStampRepository.deleteByRelatedEntityTypeAndRelatedEntityId("DIARY", diaryId);
			log.info("일지 관련 스탬프 삭제 완료: diaryId={}", diaryId);

			// 5. 일지 이미지 삭제 (이미 cascade로 처리됨)
			List<DiaryImage> images = diaryImageRepository.findAllByDiary(diary);
			for (DiaryImage image : images) {
				try {
					fileStorageUtil.deleteDiaryFile(image.getSavedFileName());
				} catch (Exception e) {
					log.warn("일지 이미지 파일 삭제 실패: imageId={}, fileName={}", 
						image.getId(), image.getSavedFileName(), e);
					// 파일 삭제 실패해도 DB는 삭제 진행
				}
			}
			log.info("일지 이미지 삭제 처리 완료: diaryId={}", diaryId);
			
		} catch (Exception e) {
			log.error("일지 연관 데이터 삭제 실패: diaryId={}, error={}", diaryId, e.getMessage(), e);
			throw new CustomException(ErrorCode.DATABASE_ERROR, "연관 데이터 삭제 중 오류가 발생했습니다.");
		}
	}

	private void validateDiaryInput(String title, String content, String action) {
		if (title == null || title.trim().isEmpty()) {
			throw new CustomException(ErrorCode.MISSING_REQUIRED_FIELD, "제목은 필수 입력값입니다.");
		}
		
		if (content == null || content.trim().isEmpty()) {
			throw new CustomException(ErrorCode.MISSING_REQUIRED_FIELD, "내용은 필수 입력값입니다.");
		}
		
		if (action == null || action.trim().isEmpty()) {
			throw new CustomException(ErrorCode.MISSING_REQUIRED_FIELD, "실천 행동은 필수 입력값입니다.");
		}
		
		if (title.length() > 100) {
			throw new CustomException(ErrorCode.INVALID_INPUT, "제목은 100자 이하로 입력해주세요.");
		}
		
		if (content.length() > 2000) {
			throw new CustomException(ErrorCode.INVALID_INPUT, "내용은 2000자 이하로 입력해주세요.");
		}
	}

	private void processImages(Diary diary, List<MultipartFile> images) {
		if (images.size() > 5) {
			throw new CustomException(ErrorCode.INVALID_INPUT, "이미지는 최대 5개까지 업로드 가능합니다.");
		}

		for (int i = 0; i < images.size(); i++) {
			MultipartFile image = images.get(i);
			if (!image.isEmpty()) {
				validateImageFile(image);
				
				try {
					String savedFileName = fileStorageUtil.saveDiaryFile(image);

					DiaryImage diaryImage = DiaryImage.builder()
						.diary(diary)
						.imagePath("/uploads/diary/" + savedFileName)
						.originalFileName(image.getOriginalFilename())
						.savedFileName(savedFileName)
						.fileSize(image.getSize())
						.contentType(image.getContentType())
						.imageOrder(i + 1)
						.uploadedAt(LocalDateTime.now())
						.build();

					diaryImageRepository.save(diaryImage);
					
				} catch (Exception e) {
					log.error("이미지 저장 실패: diaryId={}, fileName={}, error={}", 
						diary.getId(), image.getOriginalFilename(), e.getMessage(), e);
					throw new CustomException(ErrorCode.FILE_UPLOAD_FAILED);
				}
			}
		}
	}

	private void validateImageFile(MultipartFile image) {
		// 파일 크기 검증 (5MB)
		if (image.getSize() > 5 * 1024 * 1024) {
			throw new CustomException(ErrorCode.FILE_SIZE_EXCEEDED, "이미지 파일은 5MB 이하로 업로드해주세요.");
		}
		
		// 파일 형식 검증
		String contentType = image.getContentType();
		if (contentType == null || !contentType.startsWith("image/")) {
			throw new CustomException(ErrorCode.INVALID_FILE_FORMAT, "이미지 파일만 업로드 가능합니다.");
		}
	}

	private List<DiaryImageResponse> processDiaryImages(Diary diary) {
		try {
			return diaryImageRepository.findAllByDiary(diary)
				.stream()
				.sorted((img1, img2) -> img1.getImageOrder().compareTo(img2.getImageOrder()))
				.map(image -> {
					try {
						String imageUrl = imageUtil.generateImageUrl(image.getSavedFileName(), "diary");
						String mimeType = imageUtil.getImageMimeType(image.getOriginalFileName());

						// 이미지 파일 존재 여부 확인
						String fullPath = "uploads/diary/" + image.getSavedFileName();
						if (!imageUtil.imageExists(fullPath)) {
							log.warn("이미지 파일이 존재하지 않음, 기본 URL 사용: imageId={}, path={}", image.getId(), fullPath);
							imageUrl = imageUtil.getDefaultImageUrl();
							mimeType = "image/png";
						}

						return DiaryImageResponse.builder()
							.id(image.getId())
							.originalFileName(image.getOriginalFileName())
							.imageUrl(imageUrl)
							.mimeType(mimeType)
							.imageOrder(image.getImageOrder())
							.fileSize(image.getFileSize())
							.build();
					} catch (Exception e) {
						log.error("이미지 처리 실패: imageId={}, error={}", image.getId(), e.getMessage());
						// 이미지 처리 실패시 기본 URL로 응답 생성
						return DiaryImageResponse.builder()
							.id(image.getId())
							.originalFileName(image.getOriginalFileName())
							.imageUrl(imageUtil.getDefaultImageUrl())
							.mimeType("image/png")
							.imageOrder(image.getImageOrder())
							.fileSize(0L)
							.build();
					}
				})
				.toList();
		} catch (Exception e) {
			log.error("일지 이미지 목록 처리 실패: diaryId={}, error={}", diary.getId(), e.getMessage(), e);
			// 이미지 처리 실패시 빈 리스트 반환
			return List.of();
		}
	}
}
