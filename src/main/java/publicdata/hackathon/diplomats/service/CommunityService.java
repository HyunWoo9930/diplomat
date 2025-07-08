package publicdata.hackathon.diplomats.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import publicdata.hackathon.diplomats.domain.dto.response.CommunityResponse;
import publicdata.hackathon.diplomats.domain.dto.response.PopularDiscussBoardResponse;
import publicdata.hackathon.diplomats.domain.dto.response.PopularFreeBoardResponse;
import publicdata.hackathon.diplomats.domain.entity.DiscussBoard;
import publicdata.hackathon.diplomats.domain.entity.FreeBoard;
import publicdata.hackathon.diplomats.repository.DiscussBoardCommentRepository;
import publicdata.hackathon.diplomats.repository.DiscussBoardRepository;
import publicdata.hackathon.diplomats.repository.FreeBoardCommentRepository;
import publicdata.hackathon.diplomats.repository.FreeBoardRepository;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommunityService {

	private final FreeBoardRepository freeBoardRepository;
	private final DiscussBoardRepository discussBoardRepository;
	private final FreeBoardCommentRepository freeBoardCommentRepository;
	private final DiscussBoardCommentRepository discussBoardCommentRepository;

	/**
	 * 커뮤니티 메인 페이지 데이터 조회
	 * 자유게시판 인기글 상위 3개 + 토론게시판 인기글 상위 3개
	 */
	public CommunityResponse getCommunityData() {
		// 자유게시판 인기글 상위 3개
		List<FreeBoard> popularFreeBoards = freeBoardRepository.findTop3ByOrderByLikesDesc();
		List<PopularFreeBoardResponse> freeBoardResponses = popularFreeBoards.stream()
			.map(this::mapToPopularFreeBoardResponse)
			.toList();

		// 토론게시판 인기글 상위 3개  
		List<DiscussBoard> popularDiscussBoards = discussBoardRepository.findTop3ByOrderByLikesDesc();
		List<PopularDiscussBoardResponse> discussBoardResponses = popularDiscussBoards.stream()
			.map(this::mapToPopularDiscussBoardResponse)
			.toList();

		return CommunityResponse.builder()
			.popularFreeBoards(freeBoardResponses)
			.popularDiscussBoards(discussBoardResponses)
			.build();
	}

	/**
	 * 자유게시판 인기글만 조회 (상위 3개)
	 */
	public List<PopularFreeBoardResponse> getPopularFreeBoards() {
		List<FreeBoard> popularFreeBoards = freeBoardRepository.findTop3ByOrderByLikesDesc();
		return popularFreeBoards.stream()
			.map(this::mapToPopularFreeBoardResponse)
			.toList();
	}

	/**
	 * 토론게시판 인기글만 조회 (상위 3개)
	 */
	public List<PopularDiscussBoardResponse> getPopularDiscussBoards() {
		List<DiscussBoard> popularDiscussBoards = discussBoardRepository.findTop3ByOrderByLikesDesc();
		return popularDiscussBoards.stream()
			.map(this::mapToPopularDiscussBoardResponse)
			.toList();
	}

	/**
	 * FreeBoard를 PopularFreeBoardResponse로 변환
	 */
	private PopularFreeBoardResponse mapToPopularFreeBoardResponse(FreeBoard freeBoard) {
		long commentCount = freeBoardCommentRepository.countByFreeBoard(freeBoard);
		
		return PopularFreeBoardResponse.builder()
			.id(freeBoard.getId())
			.title(freeBoard.getTitle())
			.content(freeBoard.getContent())
			.likes(freeBoard.getLikes())
			.viewCount(freeBoard.getViewCount())
			.userId(freeBoard.getUser().getUserId())
			.createdAt(freeBoard.getCreatedAt())
			.commentCount((int) commentCount)
			.build();
	}

	/**
	 * DiscussBoard를 PopularDiscussBoardResponse로 변환
	 */
	private PopularDiscussBoardResponse mapToPopularDiscussBoardResponse(DiscussBoard discussBoard) {
		long commentCount = discussBoardCommentRepository.countByDiscussBoard(discussBoard);
		
		return PopularDiscussBoardResponse.builder()
			.id(discussBoard.getId())
			.title(discussBoard.getTitle())
			.content(discussBoard.getContent())
			.discussType(discussBoard.getDiscussType())
			.likes(discussBoard.getLikes())
			.viewCount(discussBoard.getViewCount())
			.userId(discussBoard.getUser().getUserId())
			.createdAt(discussBoard.getCreatedAt())
			.commentCount((int) commentCount)
			.build();
	}
}
