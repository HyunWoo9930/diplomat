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

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import publicdata.hackathon.diplomats.domain.dto.response.DiaryCommentResponse;
import publicdata.hackathon.diplomats.domain.dto.response.DiaryDetailResponse;
import publicdata.hackathon.diplomats.domain.dto.response.DiaryImageResponse;
import publicdata.hackathon.diplomats.domain.dto.response.DiaryResponse;
import publicdata.hackathon.diplomats.domain.dto.response.StampEarnedResponse;
import publicdata.hackathon.diplomats.domain.entity.Diary;
import publicdata.hackathon.diplomats.domain.entity.DiaryImage;
import publicdata.hackathon.diplomats.domain.entity.User;
import publicdata.hackathon.diplomats.repository.DiaryCommentRepository;
import publicdata.hackathon.diplomats.repository.DiaryImageRepository;
import publicdata.hackathon.diplomats.repository.DiaryRepository;
import publicdata.hackathon.diplomats.repository.UserRepository;
import publicdata.hackathon.diplomats.utils.FileStorageUtil;
import publicdata.hackathon.diplomats.utils.ImageUtil;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class DiaryService {

	private final DiaryRepository diaryRepository;
	private final DiaryCommentRepository diaryCommentRepository;
	private final DiaryImageRepository diaryImageRepository;
	private final UserRepository userRepository;
	private final FileStorageUtil fileStorageUtil;
	private final ImageUtil imageUtil;
	private final StampService stampService;

	public void createDiary(String username, String title, String content, String action,
		List<MultipartFile> images) {
		User user = userRepository.findByUserId(username)
			.orElseThrow(() -> new EntityNotFoundException("User not found"));

		Diary diary = Diary.builder()
			.action(action)
			.description(content)
			.title(title)
			.writer(user)
			.build();

		diaryRepository.save(diary);

		// 실천일기 작성 스탬프 지급
		try {
			StampEarnedResponse stampResponse = stampService.earnDiaryWriteStamp(user, diary.getId());
			if (stampResponse.isSuccess()) {
				log.info("실천일기 작성 스탬프 지급 완료: userId={}, diaryId={}, leveledUp={}", 
						username, diary.getId(), stampResponse.isLeveledUp());
			}
		} catch (Exception e) {
			log.error("실천일기 작성 스탬프 지급 실패: userId={}, diaryId={}", username, diary.getId(), e);
		}

		if (images != null && !images.isEmpty()) {
			for (int i = 0; i < images.size(); i++) {
				MultipartFile image = images.get(i);
				if (!image.isEmpty()) {
					String savedFileName = fileStorageUtil.saveDiaryFile(image);

					DiaryImage diaryImage = DiaryImage.builder()
						.diary(diary)
						.imagePath("/uploads/diary/" + savedFileName)
						.originalFileName(image.getOriginalFilename())
						.savedFileName(savedFileName)
						.fileSize(image.getSize())
						.contentType(image.getContentType())
						.imageOrder(i + 1) // 1부터 시작하는 순서
						.uploadedAt(LocalDateTime.now())
						.build();

					diaryImageRepository.save(diaryImage);
				}
			}
		}
	}

	public List<DiaryResponse> getDiaries(String username, Pageable pageable, String sortBy) {
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

		return diaryPage.stream()
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
	}

	public DiaryDetailResponse getDiaryDetails(String username, Long id) {
		Diary diary = diaryRepository.findById(id)
			.orElseThrow(() -> new EntityNotFoundException("Diary not found"));

		List<DiaryCommentResponse> diaryComments = diaryCommentRepository.findAllByDiary(diary)
			.stream()
			.map(diaryComment -> DiaryCommentResponse.builder()
				.id(diaryComment.getId())
				.content(diaryComment.getContent())
				.userId(diaryComment.getCommenter().getUserId())
				.createdAt(diaryComment.getCreatedAt())
				.updatedAt(diaryComment.getUpdatedAt())
				.build())
			.toList();

		List<DiaryImageResponse> images = diaryImageRepository.findAllByDiary(diary)
			.stream()
			.sorted((img1, img2) -> img1.getImageOrder().compareTo(img2.getImageOrder()))
			.map(image -> {
				String fullPath = "uploads/diary/" + image.getSavedFileName();
				String base64Data = imageUtil.encodeImageToBase64(fullPath);
				String mimeType = imageUtil.getImageMimeType(image.getOriginalFileName());

				return DiaryImageResponse.builder()
					.id(image.getId())
					.originalFileName(image.getOriginalFileName())
					.base64Data(base64Data)
					.mimeType(mimeType)
					.imageOrder(image.getImageOrder())
					.build();
			})
			.toList();

		return DiaryDetailResponse.builder()
			.title(diary.getTitle())
			.description(diary.getDescription())
			.action(diary.getAction())
			.likes(diary.getLikes())
			.userId(diary.getWriter().getUserId())
			.createdAt(diary.getCreatedAt())
			.updatedAt(diary.getUpdatedAt())
			.diaryComments(diaryComments)
			.diaryImages(images)
			.build();
	}

	/**
	 * 이번 달 인기 일지 상위 10개 조회
	 */
	public List<DiaryResponse> getTopMonthlyDiaries() {
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
	}
}
