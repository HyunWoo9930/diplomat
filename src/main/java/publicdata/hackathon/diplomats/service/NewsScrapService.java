package publicdata.hackathon.diplomats.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import publicdata.hackathon.diplomats.domain.dto.response.MyScrapListResponse;
import publicdata.hackathon.diplomats.domain.dto.response.PaginationInfo;
import publicdata.hackathon.diplomats.domain.dto.response.ScrapItem;
import publicdata.hackathon.diplomats.domain.dto.response.ScrapResponse;
import publicdata.hackathon.diplomats.domain.entity.NewsScrap;
import publicdata.hackathon.diplomats.domain.entity.PressRelease;
import publicdata.hackathon.diplomats.domain.entity.User;
import publicdata.hackathon.diplomats.repository.NewsScrapRepository;
import publicdata.hackathon.diplomats.repository.PressReleaseRepository;
import publicdata.hackathon.diplomats.repository.UserRepository;

@Service
@RequiredArgsConstructor
@Transactional
public class NewsScrapService {

	private final NewsScrapRepository newsScrapRepository;
	private final PressReleaseRepository pressReleaseRepository;
	private final UserRepository userRepository;

	public ScrapResponse toggleScrap(String username, Long newsId) {
		User user = userRepository.findByUserId(username)
			.orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다."));

		PressRelease pressRelease = pressReleaseRepository.findById(newsId)
			.orElseThrow(() -> new EntityNotFoundException("뉴스를 찾을 수 없습니다."));

		// 이미 스크랩했는지 확인
		boolean alreadyScrapped = newsScrapRepository.existsByUserAndPressRelease(user, pressRelease);

		if (alreadyScrapped) {
			// 스크랩 취소
			newsScrapRepository.deleteByUserAndPressRelease(user, pressRelease);

			long scrapCount = newsScrapRepository.countByPressRelease(pressRelease);
			return ScrapResponse.builder()
				.isScrapped(false)
				.scrapCount(scrapCount)
				.message("스크랩을 취소했습니다.")
				.build();
		} else {
			// 스크랩 추가
			NewsScrap newsScrap = NewsScrap.builder()
				.user(user)
				.pressRelease(pressRelease)
				.build();

			newsScrapRepository.save(newsScrap);

			long scrapCount = newsScrapRepository.countByPressRelease(pressRelease);
			return ScrapResponse.builder()
				.isScrapped(true)
				.scrapCount(scrapCount)
				.message("스크랩했습니다.")
				.build();
		}
	}

	@Transactional(readOnly = true)
	public boolean isNewsScrappedByUser(String username, Long newsId) {
		User user = userRepository.findByUserId(username)
			.orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다."));

		PressRelease pressRelease = pressReleaseRepository.findById(newsId)
			.orElseThrow(() -> new EntityNotFoundException("뉴스를 찾을 수 없습니다."));

		return newsScrapRepository.existsByUserAndPressRelease(user, pressRelease);
	}

	@Transactional(readOnly = true)
	public ScrapResponse getScrapStatus(String username, Long newsId) {
		User user = userRepository.findByUserId(username)
			.orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다."));

		PressRelease pressRelease = pressReleaseRepository.findById(newsId)
			.orElseThrow(() -> new EntityNotFoundException("뉴스를 찾을 수 없습니다."));

		boolean isScrapped = newsScrapRepository.existsByUserAndPressRelease(user, pressRelease);
		long scrapCount = newsScrapRepository.countByPressRelease(pressRelease);

		return ScrapResponse.builder()
			.isScrapped(isScrapped)
			.scrapCount(scrapCount)
			.message("스크랩 상태 조회 성공")
			.build();
	}

	@Transactional(readOnly = true)
	public MyScrapListResponse getMyScraps(String username, int page, int size) {
		User user = userRepository.findByUserId(username)
			.orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다."));

		Pageable pageable = PageRequest.of(page, size);
		Page<NewsScrap> scrapPage = newsScrapRepository.findByUserOrderByScrapedAtDesc(user, pageable);

		// 스크랩 아이템 변환
		List<ScrapItem> scrapItems = scrapPage.getContent().stream()
			.map(this::convertToScrapItem)
			.collect(Collectors.toList());

		// 페이지네이션 정보
		PaginationInfo pagination = PaginationInfo.builder()
			.currentPage(page)
			.totalPages(scrapPage.getTotalPages())
			.pageSize(size)
			.totalCount(scrapPage.getTotalElements())
			.hasNext(scrapPage.hasNext())
			.hasPrev(scrapPage.hasPrevious())
			.build();

		return MyScrapListResponse.builder()
			.scraps(scrapItems)
			.pagination(pagination)
			.build();
	}

	private ScrapItem convertToScrapItem(NewsScrap scrap) {
		PressRelease pressRelease = scrap.getPressRelease();
		String category = determineCategory(pressRelease);
		String summary = createSummary(pressRelease.getContent());

		return ScrapItem.builder()
			.scrapId(scrap.getId())
			.newsId(pressRelease.getId())
			.title(pressRelease.getTitle())
			.summary(summary)
			.url(pressRelease.getUrl())
			.publishDate(pressRelease.getPublishDate())
			.scrapedAt(scrap.getScrapedAt())
			.category(category)
			.categoryDisplay(getCategoryDisplay(category))
			.scrapped(true) // 스크랩 목록에서는 항상 true
			.build();
	}

	private String determineCategory(PressRelease pressRelease) {
		// NewsService와 동일한 로직 사용
		String text = (pressRelease.getTitle() + " " + pressRelease.getContent()).toLowerCase();

		// 간단한 키워드 매칭
		if (text.contains("기후") || text.contains("환경") || text.contains("탄소")) {
			return "CLIMATE";
		} else if (text.contains("문화") || text.contains("예술") || text.contains("한류")) {
			return "CULTURE";
		} else if (text.contains("oda") || text.contains("개발협력") || text.contains("원조")) {
			return "ODA";
		} else if (text.contains("esg") || text.contains("지속가능")) {
			return "ESG";
		}

		return "GENERAL";
	}

	private String getCategoryDisplay(String category) {
		switch (category) {
			case "CLIMATE": return "기후";
			case "CULTURE": return "문화";
			case "ODA": return "ODA";
			case "ESG": return "ESG";
			default: return "일반";
		}
	}

	private String createSummary(String content) {
		if (content == null || content.isEmpty()) {
			return "";
		}

		String cleaned = content.replaceAll("<[^>]*>", "")
			.replaceAll("\\s+", " ")
			.trim();

		return cleaned.length() > 100 ? cleaned.substring(0, 100) + "..." : cleaned;
	}
}