package publicdata.hackathon.diplomats.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import publicdata.hackathon.diplomats.domain.dto.response.RecommendedDiplomacyProgram;
import publicdata.hackathon.diplomats.service.PublicDiplomacyService;

@RestController
@RequestMapping("/api/v1/admin/public-diplomacy")
@RequiredArgsConstructor
@Tag(name = "🌍 ODA/공공외교", description = "공공외교 프로그램 관리 API")
public class PublicDiplomacyController {

	private final PublicDiplomacyService publicDiplomacyService;

	@PostMapping("/update")
	@Operation(summary = "공공외교 프로그램 수동 업데이트", description = "한국국제교류재단 API에서 공공외교 프로그램을 가져와 업데이트합니다.")
	public ResponseEntity<String> updatePrograms() {
		try {
			publicDiplomacyService.fetchAndProcessPrograms();
			return ResponseEntity.ok("공공외교 프로그램 업데이트 완료");
		} catch (Exception e) {
			return ResponseEntity.badRequest().body("업데이트 실패: " + e.getMessage());
		}
	}

	@GetMapping("/status")
	@Operation(summary = "공공외교 프로그램 현황 조회", description = "각 유형별 프로그램 개수와 상태를 조회합니다.")
	public ResponseEntity<Map<String, Object>> getStatus() {
		try {
			Map<String, Object> status = publicDiplomacyService.getProgramStatus();
			return ResponseEntity.ok(status);
		} catch (Exception e) {
			Map<String, Object> errorResponse = new HashMap<>();
			errorResponse.put("error", e.getMessage());
			return ResponseEntity.badRequest().body(errorResponse);
		}
	}

	@GetMapping("/examples")
	@Operation(summary = "유형별 프로그램 예시 조회", description = "각 유형별 상위 3개 프로그램 제목을 조회합니다.")
	public ResponseEntity<Map<String, List<String>>> getExamples() {
		try {
			Map<String, List<String>> examples = publicDiplomacyService.getTypeExamples();
			return ResponseEntity.ok(examples);
		} catch (Exception e) {
			return ResponseEntity.badRequest().body(Map.of("error", List.of(e.getMessage())));
		}
	}

	@GetMapping("/type/{type}")
	@Operation(summary = "특정 유형의 프로그램 조회", description = "특정 시민력 유형에 맞는 공공외교 프로그램을 조회합니다.")
	public ResponseEntity<List<RecommendedDiplomacyProgram>> getByType(@PathVariable String type) {
		try {
			List<RecommendedDiplomacyProgram> programs = publicDiplomacyService.getRecommendedPrograms(type);
			return ResponseEntity.ok(programs);
		} catch (Exception e) {
			return ResponseEntity.badRequest().body(List.of());
		}
	}
}