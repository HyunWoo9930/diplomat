package publicdata.hackathon.diplomats.service;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import publicdata.hackathon.diplomats.repository.FreeBoardCommentRepository;

@Service
@RequiredArgsConstructor
public class FreeBoardCommentService {
	
	private final FreeBoardCommentRepository freeBoardCommentRepository;

}
