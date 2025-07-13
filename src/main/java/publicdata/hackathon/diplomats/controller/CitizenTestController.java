package publicdata.hackathon.diplomats.controller;

import java.util.Collections;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import publicdata.hackathon.diplomats.domain.dto.request.SubmitAnswersRequest;
import publicdata.hackathon.diplomats.domain.dto.response.CitizenTestQuestionsResponse;
import publicdata.hackathon.diplomats.domain.dto.response.CitizenTestResultResponse;
import publicdata.hackathon.diplomats.jwt.CustomUserDetails;
import publicdata.hackathon.diplomats.service.CitizenTestService;

@RestController
@RequestMapping("/api/v1/citizen-test")
@RequiredArgsConstructor
@Tag(name = "📊 테스트/설문", description = "시민의식 테스트 관련 API")
@CrossOrigin(origins = "*")
public class CitizenTestController {

	private final CitizenTestService citizenTestService;

	@GetMapping("/questions")
	@Operation(summary = "시민력 테스트 질문 조회", description = "모든 질문과 선택지를 조회합니다.")
	public ResponseEntity<CitizenTestQuestionsResponse> getQuestions() {
		try {
			CitizenTestQuestionsResponse response = citizenTestService.getAllQuestions();
			return ResponseEntity.ok(response);
		} catch (Exception e) {
			return ResponseEntity.badRequest().body(
				CitizenTestQuestionsResponse.builder()
					.questions(null)
					.totalQuestions(0)
					.message("질문 조회 실패: " + e.getMessage())
					.build()
			);
		}
	}

	@PostMapping("/submit")
	@Operation(
		summary = "시민력 테스트 답변 제출",
		description = """
			답변을 제출하고 시민력 유형을 계산합니다.
			
			주의사항:
			- questionId: 질문 조회 API에서 받은 질문의 실제 ID
			- optionId: 질문 조회 API에서 받은 선택지의 실제 ID (1,2,3,4,5가 아님)
			- 모든 질문에 대한 답변을 포함해야 함 (총 12개)
			
			예시:
			질문 조회 시 받은 데이터가 다음과 같다면:
			{
			  "id": 1,
			  "content": "외국인을 처음 만났을 때...",
			  "options": [
			    {"id": 1, "optionText": "환경보호와 기후변화...", "optionOrder": 1},
			    {"id": 2, "optionText": "서로의 문화 차이를...", "optionOrder": 2},
			    {"id": 3, "optionText": "한국의 전통문화와...", "optionOrder": 3}
			  ]
			}
			
			답변 제출 시:
			{"questionId": 1, "optionId": 2}  // 두 번째 선택지를 선택한 경우
			"""
	)
	public ResponseEntity<CitizenTestResultResponse> submitAnswers(
		Authentication authentication,
		@RequestBody SubmitAnswersRequest request) {

		try {
			CustomUserDetails userDetails = (CustomUserDetails)authentication.getPrincipal();
			CitizenTestResultResponse response = citizenTestService.submitAnswers(
				userDetails.getUsername(),
				request
			);
			return ResponseEntity.ok(response);
		} catch (Exception e) {
			return ResponseEntity.badRequest().body(
				CitizenTestResultResponse.builder()
					.resultType(null)
					.displayName(null)
					.description(null)
					.message("테스트 제출 실패: " + e.getMessage())
					.build()
			);
		}
	}

	@GetMapping("/my-result")
	@Operation(summary = "내 시민력 테스트 결과 조회", description = "사용자의 시민력 테스트 결과와 추천 보도자료를 조회합니다.")
	public ResponseEntity<CitizenTestResultResponse> getMyTestResult(Authentication authentication) {
		try {
			CustomUserDetails userDetails = (CustomUserDetails)authentication.getPrincipal();
			CitizenTestResultResponse response = citizenTestService.getMyTestResult(userDetails.getUsername());
			return ResponseEntity.ok(response);
		} catch (Exception e) {
			return ResponseEntity.badRequest().body(
				CitizenTestResultResponse.builder()
					.resultType(null)
					.displayName(null)
					.description(null)
					.recommendedNews(Collections.emptyList())
					.message("테스트 결과를 불러오는 중 오류가 발생했습니다: " + e.getMessage())
					.build()
			);
		}
	}

	@GetMapping("/test/climate-action")
	@Operation(summary = "테스트: 기후행동형 결과 조회", description = "기후행동형 시민 외교사 유형의 상세 정보를 조회합니다.")
	public ResponseEntity<CitizenTestResultResponse> getClimateActionType() {
		try {
			CitizenTestResultResponse response = citizenTestService.getTestResult("CLIMATE_ACTION");
			return ResponseEntity.ok(response);
		} catch (Exception e) {
			return ResponseEntity.badRequest().body(
				CitizenTestResultResponse.builder()
					.message("기후행동형 결과 조회 실패: " + e.getMessage())
					.build()
			);
		}
	}

	@GetMapping("/test/peace-mediation")
	@Operation(summary = "테스트: 평화중재형 결과 조회", description = "평화중재형 시민 외교사 유형의 상세 정보를 조회합니다.")
	public ResponseEntity<CitizenTestResultResponse> getPeaceMediationType() {
		try {
			CitizenTestResultResponse response = citizenTestService.getTestResult("PEACE_MEDIATION");
			return ResponseEntity.ok(response);
		} catch (Exception e) {
			return ResponseEntity.badRequest().body(
				CitizenTestResultResponse.builder()
					.message("평화중재형 결과 조회 실패: " + e.getMessage())
					.build()
			);
		}
	}

	@GetMapping("/test/cultural-diplomacy")
	@Operation(summary = "테스트: 문화외교형 결과 조회", description = "문화외교형 시민 외교사 유형의 상세 정보를 조회합니다.")
	public ResponseEntity<CitizenTestResultResponse> getCulturalDiplomacyType() {
		try {
			CitizenTestResultResponse response = citizenTestService.getTestResult("CULTURAL_DIPLOMACY");
			return ResponseEntity.ok(response);
		} catch (Exception e) {
			return ResponseEntity.badRequest().body(
				CitizenTestResultResponse.builder()
					.message("문화외교형 결과 조회 실패: " + e.getMessage())
					.build()
			);
		}
	}

	@GetMapping("/test/economic-trade")
	@Operation(summary = "테스트: 경제통상형 결과 조회", description = "경제통상형 시민 외교사 유형의 상세 정보를 조회합니다.")
	public ResponseEntity<CitizenTestResultResponse> getEconomicTradeType() {
		try {
			CitizenTestResultResponse response = citizenTestService.getTestResult("ECONOMIC_TRADE");
			return ResponseEntity.ok(response);
		} catch (Exception e) {
			return ResponseEntity.badRequest().body(
				CitizenTestResultResponse.builder()
					.message("경제통상형 결과 조회 실패: " + e.getMessage())
					.build()
			);
		}
	}

	@GetMapping("/test/digital-communication")
	@Operation(summary = "테스트: 디지털소통형 결과 조회", description = "디지털소통형 시민 외교사 유형의 상세 정보를 조회합니다.")
	public ResponseEntity<CitizenTestResultResponse> getDigitalCommunicationType() {
		try {
			CitizenTestResultResponse response = citizenTestService.getTestResult("DIGITAL_COMMUNICATION");
			return ResponseEntity.ok(response);
		} catch (Exception e) {
			return ResponseEntity.badRequest().body(
				CitizenTestResultResponse.builder()
					.message("디지털소통형 결과 조회 실패: " + e.getMessage())
					.build()
			);
		}
	}

	@GetMapping("/test/all-types")
	@Operation(summary = "테스트: 모든 유형 결과 조회", description = "모든 시민 외교사 유형의 상세 정보를 조회합니다.")
	public ResponseEntity<?> getAllTypes() {
		try {
			java.util.Map<String, CitizenTestResultResponse> allTypes = new java.util.HashMap<>();

			allTypes.put("기후행동형", citizenTestService.getTestResult("CLIMATE_ACTION"));
			allTypes.put("평화중재형", citizenTestService.getTestResult("PEACE_MEDIATION"));
			allTypes.put("문화외교형", citizenTestService.getTestResult("CULTURAL_DIPLOMACY"));
			allTypes.put("경제통상형", citizenTestService.getTestResult("ECONOMIC_TRADE"));
			allTypes.put("디지털소통형", citizenTestService.getTestResult("DIGITAL_COMMUNICATION"));

			return ResponseEntity.ok(allTypes);
		} catch (Exception e) {
			return ResponseEntity.badRequest().body("모든 유형 조회 실패: " + e.getMessage());
		}
	}
}