// NewsService.java
package publicdata.hackathon.diplomats.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import publicdata.hackathon.diplomats.domain.dto.response.FilterInfo;
import publicdata.hackathon.diplomats.domain.dto.response.FilterOption;
import publicdata.hackathon.diplomats.domain.dto.response.NewsItem;
import publicdata.hackathon.diplomats.domain.dto.response.NewsListResponse;
import publicdata.hackathon.diplomats.domain.dto.response.PaginationInfo;
import publicdata.hackathon.diplomats.domain.entity.PressRelease;
import publicdata.hackathon.diplomats.domain.entity.User;
import publicdata.hackathon.diplomats.domain.enums.NewsFilter;
import publicdata.hackathon.diplomats.repository.PressReleaseRepository;
import publicdata.hackathon.diplomats.repository.UserRepository;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NewsService {

	private final PressReleaseRepository pressReleaseRepository;
	private final UserRepository userRepository;
	private final @Lazy NewsScrapService newsScrapService;

	public NewsListResponse getNews(String filter, int page, int size, String userId) {
		Pageable pageable = PageRequest.of(page, size);

		NewsFilter newsFilter = parseFilter(filter);
		Page<PressRelease> newsPage = getNewsByFilter(newsFilter, pageable);

		List<NewsItem> newsItems = newsPage.getContent().stream()
			.map(pressRelease -> convertToNewsItem(pressRelease, newsFilter, userId))
			.collect(Collectors.toList());

		PaginationInfo pagination = PaginationInfo.builder()
			.currentPage(page)
			.totalPages(newsPage.getTotalPages())
			.pageSize(size)
			.totalCount(newsPage.getTotalElements())
			.hasNext(newsPage.hasNext())
			.hasPrev(newsPage.hasPrevious())
			.build();

		// 필터 정보
		FilterInfo filterInfo = buildFilterInfo(newsFilter);

		return NewsListResponse.builder()
			.news(newsItems)
			.pagination(pagination)
			.filter(filterInfo)
			.build();
	}

	private NewsFilter parseFilter(String filter) {
		if (filter == null || filter.isEmpty()) {
			return NewsFilter.ALL;
		}

		try {
			return NewsFilter.valueOf(filter.toUpperCase());
		} catch (IllegalArgumentException e) {
			return NewsFilter.ALL;
		}
	}

	private Page<PressRelease> getNewsByFilter(NewsFilter filter, Pageable pageable) {
		switch (filter) {
			case ESG:
			case CLIMATE:
			case CULTURE:
			case ODA:
				return findNewsByKeywords(filter.getKeywords(), pageable);
			case ALL:
			default:
				return pressReleaseRepository.findAllByOrderByPublishDateDescCreatedAtDesc(pageable);
		}
	}

	private Page<PressRelease> findNewsByKeywords(List<String> keywords, Pageable pageable) {
		String keyword1 = !keywords.isEmpty() ? keywords.get(0) : null;
		String keyword2 = keywords.size() > 1 ? keywords.get(1) : null;
		String keyword3 = keywords.size() > 2 ? keywords.get(2) : null;
		String keyword4 = keywords.size() > 3 ? keywords.get(3) : null;
		String keyword5 = keywords.size() > 4 ? keywords.get(4) : null;

		return pressReleaseRepository.findByKeywordsContaining(
			keyword1, keyword2, keyword3, keyword4, keyword5, pageable
		);
	}

	private NewsItem convertToNewsItem(PressRelease pressRelease, NewsFilter filter, String userId) {
		String category = determineCategory(pressRelease);
		String summary = createSummary(pressRelease.getContent());
		
		// 스크랩 상태 확인
		boolean scrapped = false;
		if (userId != null) {
			try {
				scrapped = newsScrapService.isNewsScrappedByUser(userId, pressRelease.getId());
			} catch (Exception e) {
				// 스크랩 상태 조회 실패시 기본값 false
				scrapped = false;
			}
		}

		return NewsItem.builder()
			.id(pressRelease.getId())
			.title(pressRelease.getTitle())
			.summary(summary)
			.url(pressRelease.getUrl())
			.publishDate(pressRelease.getPublishDate())
			.category(category)
			.categoryDisplay(getCategoryDisplay(category))
			.matchScore(pressRelease.getMatchScore())
			.scrapped(scrapped)
			.build();
	}

	private String determineCategory(PressRelease pressRelease) {
		String text = (pressRelease.getTitle() + " " + pressRelease.getContent()).toLowerCase();

		// 우선순위대로 체크
		for (NewsFilter filter : Arrays.asList(NewsFilter.ESG, NewsFilter.CLIMATE, NewsFilter.CULTURE,
			NewsFilter.ODA)) {
			if (filter.getKeywords() != null) {
				for (String keyword : filter.getKeywords()) {
					if (text.contains(keyword.toLowerCase())) {
						return filter.name();
					}
				}
			}
		}

		return "GENERAL"; // 일반 뉴스
	}

	private String getCategoryDisplay(String category) {
		try {
			NewsFilter filter = NewsFilter.valueOf(category);
			return filter.getDisplayName();
		} catch (IllegalArgumentException e) {
			return "일반";
		}
	}

	private String createSummary(String content) {
		if (content == null || content.isEmpty()) {
			return "";
		}

		// HTML 태그 제거 및 요약 생성
		String cleaned = content.replaceAll("<[^>]*>", "")
			.replaceAll("\\s+", " ")
			.trim();

		return cleaned.length() > 150 ? cleaned.substring(0, 150) + "..." : cleaned;
	}

	private FilterInfo buildFilterInfo(NewsFilter currentFilter) {
		// 사용 가능한 필터 옵션들
		List<FilterOption> availableFilters = new ArrayList<>();

		// 전체 개수
		long totalCount = pressReleaseRepository.count();
		availableFilters.add(FilterOption.builder()
			.value("ALL")
			.display("전체")
			.count(totalCount)
			.build());

		// 각 필터별 개수 계산
		for (NewsFilter filter : Arrays.asList(NewsFilter.ESG, NewsFilter.CLIMATE, NewsFilter.CULTURE,
			NewsFilter.ODA)) {
			long count = calculateFilterCount(filter);
			availableFilters.add(FilterOption.builder()
				.value(filter.name())
				.display(filter.getDisplayName())
				.count(count)
				.build());
		}

		return FilterInfo.builder()
			.currentFilter(currentFilter.name())
			.currentFilterDisplay(currentFilter.getDisplayName())
			.availableFilters(availableFilters)
			.build();
	}

	private long calculateFilterCount(NewsFilter filter) {
		if (filter.getKeywords() == null || filter.getKeywords().isEmpty()) {
			return 0;
		}

		// 간단한 구현: 첫 번째 키워드로만 계산
		// 실제로는 모든 키워드를 고려한 정확한 계산 필요
		return pressReleaseRepository.countByKeywordContaining(filter.getKeywords().get(0));
	}

	public NewsListResponse getPersonalizedNews(String username, int page, int size) {
		// 사용자 조회
		User user = userRepository.findByUserId(username)
			.orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다."));

		// 사용자의 시민력 유형 확인
		String citizenType = user.getCitizenType();

		// 유형에 따른 필터 결정
		String filter = determineFilterByCitizenType(citizenType);

		// 기존 getNews 메서드 활용
		NewsListResponse response = getNews(filter, page, size, username);

		// 응답에 맞춤형 정보 추가
		FilterInfo personalizedFilter = FilterInfo.builder()
			.currentFilter("PERSONALIZED")
			.currentFilterDisplay("맞춤형 (" + getDisplayNameByCitizenType(citizenType) + ")")
			.availableFilters(response.getFilter().getAvailableFilters())
			.build();

		return NewsListResponse.builder()
			.news(response.getNews())
			.pagination(response.getPagination())
			.filter(personalizedFilter)
			.build();
	}

	private String determineFilterByCitizenType(String citizenType) {
		if (citizenType == null || citizenType.isEmpty()) {
			// 테스트를 하지 않은 경우 전체 뉴스 제공
			return "ALL";
		}

		// 시민력 유형에 따른 뉴스 필터 매핑
		switch (citizenType) {
			case "CLIMATE_ACTION":
				return "CLIMATE";
			case "PEACE_MEDIATION":
				return "ALL"; // 평화 관련은 별도 필터가 없으므로 전체
			case "CULTURAL_DIPLOMACY":
				return "CULTURE";
			case "ECONOMIC_TRADE":
				return "ALL"; // 경제 관련은 별도 필터가 없으므로 전체
			case "DIGITAL_COMMUNICATION":
				return "ALL"; // 디지털 관련은 별도 필터가 없으므로 전체
			default:
				return "ALL";
		}
	}

	private String getDisplayNameByCitizenType(String citizenType) {
		if (citizenType == null || citizenType.isEmpty()) {
			return "전체";
		}

		switch (citizenType) {
			case "CLIMATE_ACTION":
				return "기후행동형";
			case "PEACE_MEDIATION":
				return "평화중재형";
			case "CULTURAL_DIPLOMACY":
				return "문화외교형";
			case "ECONOMIC_TRADE":
				return "경제통상형";
			case "DIGITAL_COMMUNICATION":
				return "디지털소통형";
			default:
				return "일반";
		}
	}
}