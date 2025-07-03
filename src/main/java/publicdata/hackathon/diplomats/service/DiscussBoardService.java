package publicdata.hackathon.diplomats.service;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import publicdata.hackathon.diplomats.repository.DiscussBoardRepository;
import publicdata.hackathon.diplomats.repository.DiscussBoardCommentRepository;
import publicdata.hackathon.diplomats.repository.DiscussImageRepository;

@Service
@RequiredArgsConstructor
public class DiscussBoardService {
	
	private final DiscussBoardRepository discussBoardRepository;
	private final DiscussBoardCommentRepository discussBoardCommentRepository;
	private final DiscussImageRepository discussImageRepository;

}
