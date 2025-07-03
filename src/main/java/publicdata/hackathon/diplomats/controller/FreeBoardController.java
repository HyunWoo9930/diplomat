package publicdata.hackathon.diplomats.controller;

import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import lombok.RequiredArgsConstructor;
import publicdata.hackathon.diplomats.domain.dto.request.CommentRequest;
import publicdata.hackathon.diplomats.jwt.CustomUserDetails;
import publicdata.hackathon.diplomats.service.FreeBoardCommentService;
import publicdata.hackathon.diplomats.service.FreeBoardService;

@RestController
@RequestMapping("/api/v1/free-board")
@RequiredArgsConstructor
public class FreeBoardController {

	private final FreeBoardService freeBoardService;
	private final FreeBoardCommentService freeBoardCommentService;

	@PostMapping(value = "/", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<String> createFreeBoard(Authentication authentication, @RequestParam("title") String title,
		@RequestParam("content") String content,
		@RequestPart(value = "images", required = false) List<MultipartFile> images) {

		try {
			// 이미지 개수 검증
			if (images != null && images.size() > 3) {
				return ResponseEntity.badRequest().body("이미지는 최대 3장까지 업로드 가능합니다.");
			}

			CustomUserDetails customUserDetails = (CustomUserDetails)authentication.getPrincipal();
			freeBoardService.createFreeBoard(customUserDetails.getUsername(), title, content, images);
			return ResponseEntity.ok("게시글이 성공적으로 생성되었습니다.");
		} catch (RuntimeException e) {
			return ResponseEntity.badRequest().body(e.getMessage());
		}
	}

	@GetMapping("/")
	public ResponseEntity<?> getFreeBoards(Authentication authentication, int page, int size) {
		CustomUserDetails customUserDetails = (CustomUserDetails)authentication.getPrincipal();
		Pageable pageable = PageRequest.of(page, size, Sort.by("id").ascending());
		return ResponseEntity.ok(freeBoardService.getFreeBoards(customUserDetails.getUsername(), pageable));
	}

	@GetMapping("/{id}")
	public ResponseEntity<?> getFreeBoard(Authentication authentication, @PathVariable Long id) {
		CustomUserDetails customUserDetails = (CustomUserDetails)authentication.getPrincipal();
		return ResponseEntity.ok(freeBoardService.getFreeBoardDetails(customUserDetails.getUsername(), id));
	}

	@PostMapping("/{id}/comment")
	public ResponseEntity<?> commentFreeBoard(Authentication authentication, @PathVariable Long id,
		@RequestBody CommentRequest commentRequest) {
		CustomUserDetails customUserDetails = (CustomUserDetails)authentication.getPrincipal();
		freeBoardCommentService.commentFreeBoard(customUserDetails.getUsername(), id, commentRequest);
		return ResponseEntity.ok("success");
	}
}
