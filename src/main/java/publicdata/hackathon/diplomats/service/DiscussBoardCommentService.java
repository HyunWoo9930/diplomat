package publicdata.hackathon.diplomats.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import publicdata.hackathon.diplomats.domain.dto.request.CommentUpdateRequest;
import publicdata.hackathon.diplomats.domain.dto.request.DiscussCommentRequest;
import publicdata.hackathon.diplomats.domain.entity.DiscussBoard;
import publicdata.hackathon.diplomats.domain.entity.DiscussBoardComment;
import publicdata.hackathon.diplomats.domain.entity.User;
import publicdata.hackathon.diplomats.repository.DiscussBoardCommentRepository;
import publicdata.hackathon.diplomats.repository.DiscussBoardRepository;
import publicdata.hackathon.diplomats.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class DiscussBoardCommentService {
	
	private final DiscussBoardCommentRepository discussBoardCommentRepository;
	private final UserRepository userRepository;
	private final DiscussBoardRepository discussBoardRepository;

	public void commentDiscussBoard(String username, Long id, DiscussCommentRequest commentRequest) {
		User user = userRepository.findByUserId(username).orElseThrow();
		DiscussBoard discussBoard = discussBoardRepository.findById(id).orElseThrow();

		DiscussBoardComment discussBoardComment = DiscussBoardComment.builder()
			.discussBoard(discussBoard)
			.content(commentRequest.getComment())
			.commentType(commentRequest.getCommentType())
			.user(user)
			.build();
		discussBoardCommentRepository.save(discussBoardComment);
	}

	public void updateComment(String username, Long commentId, CommentUpdateRequest request) {
		DiscussBoardComment comment = discussBoardCommentRepository.findById(commentId)
			.orElseThrow(() -> new EntityNotFoundException("댓글을 찾을 수 없습니다."));

		// 작성자 확인
		if (!comment.getUser().getUserId().equals(username)) {
			throw new RuntimeException("댓글 수정 권한이 없습니다.");
		}

		// 댓글 수정
		comment.setContent(request.getComment());
		comment.setUpdatedAt(LocalDateTime.now());

		discussBoardCommentRepository.save(comment);
	}

	public void deleteComment(String username, Long commentId) {
		DiscussBoardComment comment = discussBoardCommentRepository.findById(commentId)
			.orElseThrow(() -> new EntityNotFoundException("댓글을 찾을 수 없습니다."));

		// 작성자 확인
		if (!comment.getUser().getUserId().equals(username)) {
			throw new RuntimeException("댓글 삭제 권한이 없습니다.");
		}

		discussBoardCommentRepository.delete(comment);
	}
}
