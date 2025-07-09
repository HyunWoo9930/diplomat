package publicdata.hackathon.diplomats.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import publicdata.hackathon.diplomats.domain.dto.response.MainPageResponse;
import publicdata.hackathon.diplomats.service.MainPageService;

@RestController
@RequestMapping("/api/v1/main")
@RequiredArgsConstructor
@Tag(name = "📱 메인", description = "메인페이지 관련 API")
public class MainPageController {

    private final MainPageService mainPageService;

    @GetMapping
    @Operation(summary = "메인페이지 전체 조회", 
               description = "메인페이지에 필요한 모든 데이터를 조회합니다. 외교일지 최신 3개, 외교뉴스 최신 3개, 커뮤니티 인기글 3개를 반환합니다.")
    public ResponseEntity<MainPageResponse> getMainPageData() {
        try {
            MainPageResponse response = mainPageService.getMainPageData();
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
