package publicdata.hackathon.diplomats.service;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import publicdata.hackathon.diplomats.repository.DiscussBoardCommentRepository;

@Service
@RequiredArgsConstructor
public class DiscussBoardCommentService {
	
	private final DiscussBoardCommentRepository discussBoardCommentRepository;

}
