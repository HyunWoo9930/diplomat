package publicdata.hackathon.diplomats.service;

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
import publicdata.hackathon.diplomats.domain.dto.response.PublicDiplomacyApiResponse;
import publicdata.hackathon.diplomats.domain.dto.response.RecommendedDiplomacyProgram;
import publicdata.hackathon.diplomats.domain.entity.PublicDiplomacyProgram;
import publicdata.hackathon.diplomats.repository.PublicDiplomacyProgramRepository;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class PublicDiplomacyService {

	private final PublicDiplomacyProgramRepository programRepository;
	private final RestTemplate restTemplate;

	@Value("${openapi.kf.service-key}")
	private String serviceKey;

	// 유형별 키워드 정의
	private static final Map<String, List<String>> TYPE_KEYWORDS = Map.of(
		"CLIMATE_ACTION", Arrays.asList("환경", "기후", "녹색", "지속가능", "에너지", "생태", "탄소"),
		"PEACE_MEDIATION", Arrays.asList("평화", "갈등", "중재", "협력", "대화", "화해", "안보", "교류"),
		"CULTURAL_DIPLOMACY", Arrays.asList("문화", "예술", "전통", "축제", "공연", "전시", "한류", "교육", "학술"),
		"ECONOMIC_TRADE", Arrays.asList("경제", "무역", "투자", "비즈니스", "기업", "산업", "기술", "혁신"),
		"DIGITAL_COMMUNICATION", Arrays.asList("디지털", "IT", "온라인", "스마트", "기술", "혁신", "사이버", "미디어")
	);

	@Scheduled(cron = "0 0 3 1 * ?") // 매월 1일 새벽 3시
	public void updatePublicDiplomacyPrograms() {
		log.info("공공외교 프로그램 업데이트 시작");
		try {
			fetchAndProcessPrograms();
			log.info("공공외교 프로그램 업데이트 완료");
		} catch (Exception e) {
			log.error("공공외교 프로그램 업데이트 실패", e);
		}
	}

	public void fetchAndProcessPrograms() {
		String apiUrl = String.format(
			"http://apis.data.go.kr/B260004/PublicDiplomacyBusinessInfoService/getPublicDiplomacyBusinessInfoList?serviceKey=%s&pageNo=1&numOfRows=5000",
			serviceKey
		);

		try {
			PublicDiplomacyApiResponse response = restTemplate.getForObject(apiUrl, PublicDiplomacyApiResponse.class);

			if (isValidResponse(response)) {
				List<PublicDiplomacyApiResponse.Item> items = response.getResponse().getBody().getItems().getItem();
				log.info("총 {}개의 공공외교 프로그램을 가져왔습니다.", items.size());

				// 기존 데이터 삭제
				programRepository.deleteAll();
				programRepository.flush();

				// 각 프로그램 처리
				processPrograms(items);

				// 각 유형별 상위 5개만 유지
				keepTopProgramsPerType();

				logProcessingResults();

			} else {
				throw new RuntimeException("API 응답이 올바르지 않습니다.");
			}
		} catch (Exception e) {
			log.error("API 호출 실패", e);
			throw new RuntimeException("공공외교 프로그램 업데이트 실패: " + e.getMessage(), e);
		}
	}

	private boolean isValidResponse(PublicDiplomacyApiResponse response) {
		return response != null &&
			response.getResponse() != null &&
			"0".equals(response.getResponse().getHeader().getResultCode()) &&
			response.getResponse().getBody() != null &&
			response.getResponse().getBody().getItems() != null &&
			response.getResponse().getBody().getItems().getItem() != null;
	}

	private void processPrograms(List<PublicDiplomacyApiResponse.Item> items) {
		int processedCount = 0;
		int errorCount = 0;

		for (PublicDiplomacyApiResponse.Item item : items) {
			try {
				if (processProgram(item)) {
					processedCount++;
				}
			} catch (Exception e) {
				errorCount++;
				log.warn("프로그램 처리 실패: {} - {}", item.getBusinessName(), e.getMessage());
			}
		}

		log.info("처리 완료: 성공 {}개, 실패 {}개", processedCount, errorCount);
	}

	private boolean processProgram(PublicDiplomacyApiResponse.Item item) {
		if (item.getBusinessName() == null || item.getBusinessName().trim().isEmpty()) {
			return false;
		}

		String businessName = item.getBusinessName().trim();
		String businessPurpose = item.getBusinessPurpose() != null ? item.getBusinessPurpose() : "";
		String unitBusiness = item.getUnitBusiness() != null ? item.getUnitBusiness() : "";
		String detailBusiness = item.getDetailBusiness() != null ? item.getDetailBusiness() : "";

		// 유형 매칭
		String bestMatchType = findBestMatchingType(businessName, businessPurpose, unitBusiness, detailBusiness);
		int matchScore = calculateMatchScore(businessName, businessPurpose, unitBusiness, detailBusiness, bestMatchType);

		if (matchScore <= 0) {
			return false;
		}

		try {
			PublicDiplomacyProgram program = PublicDiplomacyProgram.builder()
				.countryName(item.getCountryName())
				.countryEngName(item.getCountryEngName())
				.countryIsoCode(item.getCountryIsoCode())
				.businessName(businessName)
				.businessEngName(item.getBusinessEngName())
				.businessPurpose(businessPurpose)
				.businessTarget(item.getBusinessTarget())
				.unitBusiness(unitBusiness)
				.detailBusiness(detailBusiness)
				.businessYear(item.getBusinessYear())
				.multiYearType(item.getMultiYearType())
				.citizenType(bestMatchType)
				.matchScore(matchScore)
				.build();

			programRepository.save(program);
			return true;

		} catch (Exception e) {
			log.error("프로그램 저장 실패: {} - {}", businessName, e.getMessage());
			return false;
		}
	}

	private String findBestMatchingType(String businessName, String businessPurpose, String unitBusiness, String detailBusiness) {
		String fullText = (businessName + " " + businessPurpose + " " + unitBusiness + " " + detailBusiness).toLowerCase();
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

	private int calculateMatchScore(String businessName, String businessPurpose, String unitBusiness, String detailBusiness, String type) {
		String fullText = (businessName + " " + businessPurpose + " " + unitBusiness + " " + detailBusiness).toLowerCase();
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

	private void keepTopProgramsPerType() {
		for (String type : TYPE_KEYWORDS.keySet()) {
			List<PublicDiplomacyProgram> allPrograms = programRepository.findTop5ByCitizenTypeOrderByMatchScoreDescBusinessYearDesc(type);

			if (allPrograms.size() > 5) {
				List<PublicDiplomacyProgram> toDelete = allPrograms.subList(5, allPrograms.size());
				programRepository.deleteAll(toDelete);
			}
		}
	}

	private void logProcessingResults() {
		List<Object[]> results = programRepository.countByEachType();
		log.info("=== 유형별 공공외교 프로그램 처리 결과 ===");
		for (Object[] result : results) {
			log.info("{}: {}개", result[0], result[1]);
		}
	}

	// 특정 유형의 추천 프로그램 조회
	public List<RecommendedDiplomacyProgram> getRecommendedPrograms(String citizenType) {
		List<PublicDiplomacyProgram> programs = programRepository.findTop5ByCitizenTypeOrderByMatchScoreDescBusinessYearDesc(citizenType);

		return programs.stream()
			.map(this::convertToRecommendedDto)
			.collect(Collectors.toList());
	}

	private RecommendedDiplomacyProgram convertToRecommendedDto(PublicDiplomacyProgram program) {
		return RecommendedDiplomacyProgram.builder()
			.countryName(program.getCountryName())
			.businessName(program.getBusinessName())
			.businessPurpose(program.getBusinessPurpose())
			.businessTarget(program.getBusinessTarget())
			.unitBusiness(program.getUnitBusiness())
			.businessYear(program.getBusinessYear())
			.matchScore(program.getMatchScore())
			.build();
	}

	// PublicDiplomacyService.java에 추가
	public Map<String, Object> getProgramStatus() {
		Map<String, Object> status = new HashMap<>();

		// 전체 프로그램 개수
		long totalCount = programRepository.count();
		status.put("totalCount", totalCount);

		// 각 유형별 개수
		Map<String, Long> typeCountMap = new HashMap<>();
		List<Object[]> typeCounts = programRepository.countByEachType();

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

		// 최근 업데이트 시간
		programRepository.findTopByOrderByCreatedAtDesc()
			.ifPresent(latestProgram ->
				status.put("lastUpdated", latestProgram.getCreatedAt())
			);

		// 최신 사업연도
		programRepository.findTopByOrderByBusinessYearDesc()
			.ifPresent(latestYear ->
				status.put("latestBusinessYear", latestYear.getBusinessYear())
			);

		return status;
	}

	// 각 유형별 프로그램 샘플 조회 (사업명만)
	public Map<String, List<String>> getTypeExamples() {
		Map<String, List<String>> examples = new HashMap<>();

		for (String type : TYPE_KEYWORDS.keySet()) {
			List<PublicDiplomacyProgram> programs = programRepository.findTop3ByCitizenTypeOrderByMatchScoreDesc(type);
			List<String> businessNames = programs.stream()
				.map(PublicDiplomacyProgram::getBusinessName)
				.collect(Collectors.toList());
			examples.put(type, businessNames);
		}

		return examples;
	}
}