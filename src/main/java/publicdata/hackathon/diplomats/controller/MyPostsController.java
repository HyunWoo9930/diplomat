package publicdata.hackathon.diplomats.controller;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import publicdata.hackathon.diplomats.domain.dto.response.MyPostsResponse;
import publicdata.hackathon.diplomats.jwt.CustomUserDetails;
import publicdata.hackathon.diplomats.service.MyPostsService;

@RestController
@RequestMapping("/api/v1/my-posts")
@RequiredArgsConstructor
@Tag(name = "내 게시글", description = "사용자가 작성한 게시글 모아보기 API")
public class MyPostsController {

	private final MyPostsService myPostsService;

	@GetMapping("/")
	@Operation(summary = "내 게시글 모아보기", description = "사용자가 작성한 게시글을 필터링하여 조회합니다.")
	public ResponseEntity<MyPostsResponse> getMyPosts(
		Authentication authentication,
		@Parameter(description = "필터 (ALL: 전체, FREE: 자유게시판, DISCUSS: 토론게시판, DIARY: 외교일지)")
		@RequestParam(defaultValue = "ALL") String filter,
		@Parameter(description = "페이지 번호 (0부터 시작)")
		@RequestParam(defaultValue = "0") int page,
		@Parameter(description = "페이지 크기")
		@RequestParam(defaultValue = "10") int size) {
		
		try {
			CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
			Pageable pageable = PageRequest.of(page, size);
			
			MyPostsResponse response = myPostsService.getMyPosts(userDetails.getUsername(), filter, pageable);
			return ResponseEntity.ok(response);
		} catch (IllegalArgumentException e) {
			return ResponseEntity.badRequest().build();
		} catch (Exception e) {
			return ResponseEntity.internalServerError().build();
		}
	}
}
