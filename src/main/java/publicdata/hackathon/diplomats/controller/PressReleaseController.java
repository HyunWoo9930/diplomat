package publicdata.hackathon.diplomats.controller;

import java.util.HashMap;
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
import publicdata.hackathon.diplomats.service.PressReleaseService;

@RestController
@RequestMapping("/api/v1/admin/press-release")
@RequiredArgsConstructor
@Tag(name = "📰 뉴스/보도자료", description = "보도자료 관리 API")
@CrossOrigin(origins = "*")
public class PressReleaseController {

	private final PressReleaseService pressReleaseService;

	@PostMapping("/update")
	public ResponseEntity<String> updatePressReleases() {
		try {
			pressReleaseService.fetchAndProcessPressReleases();
			return ResponseEntity.ok("보도자료 업데이트 완료");
		} catch (Exception e) {
			return ResponseEntity.badRequest().body("업데이트 실패: " + e.getMessage());
		}
	}

	@GetMapping("/status")
	public ResponseEntity<Map<String, Object>> getStatus() {
		try {
			Map<String, Object> status = pressReleaseService.getPressReleaseStatus();
			return ResponseEntity.ok(status);
		} catch (Exception e) {
			Map<String, Object> errorResponse = new HashMap<>();
			errorResponse.put("error", e.getMessage());
			return ResponseEntity.badRequest().body(errorResponse);
		}
	}
}