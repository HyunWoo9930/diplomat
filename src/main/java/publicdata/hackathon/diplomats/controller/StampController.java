package publicdata.hackathon.diplomats.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import publicdata.hackathon.diplomats.domain.dto.response.MyPageResponse;
import publicdata.hackathon.diplomats.domain.dto.response.UserLevelResponse;
import publicdata.hackathon.diplomats.domain.entity.User;
import publicdata.hackathon.diplomats.jwt.CustomUserDetails;
import publicdata.hackathon.diplomats.repository.UserRepository;
import publicdata.hackathon.diplomats.service.StampService;

@RestController
@RequestMapping("/api/v1/stamp")
@RequiredArgsConstructor
@Tag(name = "👤 마이페이지", description = "스탬프 및 레벨 관리 API")
public class StampController {

    private final StampService stampService;
    private final UserRepository userRepository;

    @GetMapping("/my-level")
    @Operation(summary = "내 레벨 및 스탬프 정보 조회", 
               description = "현재 사용자의 레벨, 스탬프 개수, 최근 스탬프 내역, 레벨업 히스토리를 조회합니다.")
    public ResponseEntity<UserLevelResponse> getMyLevelInfo(Authentication authentication) {
        try {
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            User user = userRepository.findByUserId(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

            UserLevelResponse response = stampService.getUserLevelInfo(user);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/my-level/detailed")
    @Operation(summary = "내 레벨 및 스탬프 정보 상세 조회", 
               description = "사용자의 상세 스탬프 통계(실천일지/좋아요/투표별 개수)와 일자별 히스토리를 조회합니다.")
    public ResponseEntity<UserLevelResponse> getMyLevelInfoDetailed(Authentication authentication) {
        try {
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            User user = userRepository.findByUserId(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

            UserLevelResponse response = stampService.getUserLevelInfoDetailed(user);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/my-page")
    @Operation(summary = "마이페이지 전체 정보 조회", 
               description = "사용자 ID, 마스킹된 비밀번호, 외교 레벨, 스탬프 개수, 시민력 테스트 결과를 조회합니다.")
    public ResponseEntity<MyPageResponse> getMyPageInfo(Authentication authentication) {
        try {
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            User user = userRepository.findByUserId(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

            MyPageResponse response = stampService.getMyPageInfo(user);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/level-guide")
    @Operation(summary = "레벨 시스템 가이드", 
               description = "레벨 시스템의 전체 구조와 각 레벨별 필요 스탬프 수를 안내합니다.")
    public ResponseEntity<?> getLevelGuide() {
        return ResponseEntity.ok(java.util.Map.of(
            "message", "시민외교 레벨 시스템",
            "description", "다양한 활동을 통해 스탬프를 모아 레벨을 올려보세요!",
            "levels", java.util.List.of(
                java.util.Map.of("level", 1, "name", "Lv.1 시민외교 새싹", "requiredStamps", "0~9개"),
                java.util.Map.of("level", 2, "name", "Lv.2 시민외교 꿈나무", "requiredStamps", "10~19개"),
                java.util.Map.of("level", 3, "name", "Lv.3 시민외교 리더", "requiredStamps", "20~29개"),
                java.util.Map.of("level", 4, "name", "Lv.4 시민외교 전문가", "requiredStamps", "30~39개"),
                java.util.Map.of("level", 5, "name", "Lv.5 시민외교 대장", "requiredStamps", "40개 이상")
            ),
            "stampEarningMethods", java.util.List.of(
                java.util.Map.of("activity", "실천일기 작성", "stamps", 1, "description", "외교 실천 일지를 작성하면 스탬프 1개"),
                java.util.Map.of("activity", "실천일기 좋아요 받기", "stamps", 1, "description", "다른 사용자가 내 일지에 좋아요를 누르면 스탬프 1개"),
                java.util.Map.of("activity", "투표 참여", "stamps", 1, "description", "월별 투표나 ODA 투표에 참여하면 스탬프 1개")
            )
        ));
    }
}
