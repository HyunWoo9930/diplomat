package publicdata.hackathon.diplomats.service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import publicdata.hackathon.diplomats.domain.dto.response.MofaApiResponse;
import publicdata.hackathon.diplomats.domain.dto.response.RecommendedPressRelease;
import publicdata.hackathon.diplomats.domain.entity.PressRelease;
import publicdata.hackathon.diplomats.repository.PressReleaseRepository;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class PressReleaseService {

	private final PressReleaseRepository pressReleaseRepository;
	private final RestTemplate restTemplate;

	@Value("${openapi.mofa.service-key}")
	private String serviceKey;

	// 유형별 키워드 정의
	private static final Map<String, List<String>> TYPE_KEYWORDS = Map.of(
		"CLIMATE_ACTION", Arrays.asList("기후", "환경", "탄소", "녹색", "지속가능", "에너지", "친환경", "온실가스", "탄소중립"),
		"PEACE_MEDIATION", Arrays.asList("평화", "중재", "협상", "대화", "갈등", "해결", "조정", "화해", "북한", "안보"),
		"CULTURAL_DIPLOMACY", Arrays.asList("문화", "교류", "전통", "예술", "한류", "축제", "공연", "전시", "K-컬처", "문화협력"),
		"ECONOMIC_TRADE", Arrays.asList("경제", "무역", "투자", "협력", "통상", "FTA", "수출", "기업", "경제협력", "비즈니스"),
		"DIGITAL_COMMUNICATION", Arrays.asList("디지털", "IT", "기술", "혁신", "온라인", "스마트", "AI", "사이버", "4차산업", "ICT")
	);

	@Scheduled(cron = "0 0 2 1 * ?")
	public void updatePressReleases() {
		log.info("보도자료 업데이트 시작");
		try {
			fetchAndProcessPressReleases();
			log.info("보도자료 업데이트 완료");
		} catch (Exception e) {
			log.error("보도자료 업데이트 실패", e);
		}
	}

	private void logProcessingResults() {
		List<Object[]> results = pressReleaseRepository.countByEachType();
		log.info("=== 유형별 보도자료 처리 결과 ===");
		for (Object[] result : results) {
			log.info("{}: {}개", result[0], result[1]);
		}
	}

	private String cleanHtmlContent(String htmlContent) {
		if (htmlContent == null) {
			return "";
		}

		// HTML 태그 제거
		String cleaned = htmlContent.replaceAll("<[^>]*>", "");

		// HTML 엔티티 디코딩
		cleaned = cleaned.replace("&nbsp;", " ")
			.replace("&lt;", "<")
			.replace("&gt;", ">")
			.replace("&amp;", "&")
			.replace("&quot;", "\"")
			.replace("&#39;", "'");

		// 여러 공백을 하나로 통합
		cleaned = cleaned.replaceAll("\\s+", " ").trim();

		return cleaned;
	}

	private String createPressReleaseUrl(String fileUrl) {
		if (fileUrl != null && !fileUrl.isEmpty()) {
			// 상대 경로인 경우 절대 경로로 변경
			if (fileUrl.startsWith("www.")) {
				return "https://" + fileUrl;
			} else if (!fileUrl.startsWith("http")) {
				return "https://www.mofa.go.kr/" + fileUrl;
			}
			return fileUrl;
		}

		// 기본 외교부 보도자료 페이지
		return "https://www.mofa.go.kr/www/brd/m_4080/list.do";
	}

	private LocalDate parseDate(String dateString) {
		try {
			if (dateString != null && !dateString.isEmpty()) {
				// "2022-05-27" 형태
				return LocalDate.parse(dateString, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
			}
		} catch (Exception e) {
			log.warn("날짜 파싱 실패: {}", dateString);
		}
		return LocalDate.now();
	}

	private String findBestMatchingType(String title, String content) {
		String fullText = ((title != null ? title : "") + " " + (content != null ? content : "")).toLowerCase();
		Map<String, Integer> typeScores = new HashMap<>();

		for (Map.Entry<String, List<String>> entry : TYPE_KEYWORDS.entrySet()) {
			int score = 0;
			for (String keyword : entry.getValue()) {
				score += countOccurrences(fullText, keyword);
			}
			typeScores.put(entry.getKey(), score);
		}

		return typeScores.entrySet().stream()
			.max(Map.Entry.comparingByValue())
			.map(Map.Entry::getKey)
			.orElse("CULTURAL_DIPLOMACY");
	}

	private int calculateMatchScore(String title, String content, String type) {
		String fullText = ((title != null ? title : "") + " " + (content != null ? content : "")).toLowerCase();
		List<String> keywords = TYPE_KEYWORDS.get(type);

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

	private void keepTopReleasesPerType() {
		for (String type : TYPE_KEYWORDS.keySet()) {
			List<PressRelease> allReleases = pressReleaseRepository.findTop5ByCitizenTypeOrderByMatchScoreDescPublishDateDesc(type);

			// 상위 5개를 제외한 나머지 삭제
			if (allReleases.size() > 5) {
				List<PressRelease> toDelete = allReleases.subList(5, allReleases.size());
				pressReleaseRepository.deleteAll(toDelete);
			}
		}
	}

	public List<RecommendedPressRelease> getRecommendedPressReleases(String citizenType) {
		List<PressRelease> releases = pressReleaseRepository.findTop5ByCitizenTypeOrderByMatchScoreDescPublishDateDesc(citizenType);

		return releases.stream()
			.map(this::convertToRecommendedDto)
			.collect(Collectors.toList());
	}

	private RecommendedPressRelease convertToRecommendedDto(PressRelease pressRelease) {
		String summary = createSummary(pressRelease.getContent());

		return RecommendedPressRelease.builder()
			.title(pressRelease.getTitle())
			.url(pressRelease.getUrl())
			.publishDate(pressRelease.getPublishDate())
			.summary(summary)
			.matchScore(pressRelease.getMatchScore())
			.build();
	}

	private String createSummary(String content) {
		if (content == null || content.isEmpty()) {
			return "";
		}

		// 첫 100자로 요약 생성
		String summary = content.length() > 100 ? content.substring(0, 100) + "..." : content;
		return summary.replaceAll("\\s+", " ").trim();
	}

	public Map<String, Object> getPressReleaseStatus() {
		Map<String, Object> status = new HashMap<>();

		// 전체 보도자료 개수
		long totalCount = pressReleaseRepository.count();
		status.put("totalCount", totalCount);

		// 각 유형별 개수
		Map<String, Long> typeCountMap = new HashMap<>();
		List<Object[]> typeCounts = pressReleaseRepository.countByEachType();

		for (Object[] result : typeCounts) {
			String type = (String) result[0];
			Long count = (Long) result[1];
			typeCountMap.put(type, count);
		}

		status.put("typeDistribution", typeCountMap);

		// 유형별 한국어 이름 매핑
		Map<String, String> typeDisplayNames = Map.of(
			"CLIMATE_ACTION", "기후행동형",
			"PEACE_MEDIATION", "평화중재형",
			"CULTURAL_DIPLOMACY", "문화외교형",
			"ECONOMIC_TRADE", "경제통상형",
			"DIGITAL_COMMUNICATION", "디지털소통형"
		);
		status.put("typeDisplayNames", typeDisplayNames);

		// 최근 업데이트 시간 (가장 최근 생성된 보도자료의 생성 시간)
		pressReleaseRepository.findTopByOrderByCreatedAtDesc()
			.ifPresent(latestRelease ->
				status.put("lastUpdated", latestRelease.getCreatedAt())
			);

		return status;
	}

	public Map<String, List<String>> getTypeExamples() {
		Map<String, List<String>> examples = new HashMap<>();

		for (String type : TYPE_KEYWORDS.keySet()) {
			List<PressRelease> releases = pressReleaseRepository.findTop3ByCitizenTypeOrderByMatchScoreDesc(type);
			List<String> titles = releases.stream()
				.map(PressRelease::getTitle)
				.collect(Collectors.toList());
			examples.put(type, titles);
		}

		return examples;
	}

	@Transactional
	public void fetchAndProcessPressReleases() {
		String apiUrl = String.format(
			"https://apis.data.go.kr/1262000/pressRlsService/getPressRls?serviceKey=%s&pageNo=1&numOfRows=500&returnType=json",
			serviceKey
		);

		try {
			MofaApiResponse response = restTemplate.getForObject(apiUrl, MofaApiResponse.class);

			if (isValidResponse(response)) {
				List<MofaApiResponse.Item> items = response.getResponse().getBody().getItems().getItem();
				log.info("총 {}개의 보도자료를 가져왔습니다.", items.size());

				// 기존 데이터 삭제
				pressReleaseRepository.deleteAll();
				pressReleaseRepository.flush(); // 즉시 삭제 실행

				// 각 보도자료 처리
				processItems(items);

				// 각 유형별 상위 5개만 유지
				keepTopReleasesPerType();

				logProcessingResults();

			} else {
				throw new RuntimeException("API 응답이 올바르지 않습니다.");
			}
		} catch (Exception e) {
			log.error("API 호출 실패", e);
			throw new RuntimeException("보도자료 업데이트 실패: " + e.getMessage(), e);
		}
	}

	private boolean isValidResponse(MofaApiResponse response) {
		return response != null &&
			response.getResponse() != null &&
			"0".equals(response.getResponse().getHeader().getResultCode()) &&
			response.getResponse().getBody() != null &&
			response.getResponse().getBody().getItems() != null &&
			response.getResponse().getBody().getItems().getItem() != null;
	}

	private void processItems(List<MofaApiResponse.Item> items) {
		int processedCount = 0;
		int errorCount = 0;

		for (MofaApiResponse.Item item : items) {
			try {
				if (processItem(item)) {
					processedCount++;
				}
			} catch (Exception e) {
				errorCount++;
				log.warn("보도자료 처리 실패: {} - {}", item.getTitle(), e.getMessage());
			}
		}

		log.info("처리 완료: 성공 {}개, 실패 {}개", processedCount, errorCount);
	}

	private boolean processItem(MofaApiResponse.Item item) {
		// 필수 데이터 검증
		if (item.getTitle() == null || item.getTitle().trim().isEmpty()) {
			log.debug("제목이 없는 보도자료 건너뜀");
			return false;
		}

		String title = item.getTitle().trim();
		String content = cleanHtmlContent(item.getContent());

		// 유형 매칭
		String bestMatchType = findBestMatchingType(title, content);
		int matchScore = calculateMatchScore(title, content, bestMatchType);

		// 매칭 점수가 0인 경우 건너뛰기
		if (matchScore <= 0) {
			log.debug("매칭 점수가 0인 보도자료 건너뜀: {}", title);
			return false;
		}

		try {
			LocalDate publishDate = parseDate(item.getUpdtDate());
			String url = createPressReleaseUrl(item.getFileUrl());

			PressRelease pressRelease = PressRelease.builder()
				.title(title)
				.content(content)
				.url(url)
				.publishDate(publishDate)
				.citizenType(bestMatchType)
				.matchScore(matchScore)
				.build();

			// 저장 전 검증
			if (pressRelease.getTitle() == null || pressRelease.getCitizenType() == null) {
				log.warn("필수 필드가 null인 엔티티 건너뜀: {}", title);
				return false;
			}

			pressReleaseRepository.save(pressRelease);
			log.debug("보도자료 저장 성공: {} (유형: {}, 점수: {})", title, bestMatchType, matchScore);
			return true;

		} catch (Exception e) {
			log.error("보도자료 저장 실패: {} - {}", title, e.getMessage());
			return false;
		}
	}
}