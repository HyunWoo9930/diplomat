package publicdata.hackathon.diplomats.controller;

import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CrossOrigin;
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
import publicdata.hackathon.diplomats.domain.dto.request.CommentRequest;
import publicdata.hackathon.diplomats.domain.dto.request.CommentUpdateRequest;
import publicdata.hackathon.diplomats.domain.dto.request.FreeBoardUpdateRequest;
import publicdata.hackathon.diplomats.domain.dto.response.ApiResponse;
import publicdata.hackathon.diplomats.domain.dto.response.CreatePostResponse;
import publicdata.hackathon.diplomats.jwt.CustomUserDetails;
import publicdata.hackathon.diplomats.service.FreeBoardCommentService;
import publicdata.hackathon.diplomats.service.FreeBoardService;

@RestController
@RequestMapping("/api/v1/free-board")
@RequiredArgsConstructor
@Tag(name = "💬 커뮤니티", description = "자유게시판 관련 API")
@CrossOrigin(origins = "*")
public class FreeBoardController {

	private final FreeBoardService freeBoardService;
	private final FreeBoardCommentService freeBoardCommentService;

	@PostMapping(value = "/", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	@Operation(summary = "자유게시판 글 작성", description = "자유게시판에 새 글을 작성합니다. 이미지는 최대 3장까지 업로드 가능합니다.")
	public ResponseEntity<ApiResponse<CreatePostResponse>> createFreeBoard(Authentication authentication,
		@RequestParam("title") String title,
		@RequestParam("content") String content,
		@RequestPart(value = "images", required = false) List<MultipartFile> images) {

		try {
			// 이미지 개수 검증
			if (images != null && images.size() > 3) {
				return ResponseEntity.badRequest().body(
					ApiResponse.error("이미지는 최대 3장까지 업로드 가능합니다.", null)
				);
			}

			CustomUserDetails customUserDetails = (CustomUserDetails)authentication.getPrincipal();
			Long freeBoardId = freeBoardService.createFreeBoard(customUserDetails.getUsername(), title, content, images);
			CreatePostResponse response = CreatePostResponse.of(freeBoardId, "게시글이 성공적으로 생성되었습니다.");
			
			return ResponseEntity.ok(ApiResponse.success("게시글이 성공적으로 생성되었습니다.", response));
		} catch (RuntimeException e) {
			return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage(), null));
		}
	}

	@GetMapping("/")
	@Operation(summary = "자유게시판 글 목록 조회", description = "자유게시판 글 목록을 페이징하여 조회합니다.")
	public ResponseEntity<ApiResponse<?>> getFreeBoards(Authentication authentication,
		@RequestParam(defaultValue = "0") int page,
		@RequestParam(defaultValue = "10") int size,
		@RequestParam(defaultValue = "latest") String sortBy) {
		CustomUserDetails customUserDetails = (CustomUserDetails)authentication.getPrincipal();
		Pageable pageable = PageRequest.of(page, size);
		return ResponseEntity.ok(
			ApiResponse.success("자유게시판 목록 조회 성공", 
				freeBoardService.getFreeBoards(customUserDetails.getUsername(), pageable, sortBy))
		);
	}

	@GetMapping("/{id}")
	@Operation(summary = "자유게시판 글 상세 조회", description = "특정 자유게시판 글의 상세 내용을 조회합니다.")
	public ResponseEntity<ApiResponse<?>> getFreeBoard(Authentication authentication, @PathVariable Long id) {
		CustomUserDetails customUserDetails = (CustomUserDetails)authentication.getPrincipal();
		return ResponseEntity.ok(
			ApiResponse.success("자유게시판 상세 조회 성공",
				freeBoardService.getFreeBoardDetails(customUserDetails.getUsername(), id))
		);
	}

	@PostMapping("/{id}/comment")
	public ResponseEntity<?> commentFreeBoard(Authentication authentication, @PathVariable Long id,
		@RequestBody CommentRequest commentRequest) {
		CustomUserDetails customUserDetails = (CustomUserDetails)authentication.getPrincipal();
		freeBoardCommentService.commentFreeBoard(customUserDetails.getUsername(), id, commentRequest);
		return ResponseEntity.ok("success");
	}

	// 게시글 수정
	@PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	@Operation(summary = "자유게시판 글 수정", description = "자유게시판 글을 수정합니다. 이미지는 최대 3장까지 업로드 가능합니다.")
	public ResponseEntity<ApiResponse<String>> updateFreeBoard(Authentication authentication, @PathVariable Long id,
		@RequestParam("title") String title,
		@RequestParam("content") String content,
		@RequestPart(value = "images", required = false) List<MultipartFile> images) {
		try {
			// 이미지 개수 검증
			if (images != null && images.size() > 3) {
				return ResponseEntity.badRequest().body(
					ApiResponse.error("이미지는 최대 3장까지 업로드 가능합니다.", null)
				);
			}

			CustomUserDetails customUserDetails = (CustomUserDetails)authentication.getPrincipal();
			freeBoardService.updateFreeBoard(customUserDetails.getUsername(), id, title, content, images);
			return ResponseEntity.ok(ApiResponse.success("게시글이 성공적으로 수정되었습니다."));
		} catch (RuntimeException e) {
			return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage(), null));
		}
	}

	// 게시글 삭제
	@DeleteMapping("/{id}")
	public ResponseEntity<String> deleteFreeBoard(Authentication authentication, @PathVariable Long id) {
		try {
			CustomUserDetails customUserDetails = (CustomUserDetails)authentication.getPrincipal();
			freeBoardService.deleteFreeBoard(customUserDetails.getUsername(), id);
			return ResponseEntity.ok("게시글이 성공적으로 삭제되었습니다.");
		} catch (RuntimeException e) {
			return ResponseEntity.badRequest().body(e.getMessage());
		}
	}

	// 댓글 수정
	@PutMapping("/comment/{commentId}")
	public ResponseEntity<String> updateComment(Authentication authentication, @PathVariable Long commentId,
		@RequestBody CommentUpdateRequest request) {
		try {
			CustomUserDetails customUserDetails = (CustomUserDetails)authentication.getPrincipal();
			freeBoardCommentService.updateComment(customUserDetails.getUsername(), commentId, request);
			return ResponseEntity.ok("댓글이 성공적으로 수정되었습니다.");
		} catch (RuntimeException e) {
			return ResponseEntity.badRequest().body(e.getMessage());
		}
	}

	// 댓글 삭제
	@DeleteMapping("/comment/{commentId}")
	public ResponseEntity<String> deleteComment(Authentication authentication, @PathVariable Long commentId) {
		try {
			CustomUserDetails customUserDetails = (CustomUserDetails)authentication.getPrincipal();
			freeBoardCommentService.deleteComment(customUserDetails.getUsername(), commentId);
			return ResponseEntity.ok("댓글이 성공적으로 삭제되었습니다.");
		} catch (RuntimeException e) {
			return ResponseEntity.badRequest().body(e.getMessage());
		}
	}
}
