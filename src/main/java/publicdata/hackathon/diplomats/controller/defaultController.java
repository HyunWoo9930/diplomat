package publicdata.hackathon.diplomats.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/v1/")
@Tag(name = "📱 메인", description = "기본 API")
public class defaultController {

	@GetMapping("default")
	@Operation(summary = "기본 테스트 API", description = "서버 연결 상태를 확인하는 기본 API입니다.")
	public String defaultAPI() {
		return "Hello World!!";
	}
}
