package publicdata.hackathon.diplomats.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import publicdata.hackathon.diplomats.domain.dto.request.CommentRequest;
import publicdata.hackathon.diplomats.domain.dto.request.CommentUpdateRequest;
import publicdata.hackathon.diplomats.domain.entity.FreeBoard;
import publicdata.hackathon.diplomats.domain.entity.FreeBoardComment;
import publicdata.hackathon.diplomats.domain.entity.User;
import publicdata.hackathon.diplomats.repository.FreeBoardCommentRepository;
import publicdata.hackathon.diplomats.repository.FreeBoardRepository;
import publicdata.hackathon.diplomats.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class FreeBoardCommentService {

	private final FreeBoardCommentRepository freeBoardCommentRepository;
	private final UserRepository userRepository;
	private final FreeBoardRepository freeBoardRepository;

	public void commentFreeBoard(String username, Long id, CommentRequest commentRequest) {
		User user = userRepository.findByUserId(username).orElseThrow();
		FreeBoard freeBoard = freeBoardRepository.findById(id).orElseThrow();

		FreeBoardComment freeBoardComment = FreeBoardComment.builder()
			.freeBoard(freeBoard)
			.content(commentRequest.getComment())
			.user(user)
			.build();
		freeBoardCommentRepository.save(freeBoardComment);
	}

	public void updateComment(String username, Long commentId, CommentUpdateRequest request) {
		FreeBoardComment comment = freeBoardCommentRepository.findById(commentId)
			.orElseThrow(() -> new EntityNotFoundException("댓글을 찾을 수 없습니다."));

		// 작성자 확인
		if (!comment.getUser().getUserId().equals(username)) {
			throw new RuntimeException("댓글 수정 권한이 없습니다.");
		}

		// 댓글 수정
		comment.setContent(request.getComment());
		comment.setUpdatedAt(LocalDateTime.now());

		freeBoardCommentRepository.save(comment);
	}

	public void deleteComment(String username, Long commentId) {
		FreeBoardComment comment = freeBoardCommentRepository.findById(commentId)
			.orElseThrow(() -> new EntityNotFoundException("댓글을 찾을 수 없습니다."));

		// 작성자 확인
		if (!comment.getUser().getUserId().equals(username)) {
			throw new RuntimeException("댓글 삭제 권한이 없습니다.");
		}

		freeBoardCommentRepository.delete(comment);
	}
}
