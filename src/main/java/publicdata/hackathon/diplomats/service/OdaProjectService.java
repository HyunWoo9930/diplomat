package publicdata.hackathon.diplomats.service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import publicdata.hackathon.diplomats.domain.dto.response.OdaApiResponse;
import publicdata.hackathon.diplomats.domain.entity.OdaProject;
import publicdata.hackathon.diplomats.repository.OdaProjectRepository;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class OdaProjectService {

	private final OdaProjectRepository odaProjectRepository;
	private final RestTemplate restTemplate;

	@Value("${openapi.mofa.service-key}")
	private String serviceKey;

	// 분야별 키워드 정의
	private static final Map<String, List<String>> CATEGORY_KEYWORDS = Map.of(
		"ENVIRONMENT", Arrays.asList("환경", "기후", "탄소", "녹색", "지속가능", "에너지", "친환경", "온실가스", "탄소중립", "청정에너지", "재생에너지"),
		"EDUCATION", Arrays.asList("교육", "학교", "교사", "학습", "훈련", "역량", "인재", "대학", "직업교육", "기술교육", "교육과정"),
		"HEALTH", Arrays.asList("보건", "의료", "건강", "병원", "의사", "간호", "치료", "예방", "백신", "질병", "의약품", "모자보건"),
		"WOMEN", Arrays.asList("여성", "성평등", "젠더", "모성", "출산", "육아", "여성참여", "여성리더십", "여성경제", "성인지"),
		"OTHERS", Arrays.asList("농업", "농촌", "인프라", "교통", "통신", "IT", "거버넌스", "법제도", "평화", "인권", "민주주의")
	);

	// 카테고리별 한국어 이름 매핑
	private static final Map<String, String> CATEGORY_DISPLAY_NAMES = Map.of(
		"ENVIRONMENT", "환경·기후변화",
		"EDUCATION", "교육·인재양성",
		"HEALTH", "보건·의료",
		"WOMEN", "여성·성평등",
		"OTHERS", "기타·종합개발"
	);

	/**
	 * 매월 자동으로 ODA 프로젝트 데이터를 수집하고 처리합니다.
	 * 매월 1일 새벽 3시에 실행됩니다.
	 */
	@Scheduled(cron = "0 0 3 1 * ?") // 매월 1일 새벽 3시
	public void updateOdaProjects() {
		log.info("ODA 프로젝트 업데이트 시작");
		try {
			fetchAndProcessOdaProjects();
			log.info("ODA 프로젝트 업데이트 완료");
		} catch (Exception e) {
			log.error("ODA 프로젝트 업데이트 실패", e);
		}
	}

	/**
	 * 공공데이터 API에서 ODA 프로젝트 정보를 가져와서 분야별로 분류하고 저장합니다.
	 * 각 분야별로 상위 5개 프로젝트만 유지합니다.
	 */
	@Transactional
	public void fetchAndProcessOdaProjects() {
		String apiUrl = String.format(
			"https://apis.data.go.kr/1262000/pressRlsService/getPressRls?serviceKey=%s&pageNo=1&numOfRows=500&returnType=json",
			serviceKey
		);

		try {
			// 실제로는 ODA 전용 API를 사용해야 하지만, 일단 기존 API로 시뮬레이션
			OdaApiResponse response = restTemplate.getForObject(apiUrl, OdaApiResponse.class);

			if (isValidResponse(response)) {
				List<OdaApiResponse.Item> items = response.getResponse().getBody().getItems().getItem();
				log.info("총 {}개의 ODA 프로젝트 정보를 가져왔습니다.", items.size());

				// 기존 데이터 삭제
				odaProjectRepository.deleteAll();
				odaProjectRepository.flush();

				// 각 프로젝트 처리
				processOdaItems(items);

				// 각 분야별 상위 5개만 유지
				keepTopProjectsPerCategory();

				logProcessingResults();

			} else {
				throw new RuntimeException("ODA API 응답이 올바르지 않습니다.");
			}
		} catch (Exception e) {
			log.error("ODA API 호출 실패", e);
			throw new RuntimeException("ODA 프로젝트 업데이트 실패: " + e.getMessage(), e);
		}
	}

	private boolean isValidResponse(OdaApiResponse response) {
		return response != null &&
			response.getResponse() != null &&
			"0".equals(response.getResponse().getHeader().getResultCode()) &&
			response.getResponse().getBody() != null &&
			response.getResponse().getBody().getItems() != null &&
			response.getResponse().getBody().getItems().getItem() != null;
	}

	private void processOdaItems(List<OdaApiResponse.Item> items) {
		int processedCount = 0;
		int errorCount = 0;

		for (OdaApiResponse.Item item : items) {
			try {
				if (processOdaItem(item)) {
					processedCount++;
				}
			} catch (Exception e) {
				errorCount++;
				log.warn("ODA 프로젝트 처리 실패: {} - {}", getItemTitle(item), e.getMessage());
			}
		}

		log.info("ODA 프로젝트 처리 완료: 성공 {}개, 실패 {}개", processedCount, errorCount);
	}

	private boolean processOdaItem(OdaApiResponse.Item item) {
		String title = getItemTitle(item);
		if (title == null || title.trim().isEmpty()) {
			log.debug("제목이 없는 ODA 프로젝트 건너뜀");
			return false;
		}

		String content = cleanHtmlContent(getItemContent(item));
		String countryName = getItemCountry(item);

		// 분야 매칭
		String bestMatchCategory = findBestMatchingCategory(title, content);
		int matchScore = calculateMatchScore(title, content, bestMatchCategory);

		if (matchScore <= 0) {
			return false;
		}

		try {
			LocalDate publishDate = parseDate(item.getUpdtDate());
			LocalDate projectStartDate = parseDate(getItemStartDate(item));
			LocalDate projectEndDate = parseDate(getItemEndDate(item));
			String url = createProjectUrl(item.getFileUrl());
			String budget = getItemBudget(item);

			OdaProject odaProject = OdaProject.builder()
				.title(title.trim())
				.content(content)
				.url(url)
				.category(bestMatchCategory)
				.countryName(countryName != null ? countryName : "개발도상국")
				.projectStartDate(projectStartDate)
				.projectEndDate(projectEndDate)
				.budget(budget)
				.publishDate(publishDate)
				.matchScore(matchScore)
				.build();

			odaProjectRepository.save(odaProject);
			return true;

		} catch (Exception e) {
			log.error("ODA 프로젝트 저장 실패: {} - {}", title, e.getMessage());
			return false;
		}
	}

	private String getItemTitle(OdaApiResponse.Item item) {
		if (item.getProjectTitle() != null) return item.getProjectTitle();
		if (item.getTitle() != null) return item.getTitle();
		return null;
	}

	private String getItemContent(OdaApiResponse.Item item) {
		if (item.getProjectDescription() != null) return item.getProjectDescription();
		if (item.getContent() != null) return item.getContent();
		return "";
	}

	private String getItemCountry(OdaApiResponse.Item item) {
		if (item.getTargetCountry() != null) return item.getTargetCountry();
		if (item.getCountry() != null) return item.getCountry();
		return null;
	}

	private String getItemStartDate(OdaApiResponse.Item item) {
		if (item.getProjectStartDate() != null) return item.getProjectStartDate();
		if (item.getStartDate() != null) return item.getStartDate();
		return null;
	}

	private String getItemEndDate(OdaApiResponse.Item item) {
		if (item.getProjectEndDate() != null) return item.getProjectEndDate();
		if (item.getEndDate() != null) return item.getEndDate();
		return null;
	}

	private String getItemBudget(OdaApiResponse.Item item) {
		if (item.getProjectBudget() != null) return item.getProjectBudget();
		if (item.getBudget() != null) return item.getBudget();
		return null;
	}

	private String cleanHtmlContent(String htmlContent) {
		if (htmlContent == null) return "";
		String cleaned = htmlContent.replaceAll("<[^>]*>", "");
		cleaned = cleaned.replace("&nbsp;", " ")
			.replace("&lt;", "<")
			.replace("&gt;", ">")
			.replace("&amp;", "&")
			.replace("&quot;", "\"")
			.replace("&#39;", "'");
		return cleaned.replaceAll("\\s+", " ").trim();
	}

	private String createProjectUrl(String fileUrl) {
		if (fileUrl != null && !fileUrl.isEmpty()) {
			if (fileUrl.startsWith("www.")) {
				return "https://" + fileUrl;
			} else if (!fileUrl.startsWith("http")) {
				return "https://www.mofa.go.kr/" + fileUrl;
			}
			return fileUrl;
		}
		return "https://www.mofa.go.kr/www/brd/m_4080/list.do";
	}

	private LocalDate parseDate(String dateString) {
		try {
			if (dateString != null && !dateString.isEmpty()) {
				if (dateString.matches("\\d{4}-\\d{2}-\\d{2}")) {
					return LocalDate.parse(dateString, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
				} else if (dateString.matches("\\d{4}\\.\\d{2}\\.\\d{2}")) {
					return LocalDate.parse(dateString, DateTimeFormatter.ofPattern("yyyy.MM.dd"));
				} else if (dateString.matches("\\d{4}/\\d{2}/\\d{2}")) {
					return LocalDate.parse(dateString, DateTimeFormatter.ofPattern("yyyy/MM/dd"));
				}
			}
		} catch (Exception e) {
			log.warn("날짜 파싱 실패: {}", dateString);
		}
		return LocalDate.now();
	}

	private String findBestMatchingCategory(String title, String content) {
		String fullText = ((title != null ? title : "") + " " + (content != null ? content : "")).toLowerCase();
		Map<String, Integer> categoryScores = new HashMap<>();

		for (Map.Entry<String, List<String>> entry : CATEGORY_KEYWORDS.entrySet()) {
			int score = 0;
			for (String keyword : entry.getValue()) {
				score += countOccurrences(fullText, keyword);
			}
			categoryScores.put(entry.getKey(), score);
		}

		return categoryScores.entrySet().stream()
			.max(Map.Entry.comparingByValue())
			.map(Map.Entry::getKey)
			.orElse("OTHERS");
	}

	private int calculateMatchScore(String title, String content, String category) {
		String fullText = ((title != null ? title : "") + " " + (content != null ? content : "")).toLowerCase();
		List<String> keywords = CATEGORY_KEYWORDS.get(category);

		int score = 0;
		for (String keyword : keywords) {
			score += countOccurrences(fullText, keyword);
		}
		return score;
	}

	private int countOccurrences(String text, String keyword) {
		int count = 0;
		int index = 0;
		while ((index = text.indexOf(keyword, index)) != -1) {
			count++;
			index += keyword.length();
		}
		return count;
	}

	private void keepTopProjectsPerCategory() {
		for (String category : CATEGORY_KEYWORDS.keySet()) {
			List<OdaProject> allProjects = odaProjectRepository.findTop5ByCategoryOrderByMatchScoreDescPublishDateDesc(category);
			if (allProjects.size() > 5) {
				List<OdaProject> toDelete = allProjects.subList(5, allProjects.size());
				odaProjectRepository.deleteAll(toDelete);
			}
		}
	}

	private void logProcessingResults() {
		List<Object[]> results = odaProjectRepository.countByEachCategory();
		log.info("=== 분야별 ODA 프로젝트 처리 결과 ===");
		for (Object[] result : results) {
			String category = (String) result[0];
			String displayName = CATEGORY_DISPLAY_NAMES.getOrDefault(category, category);
			log.info("{} ({}): {}개", displayName, category, result[1]);
		}
	}

	public Map<String, Object> getOdaProjectStatus() {
		Map<String, Object> status = new HashMap<>();
		long totalCount = odaProjectRepository.count();
		status.put("totalCount", totalCount);

		Map<String, Long> categoryCountMap = new HashMap<>();
		List<Object[]> categoryCounts = odaProjectRepository.countByEachCategory();

		for (Object[] result : categoryCounts) {
			String category = (String) result[0];
			Long count = (Long) result[1];
			categoryCountMap.put(category, count);
		}

		status.put("categoryDistribution", categoryCountMap);
		status.put("categoryDisplayNames", CATEGORY_DISPLAY_NAMES);

		return status;
	}
}
