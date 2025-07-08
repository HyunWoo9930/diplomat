package publicdata.hackathon.diplomats.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import publicdata.hackathon.diplomats.domain.dto.response.MyPostItemResponse;
import publicdata.hackathon.diplomats.domain.dto.response.MyPostsResponse;
import publicdata.hackathon.diplomats.domain.entity.Diary;
import publicdata.hackathon.diplomats.domain.entity.DiscussBoard;
import publicdata.hackathon.diplomats.domain.entity.FreeBoard;
import publicdata.hackathon.diplomats.domain.entity.User;
import publicdata.hackathon.diplomats.domain.enums.PostType;
import publicdata.hackathon.diplomats.repository.DiaryCommentRepository;
import publicdata.hackathon.diplomats.repository.DiaryRepository;
import publicdata.hackathon.diplomats.repository.DiscussBoardCommentRepository;
import publicdata.hackathon.diplomats.repository.DiscussBoardRepository;
import publicdata.hackathon.diplomats.repository.FreeBoardCommentRepository;
import publicdata.hackathon.diplomats.repository.FreeBoardRepository;
import publicdata.hackathon.diplomats.repository.UserRepository;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MyPostsService {

	private final UserRepository userRepository;
	private final FreeBoardRepository freeBoardRepository;
	private final DiscussBoardRepository discussBoardRepository;
	private final DiaryRepository diaryRepository;
	private final FreeBoardCommentRepository freeBoardCommentRepository;
	private final DiscussBoardCommentRepository discussBoardCommentRepository;
	private final DiaryCommentRepository diaryCommentRepository;

	/**
	 * 내가 작성한 게시글 모아보기
	 * @param username 사용자 ID
	 * @param filter ALL, FREE, DISCUSS, DIARY
	 * @param pageable 페이징 정보
	 */
	public MyPostsResponse getMyPosts(String username, String filter, Pageable pageable) {
		User user = userRepository.findByUserId(username)
			.orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다."));

		List<MyPostItemResponse> allPosts = new ArrayList<>();
		long totalCount = 0;

		switch (filter.toUpperCase()) {
			case "ALL":
				// 전체 게시글 조회
				allPosts.addAll(getMyFreeBoards(user, pageable));
				allPosts.addAll(getMyDiscussBoards(user, pageable));
				allPosts.addAll(getMyDiaries(user, pageable));
				
				// 생성일순으로 정렬
				allPosts.sort(Comparator.comparing(MyPostItemResponse::getCreatedAt).reversed());
				
				// 페이징 처리
				int start = (int) pageable.getOffset();
				int end = Math.min(start + pageable.getPageSize(), allPosts.size());
				allPosts = allPosts.subList(start, end);
				
				totalCount = countAllMyPosts(user);
				break;

			case "FREE":
				// 자유게시판만 조회
				allPosts.addAll(getMyFreeBoards(user, pageable));
				totalCount = freeBoardRepository.findByUserOrderByCreatedAtDesc(user, Pageable.unpaged()).getTotalElements();
				break;

			case "DISCUSS":
				// 토론게시판만 조회
				allPosts.addAll(getMyDiscussBoards(user, pageable));
				totalCount = discussBoardRepository.findByUserOrderByCreatedAtDesc(user, Pageable.unpaged()).getTotalElements();
				break;

			case "DIARY":
				// 외교일지만 조회
				allPosts.addAll(getMyDiaries(user, pageable));
				totalCount = diaryRepository.findByWriterOrderByCreatedAtDesc(user, Pageable.unpaged()).getTotalElements();
				break;

			default:
				throw new IllegalArgumentException("유효하지 않은 필터입니다: " + filter);
		}

		return MyPostsResponse.builder()
			.posts(allPosts)
			.totalCount(totalCount)
			.filter(filter.toUpperCase())
			.build();
	}

	/**
	 * 내 자유게시판 글 조회
	 */
	private List<MyPostItemResponse> getMyFreeBoards(User user, Pageable pageable) {
		Page<FreeBoard> freeBoards = freeBoardRepository.findByUserOrderByCreatedAtDesc(user, pageable);
		
		return freeBoards.stream()
			.map(freeBoard -> {
				long commentCount = freeBoardCommentRepository.countByFreeBoard(freeBoard);
				
				return MyPostItemResponse.builder()
					.id(freeBoard.getId())
					.title(freeBoard.getTitle())
					.content(freeBoard.getContent())
					.postType(PostType.FREE_BOARD)
					.likes(freeBoard.getLikes())
					.viewCount(freeBoard.getViewCount())
					.commentCount((int) commentCount)
					.createdAt(freeBoard.getCreatedAt())
					.updatedAt(freeBoard.getUpdatedAt())
					.build();
			})
			.toList();
	}

	/**
	 * 내 토론게시판 글 조회
	 */
	private List<MyPostItemResponse> getMyDiscussBoards(User user, Pageable pageable) {
		Page<DiscussBoard> discussBoards = discussBoardRepository.findByUserOrderByCreatedAtDesc(user, pageable);
		
		return discussBoards.stream()
			.map(discussBoard -> {
				long commentCount = discussBoardCommentRepository.countByDiscussBoard(discussBoard);
				
				return MyPostItemResponse.builder()
					.id(discussBoard.getId())
					.title(discussBoard.getTitle())
					.content(discussBoard.getContent())
					.postType(PostType.DISCUSS_BOARD)
					.discussType(discussBoard.getDiscussType())
					.likes(discussBoard.getLikes())
					.viewCount(discussBoard.getViewCount())
					.commentCount((int) commentCount)
					.createdAt(discussBoard.getCreatedAt())
					.updatedAt(discussBoard.getUpdatedAt())
					.build();
			})
			.toList();
	}

	/**
	 * 내 외교일지 조회
	 */
	private List<MyPostItemResponse> getMyDiaries(User user, Pageable pageable) {
		Page<Diary> diaries = diaryRepository.findByWriterOrderByCreatedAtDesc(user, pageable);
		
		return diaries.stream()
			.map(diary -> {
				long commentCount = diaryCommentRepository.countByDiary(diary);
				
				return MyPostItemResponse.builder()
					.id(diary.getId())
					.title(diary.getTitle())
					.content(diary.getDescription())
					.postType(PostType.DIARY)
					.action(diary.getAction())
					.likes(diary.getLikes())
					.viewCount(diary.getViewCount())
					.commentCount((int) commentCount)
					.createdAt(diary.getCreatedAt())
					.updatedAt(diary.getUpdatedAt())
					.build();
			})
			.toList();
	}

	/**
	 * 전체 게시글 수 계산
	 */
	private long countAllMyPosts(User user) {
		long freeBoardCount = freeBoardRepository.findByUserOrderByCreatedAtDesc(user, Pageable.unpaged()).getTotalElements();
		long discussBoardCount = discussBoardRepository.findByUserOrderByCreatedAtDesc(user, Pageable.unpaged()).getTotalElements();
		long diaryCount = diaryRepository.findByWriterOrderByCreatedAtDesc(user, Pageable.unpaged()).getTotalElements();
		
		return freeBoardCount + discussBoardCount + diaryCount;
	}
}
