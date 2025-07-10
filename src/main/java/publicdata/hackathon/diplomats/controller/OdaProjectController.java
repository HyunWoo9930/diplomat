package publicdata.hackathon.diplomats.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import publicdata.hackathon.diplomats.service.OdaProjectService;

@RestController
@RequestMapping("/api/v1/oda-project")
@RequiredArgsConstructor
@Tag(name = "🌍 ODA/공공외교", description = "ODA 프로젝트 관리 API")
@CrossOrigin(origins = "*")
public class OdaProjectController {

	private final OdaProjectService odaProjectService;

	@PostMapping("/update")
	@Operation(summary = "ODA 프로젝트 데이터 업데이트", 
			   description = "외교부 공공데이터 API에서 ODA 프로젝트 정보를 가져와서 분야별로 분류하고 저장합니다. " +
			   				 "각 분야(환경, 교육, 보건, 여성, 기타)별로 상위 5개씩 유지됩니다.")
	public ResponseEntity<String> updateOdaProjects() {
		try {
			odaProjectService.fetchAndProcessOdaProjects();
			return ResponseEntity.ok("ODA 프로젝트 업데이트 완료");
		} catch (Exception e) {
			return ResponseEntity.badRequest().body("업데이트 실패: " + e.getMessage());
		}
	}

	@GetMapping("/status")
	@Operation(summary = "ODA 프로젝트 현황 조회", 
			   description = "현재 저장된 ODA 프로젝트의 전체 개수와 분야별 분포 현황을 조회합니다. " +
			   				 "투표 시스템의 기초 데이터 상태를 확인할 수 있습니다.")
	public ResponseEntity<Map<String, Object>> getOdaProjectStatus() {
		try {
			Map<String, Object> status = odaProjectService.getOdaProjectStatus();
			return ResponseEntity.ok(status);
		} catch (Exception e) {
			return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
		}
	}
}
