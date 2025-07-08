package publicdata.hackathon.diplomats.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import publicdata.hackathon.diplomats.domain.dto.request.CommentRequest;
import publicdata.hackathon.diplomats.domain.dto.request.CommentUpdateRequest;
import publicdata.hackathon.diplomats.domain.entity.Diary;
import publicdata.hackathon.diplomats.domain.entity.DiaryComment;
import publicdata.hackathon.diplomats.domain.entity.User;
import publicdata.hackathon.diplomats.repository.DiaryCommentRepository;
import publicdata.hackathon.diplomats.repository.DiaryRepository;
import publicdata.hackathon.diplomats.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class DiaryCommentService {
	
	private final DiaryCommentRepository diaryCommentRepository;
	private final UserRepository userRepository;
	private final DiaryRepository diaryRepository;

	public void commentDiary(String username, Long id, CommentRequest commentRequest) {
		User user = userRepository.findByUserId(username).orElseThrow();
		Diary diary = diaryRepository.findById(id).orElseThrow();

		DiaryComment diaryComment = DiaryComment.builder()
			.diary(diary)
			.content(commentRequest.getComment())
			.commenter(user)
			.build();
		diaryCommentRepository.save(diaryComment);
	}

	public void updateComment(String username, Long commentId, CommentUpdateRequest request) {
		DiaryComment comment = diaryCommentRepository.findById(commentId)
			.orElseThrow(() -> new EntityNotFoundException("댓글을 찾을 수 없습니다."));

		// 작성자 확인
		if (!comment.getCommenter().getUserId().equals(username)) {
			throw new RuntimeException("댓글 수정 권한이 없습니다.");
		}

		// 댓글 수정
		comment.setContent(request.getComment());
		comment.setUpdatedAt(LocalDateTime.now());

		diaryCommentRepository.save(comment);
	}

	public void deleteComment(String username, Long commentId) {
		DiaryComment comment = diaryCommentRepository.findById(commentId)
			.orElseThrow(() -> new EntityNotFoundException("댓글을 찾을 수 없습니다."));

		// 작성자 확인
		if (!comment.getCommenter().getUserId().equals(username)) {
			throw new RuntimeException("댓글 삭제 권한이 없습니다.");
		}

		diaryCommentRepository.delete(comment);
	}
}
