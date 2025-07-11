package publicdata.hackathon.diplomats.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import publicdata.hackathon.diplomats.domain.dto.response.ApiResponse;
import publicdata.hackathon.diplomats.utils.SecurityUtils;

@Slf4j
@RestController
@RequestMapping("/api/v1/test")
@RequiredArgsConstructor
@Tag(name = "🧪 테스트", description = "인증 테스트용 API")
@CrossOrigin(origins = "*")
public class TestController {

    @GetMapping("/public")
    @Operation(summary = "공개 엔드포인트", description = "인증이 필요하지 않은 테스트 엔드포인트")
    public ResponseEntity<ApiResponse<String>> publicEndpoint() {
        return ResponseEntity.ok(ApiResponse.success("공개 엔드포인트에 성공적으로 접근했습니다."));
    }

    @GetMapping("/private")
    @Operation(summary = "인증 필요 엔드포인트", description = "인증이 필요한 테스트 엔드포인트")
    public ResponseEntity<ApiResponse<String>> privateEndpoint() {
        String currentUserId = SecurityUtils.getCurrentUserIdString();
        log.info("인증된 사용자: {}", currentUserId);
        
        return ResponseEntity.ok(ApiResponse.success("인증된 사용자 " + currentUserId + "님, 안녕하세요!"));
    }

    @GetMapping("/user-info")
    @Operation(summary = "사용자 정보", description = "현재 인증된 사용자의 정보를 반환")
    public ResponseEntity<ApiResponse<?>> getUserInfo() {
        var user = SecurityUtils.getCurrentUser();
        
        return ResponseEntity.ok(ApiResponse.success("사용자 정보를 조회했습니다.", Map.of(
            "id", user.getId(),
            "userId", user.getUserId(),
            "level", user.getCurrentLevel(),
            "stamps", user.getTotalStamps()
        )));
    }
}
