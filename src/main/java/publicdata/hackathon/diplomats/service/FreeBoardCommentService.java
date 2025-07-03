package publicdata.hackathon.diplomats.service;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import publicdata.hackathon.diplomats.domain.dto.request.CommentRequest;
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
}
