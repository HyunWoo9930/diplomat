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
import publicdata.hackathon.diplomats.domain.dto.request.CommentRequest;
import publicdata.hackathon.diplomats.domain.dto.request.CommentUpdateRequest;
import publicdata.hackathon.diplomats.jwt.CustomUserDetails;
import publicdata.hackathon.diplomats.service.DiaryCommentService;
import publicdata.hackathon.diplomats.service.DiaryService;

@RestController
@RequestMapping("/api/v1/diary")
@RequiredArgsConstructor
@Tag(name = "💬 커뮤니티", description = "외교실천일지 관련 API")
public class DiaryController {
	private final DiaryService diaryService;
	private final DiaryCommentService diaryCommentService;

	@PostMapping(value = "/", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	@Operation(summary = "외교일지 생성", description = "새로운 외교일지를 생성합니다.")
	public ResponseEntity<String> createDiary(Authentication authentication,
		@RequestParam("title") String title,
		@RequestParam("content") String content,
		@RequestParam("실천항목") String action,
		@RequestPart(value = "images", required = false) List<MultipartFile> images) {

		try {
			if (images != null && images.size() > 3) {
				return ResponseEntity.badRequest().body("이미지는 최대 3장까지 업로드 가능합니다.");
			}

			CustomUserDetails customUserDetails = (CustomUserDetails)authentication.getPrincipal();
			diaryService.createDiary(customUserDetails.getUsername(), title, content, action, images);
			return ResponseEntity.ok("실천일지가 성공적으로 생성되었습니다.");
		} catch (RuntimeException e) {
			return ResponseEntity.badRequest().body(e.getMessage());
		}
	}

	@GetMapping("/")
	@Operation(summary = "외교일지 목록 조회", description = "외교일지 목록을 페이징하여 조회합니다.")
	public ResponseEntity<?> getDiaries(Authentication authentication,
		@RequestParam(defaultValue = "0") int page,
		@RequestParam(defaultValue = "10") int size,
		@RequestParam(defaultValue = "latest") String sortBy) {
		CustomUserDetails customUserDetails = (CustomUserDetails)authentication.getPrincipal();
		Pageable pageable = PageRequest.of(page, size);
		return ResponseEntity.ok(diaryService.getDiaries(customUserDetails.getUsername(), pageable, sortBy));
	}

	@GetMapping("/{id}")
	@Operation(summary = "외교일지 상세 조회", description = "특정 외교일지의 상세 내용을 조회합니다.")
	public ResponseEntity<?> getDiary(Authentication authentication, @PathVariable Long id) {
		CustomUserDetails customUserDetails = (CustomUserDetails)authentication.getPrincipal();
		return ResponseEntity.ok(diaryService.getDiaryDetails(customUserDetails.getUsername(), id));
	}

	@PostMapping("/{id}/comment")
	@Operation(summary = "외교일지 댓글 작성", description = "외교일지에 댓글을 작성합니다.")
	public ResponseEntity<?> commentDiary(Authentication authentication, @PathVariable Long id,
		@RequestBody CommentRequest commentRequest) {
		CustomUserDetails customUserDetails = (CustomUserDetails)authentication.getPrincipal();
		diaryCommentService.commentDiary(customUserDetails.getUsername(), id, commentRequest);
		return ResponseEntity.ok("댓글이 성공적으로 작성되었습니다.");
	}

	@PutMapping("/comment/{commentId}")
	@Operation(summary = "외교일지 댓글 수정", description = "외교일지 댓글을 수정합니다.")
	public ResponseEntity<String> updateComment(Authentication authentication, @PathVariable Long commentId,
		@RequestBody CommentUpdateRequest request) {
		try {
			CustomUserDetails customUserDetails = (CustomUserDetails)authentication.getPrincipal();
			diaryCommentService.updateComment(customUserDetails.getUsername(), commentId, request);
			return ResponseEntity.ok("댓글이 성공적으로 수정되었습니다.");
		} catch (RuntimeException e) {
			return ResponseEntity.badRequest().body(e.getMessage());
		}
	}

	@DeleteMapping("/comment/{commentId}")
	@Operation(summary = "외교일지 댓글 삭제", description = "외교일지 댓글을 삭제합니다.")
	public ResponseEntity<String> deleteComment(Authentication authentication, @PathVariable Long commentId) {
		try {
			CustomUserDetails customUserDetails = (CustomUserDetails)authentication.getPrincipal();
			diaryCommentService.deleteComment(customUserDetails.getUsername(), commentId);
			return ResponseEntity.ok("댓글이 성공적으로 삭제되었습니다.");
		} catch (RuntimeException e) {
			return ResponseEntity.badRequest().body(e.getMessage());
		}
	}

	@GetMapping("/top-monthly")
	@Operation(summary = "이번 달 인기 일지", description = "이번 달 인기 일지 상위 10개를 조회합니다.")
	public ResponseEntity<?> getTopMonthlyDiaries() {
		return ResponseEntity.ok(diaryService.getTopMonthlyDiaries());
	}
}
