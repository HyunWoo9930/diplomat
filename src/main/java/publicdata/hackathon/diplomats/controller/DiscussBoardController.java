package publicdata.hackathon.diplomats.controller;

import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import publicdata.hackathon.diplomats.domain.dto.request.CommentUpdateRequest;
import publicdata.hackathon.diplomats.domain.dto.request.DiscussBoardUpdateRequest;
import publicdata.hackathon.diplomats.domain.dto.request.DiscussCommentRequest;
import publicdata.hackathon.diplomats.domain.enums.DiscussType;
import publicdata.hackathon.diplomats.jwt.CustomUserDetails;
import publicdata.hackathon.diplomats.service.DiscussBoardCommentService;
import publicdata.hackathon.diplomats.service.DiscussBoardService;

@RestController
@RequestMapping("/api/v1/discuss-board")
@RequiredArgsConstructor
@Tag(name = "토론게시판", description = "토론게시판 관련 API")
public class DiscussBoardController {
	
	private final DiscussBoardService discussBoardService;
	private final DiscussBoardCommentService discussBoardCommentService;

	@PostMapping(value = "/", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	@Operation(summary = "토론게시글 생성", description = "새로운 토론게시글을 생성합니다.")
	public ResponseEntity<String> createDiscussBoard(Authentication authentication,
		@RequestParam("title") String title,
		@RequestParam("content") String content,
		@RequestParam("discussType") DiscussType discussType,
		@RequestPart(value = "images", required = false) List<MultipartFile> images) {

		try {
			// 이미지 개수 검증
			if (images != null && images.size() > 3) {
				return ResponseEntity.badRequest().body("이미지는 최대 3장까지 업로드 가능합니다.");
			}

			CustomUserDetails customUserDetails = (CustomUserDetails)authentication.getPrincipal();
			discussBoardService.createDiscussBoard(customUserDetails.getUsername(), title, content, discussType, images);
			return ResponseEntity.ok("토론게시글이 성공적으로 생성되었습니다.");
		} catch (RuntimeException e) {
			return ResponseEntity.badRequest().body(e.getMessage());
		}
	}

	@GetMapping("/")
	@Operation(summary = "토론게시글 목록 조회", description = "토론게시글 목록을 페이징하여 조회합니다.")
	public ResponseEntity<?> getDiscussBoards(Authentication authentication,
		@RequestParam(defaultValue = "0") int page,
		@RequestParam(defaultValue = "10") int size,
		@RequestParam(defaultValue = "latest") String sortBy) {
		CustomUserDetails customUserDetails = (CustomUserDetails)authentication.getPrincipal();
		Pageable pageable = PageRequest.of(page, size);
		return ResponseEntity.ok(discussBoardService.getDiscussBoards(customUserDetails.getUsername(), pageable, sortBy));
	}

	@GetMapping("/{id}")
	@Operation(summary = "토론게시글 상세 조회", description = "특정 토론게시글의 상세 내용을 조회합니다.")
	public ResponseEntity<?> getDiscussBoard(Authentication authentication, @PathVariable Long id) {
		CustomUserDetails customUserDetails = (CustomUserDetails)authentication.getPrincipal();
		return ResponseEntity.ok(discussBoardService.getDiscussBoardDetails(customUserDetails.getUsername(), id));
	}

	@PostMapping("/{id}/comment")
	@Operation(summary = "토론게시글 댓글 작성", description = "토론게시글에 댓글을 작성합니다.")
	public ResponseEntity<?> commentDiscussBoard(Authentication authentication, @PathVariable Long id,
		@RequestBody DiscussCommentRequest commentRequest) {
		CustomUserDetails customUserDetails = (CustomUserDetails)authentication.getPrincipal();
		discussBoardCommentService.commentDiscussBoard(customUserDetails.getUsername(), id, commentRequest);
		return ResponseEntity.ok("댓글이 성공적으로 작성되었습니다.");
	}

	// 게시글 수정
	@PutMapping("/{id}")
	@Operation(summary = "토론게시글 수정", description = "토론게시글을 수정합니다.")
	public ResponseEntity<String> updateDiscussBoard(Authentication authentication, @PathVariable Long id,
		@RequestBody DiscussBoardUpdateRequest request) {
		try {
			CustomUserDetails customUserDetails = (CustomUserDetails)authentication.getPrincipal();
			discussBoardService.updateDiscussBoard(customUserDetails.getUsername(), id, request);
			return ResponseEntity.ok("게시글이 성공적으로 수정되었습니다.");
		} catch (RuntimeException e) {
			return ResponseEntity.badRequest().body(e.getMessage());
		}
	}

	// 게시글 삭제
	@DeleteMapping("/{id}")
	@Operation(summary = "토론게시글 삭제", description = "토론게시글을 삭제합니다.")
	public ResponseEntity<String> deleteDiscussBoard(Authentication authentication, @PathVariable Long id) {
		try {
			CustomUserDetails customUserDetails = (CustomUserDetails)authentication.getPrincipal();
			discussBoardService.deleteDiscussBoard(customUserDetails.getUsername(), id);
			return ResponseEntity.ok("게시글이 성공적으로 삭제되었습니다.");
		} catch (RuntimeException e) {
			return ResponseEntity.badRequest().body(e.getMessage());
		}
	}

	// 댓글 수정
	@PutMapping("/comment/{commentId}")
	@Operation(summary = "토론게시글 댓글 수정", description = "토론게시글 댓글을 수정합니다.")
	public ResponseEntity<String> updateComment(Authentication authentication, @PathVariable Long commentId,
		@RequestBody CommentUpdateRequest request) {
		try {
			CustomUserDetails customUserDetails = (CustomUserDetails)authentication.getPrincipal();
			discussBoardCommentService.updateComment(customUserDetails.getUsername(), commentId, request);
			return ResponseEntity.ok("댓글이 성공적으로 수정되었습니다.");
		} catch (RuntimeException e) {
			return ResponseEntity.badRequest().body(e.getMessage());
		}
	}

	// 댓글 삭제
	@DeleteMapping("/comment/{commentId}")
	@Operation(summary = "토론게시글 댓글 삭제", description = "토론게시글 댓글을 삭제합니다.")
	public ResponseEntity<String> deleteComment(Authentication authentication, @PathVariable Long commentId) {
		try {
			CustomUserDetails customUserDetails = (CustomUserDetails)authentication.getPrincipal();
			discussBoardCommentService.deleteComment(customUserDetails.getUsername(), commentId);
			return ResponseEntity.ok("댓글이 성공적으로 삭제되었습니다.");
		} catch (RuntimeException e) {
			return ResponseEntity.badRequest().body(e.getMessage());
		}
	}
}
