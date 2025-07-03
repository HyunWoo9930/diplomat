package publicdata.hackathon.diplomats.controller;

import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import lombok.RequiredArgsConstructor;
import publicdata.hackathon.diplomats.domain.enums.DiscussType;
import publicdata.hackathon.diplomats.jwt.CustomUserDetails;
import publicdata.hackathon.diplomats.service.DiscussBoardService;

@RestController
@RequestMapping("/api/v1/discuss-board")
@RequiredArgsConstructor
public class DiscussBoardController {
	private final DiscussBoardService discussBoardService;

	@PostMapping(value = "/", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<?> createDiscussBoard(Authentication authentication,
		@RequestParam("title") String title,
		@RequestParam("content") String content,
		@RequestParam("discussType") DiscussType discussType,
		@RequestPart(value = "images", required = false) List<MultipartFile> images) {

		CustomUserDetails customUserDetails = (CustomUserDetails)authentication.getPrincipal();
		discussBoardService.createDiscussBoard(customUserDetails.getUsername(), title, content, discussType, images);
		return ResponseEntity.ok().build();
	}
}
