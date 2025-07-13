package publicdata.hackathon.diplomats.controller;

import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import publicdata.hackathon.diplomats.domain.dto.request.CommentRequest;
import publicdata.hackathon.diplomats.domain.dto.request.CommentUpdateRequest;
import publicdata.hackathon.diplomats.domain.dto.request.DiaryRequest;
import publicdata.hackathon.diplomats.domain.dto.response.ApiResponse;
import publicdata.hackathon.diplomats.domain.dto.response.CreatePostResponse;
import publicdata.hackathon.diplomats.domain.dto.response.MonthlyVoteResultResponse;
import publicdata.hackathon.diplomats.service.DiaryCommentService;
import publicdata.hackathon.diplomats.service.DiaryService;
import publicdata.hackathon.diplomats.service.MonthlyVoteResultService;
import publicdata.hackathon.diplomats.utils.SecurityUtils;

@Slf4j
@RestController
@RequestMapping("/api/v1/diary")
@RequiredArgsConstructor
@Tag(name = "💬 커뮤니티", description = "외교실천일지 관련 API")
@CrossOrigin(origins = "*")
public class DiaryController {
	private final DiaryService diaryService;
	private final DiaryCommentService diaryCommentService;
	private final MonthlyVoteResultService monthlyVoteResultService;

	@PostMapping(value = "/", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	@Operation(summary = "외교일지 생성", description = "새로운 외교일지를 생성합니다.")
	public ResponseEntity<ApiResponse<CreatePostResponse>> createDiary(
		@RequestParam("title") String title,
		@RequestParam("content") String content,
		@RequestParam("실천항목") String action,
		@RequestPart(value = "images", required = false) List<MultipartFile> images) {

		String currentUserId = SecurityUtils.getCurrentUserIdString();
		log.info("외교일지 생성 요청: userId={}, title={}", currentUserId, title);
		
		Long diaryId = diaryService.createDiary(currentUserId, title, content, action, images);
		CreatePostResponse response = CreatePostResponse.of(diaryId, "실천일지가 성공적으로 생성되었습니다.");
		
		return ResponseEntity.ok(ApiResponse.success("실천일지가 성공적으로 생성되었습니다.", response));
	}

	@GetMapping("/")
	@Operation(summary = "외교일지 목록 조회", description = "외교일지 목록을 페이징하여 조회합니다.")
	public ResponseEntity<ApiResponse<?>> getDiaries(
		@RequestParam(defaultValue = "0") int page,
		@RequestParam(defaultValue = "10") int size,
		@RequestParam(defaultValue = "latest") String sortBy) {
		
		String currentUserId = SecurityUtils.getCurrentUserIdString();
		log.info("외교일지 목록 조회: userId={}, page={}, size={}, sortBy={}", currentUserId, page, size, sortBy);
		
		Pageable pageable = PageRequest.of(page, size);
		var diaries = diaryService.getDiaries(currentUserId, pageable, sortBy);
		
		return ResponseEntity.ok(ApiResponse.success("외교일지 목록을 조회했습니다.", diaries));
	}

	@GetMapping("/{id}")
	@Operation(summary = "외교일지 상세 조회", description = "특정 외교일지의 상세 내용을 조회합니다.")
	public ResponseEntity<ApiResponse<?>> getDiary(@PathVariable Long id) {
		String currentUserId = SecurityUtils.getCurrentUserIdString();
		log.info("외교일지 상세 조회: userId={}, diaryId={}", currentUserId, id);
		
		var diary = diaryService.getDiaryDetails(currentUserId, id);
		return ResponseEntity.ok(ApiResponse.success("외교일지 상세 정보를 조회했습니다.", diary));
	}

	@PostMapping("/{id}/comment")
	@Operation(summary = "외교일지 댓글 작성", description = "외교일지에 댓글을 작성합니다.")
	public ResponseEntity<ApiResponse<String>> commentDiary(@PathVariable Long id,
		@Valid @RequestBody CommentRequest commentRequest) {
		
		String currentUserId = SecurityUtils.getCurrentUserIdString();
		log.info("외교일지 댓글 작성: userId={}, diaryId={}", currentUserId, id);
		
		diaryCommentService.commentDiary(currentUserId, id, commentRequest);
		return ResponseEntity.ok(ApiResponse.success("댓글이 성공적으로 작성되었습니다."));
	}

	@PutMapping("/comment/{commentId}")
	@Operation(summary = "외교일지 댓글 수정", description = "외교일지 댓글을 수정합니다.")
	public ResponseEntity<ApiResponse<String>> updateComment(@PathVariable Long commentId,
		@Valid @RequestBody CommentUpdateRequest request) {
		
		String currentUserId = SecurityUtils.getCurrentUserIdString();
		log.info("외교일지 댓글 수정: userId={}, commentId={}", currentUserId, commentId);
		
		diaryCommentService.updateComment(currentUserId, commentId, request);
		return ResponseEntity.ok(ApiResponse.success("댓글이 성공적으로 수정되었습니다."));
	}

	@DeleteMapping("/comment/{commentId}")
	@Operation(summary = "외교일지 댓글 삭제", description = "외교일지 댓글을 삭제합니다.")
	public ResponseEntity<ApiResponse<String>> deleteComment(@PathVariable Long commentId) {
		String currentUserId = SecurityUtils.getCurrentUserIdString();
		log.info("외교일지 댓글 삭제: userId={}, commentId={}", currentUserId, commentId);
		
		diaryCommentService.deleteComment(currentUserId, commentId);
		return ResponseEntity.ok(ApiResponse.success("댓글이 성공적으로 삭제되었습니다."));
	}

	@GetMapping("/top-monthly")
	@Operation(summary = "이번 달 인기 일지", description = "이번 달 인기 일지 상위 10개를 조회합니다.")
	public ResponseEntity<ApiResponse<?>> getTopMonthlyDiaries() {
		log.info("이번 달 인기 일지 조회 요청");
		
		var topDiaries = diaryService.getTopMonthlyDiaries();
		return ResponseEntity.ok(ApiResponse.success("이번 달 인기 일지를 조회했습니다.", topDiaries));
	}

	@PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	@Operation(summary = "외교일지 수정", description = "외교일지를 수정합니다. 이미지는 최대 5장까지 업로드 가능합니다.")
	public ResponseEntity<ApiResponse<String>> updateDiary(@PathVariable Long id,
		@RequestParam("title") String title,
		@RequestParam("content") String content,
		@RequestParam("action") String action,
		@RequestPart(value = "images", required = false) List<MultipartFile> images) {
		
		try {
			// 이미지 개수 검증
			if (images != null && images.size() > 5) {
				return ResponseEntity.badRequest().body(
					ApiResponse.error("이미지는 최대 5장까지 업로드 가능합니다.", null)
				);
			}

			String currentUserId = SecurityUtils.getCurrentUserIdString();
			log.info("외교일지 수정: userId={}, diaryId={}", currentUserId, id);
			
			diaryService.updateDiary(currentUserId, id, title, content, action, images);
			return ResponseEntity.ok(ApiResponse.success("일지가 성공적으로 수정되었습니다."));
		} catch (RuntimeException e) {
			return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage(), null));
		}
	}

	@DeleteMapping("/{id}")
	@Operation(summary = "외교일지 삭제", description = "외교일지를 삭제합니다.")
	public ResponseEntity<ApiResponse<String>> deleteDiary(@PathVariable Long id) {
		String currentUserId = SecurityUtils.getCurrentUserIdString();
		log.info("외교일지 삭제: userId={}, diaryId={}", currentUserId, id);
		
		diaryService.deleteDiary(currentUserId, id);
		return ResponseEntity.ok(ApiResponse.success("일지가 성공적으로 삭제되었습니다."));
	}

	@GetMapping("/monthly-result/{year}/{month}")
	@Operation(
		summary = "특정 월 통합 투표 결과 조회", 
		description = "지정된 년도와 월의 일지 투표 결과, ODA 투표 결과, 인기 일지 목록을 통합하여 조회합니다.",
		parameters = {
			@io.swagger.v3.oas.annotations.Parameter(name = "year", description = "조회할 년도 (예: 2025)", example = "2025"),
			@io.swagger.v3.oas.annotations.Parameter(name = "month", description = "조회할 월 (1-12)", example = "7")
		}
	)
	public ResponseEntity<ApiResponse<MonthlyVoteResultResponse>> getMonthlyVoteResult(
		@PathVariable Integer year,
		@PathVariable Integer month) {
		
		log.info("특정 월 통합 투표 결과 조회 요청: year={}, month={}", year, month);
		
		try {
			MonthlyVoteResultResponse response = monthlyVoteResultService.getMonthlyVoteResult(year, month);
			return ResponseEntity.ok(ApiResponse.success(
				year + "년 " + month + "월 통합 투표 결과를 조회했습니다.", response));
		} catch (Exception e) {
			log.error("월별 통합 투표 결과 조회 실패: year={}, month={}, error={}", year, month, e.getMessage());
			return ResponseEntity.badRequest().body(
				ApiResponse.error(e.getMessage(), null));
		}
	}
}
