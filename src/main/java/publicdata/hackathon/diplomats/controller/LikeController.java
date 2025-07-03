package publicdata.hackathon.diplomats.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import publicdata.hackathon.diplomats.domain.dto.request.LikeRequest;
import publicdata.hackathon.diplomats.domain.dto.response.LikeResponse;
import publicdata.hackathon.diplomats.jwt.CustomUserDetails;
import publicdata.hackathon.diplomats.service.LikeService;

@RestController
@RequestMapping("/api/v1/like")
@RequiredArgsConstructor
@Tag(name = "좋아요", description = "범용 좋아요 API")
public class LikeController {
    
    private final LikeService likeService;
    
    @PostMapping("/toggle")
    @Operation(summary = "좋아요 토글", description = "좋아요를 누르거나 취소합니다. (자신의 게시글에는 좋아요 불가)")
    public ResponseEntity<LikeResponse> toggleLike(
        Authentication authentication,
        @RequestBody LikeRequest request) {
        
        try {
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            LikeResponse response = likeService.toggleLike(
                userDetails.getUsername(), 
                request.getTargetType(), 
                request.getTargetId()
            );
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(
                LikeResponse.builder()
                    .isLiked(false)
                    .likeCount(0)
                    .message(e.getMessage())
                    .build()
            );
        }
    }
    
    @GetMapping("/status")
    @Operation(summary = "좋아요 상태 조회", description = "특정 게시글의 좋아요 상태와 개수를 조회합니다.")
    public ResponseEntity<LikeResponse> getLikeStatus(
        Authentication authentication,
        @RequestParam String targetType,
        @RequestParam Long targetId) {
        
        try {
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            LikeResponse response = likeService.getLikeStatus(
                userDetails.getUsername(), 
                targetType, 
                targetId
            );
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(
                LikeResponse.builder()
                    .isLiked(false)
                    .likeCount(0)
                    .message(e.getMessage())
                    .build()
            );
        }
    }
}
