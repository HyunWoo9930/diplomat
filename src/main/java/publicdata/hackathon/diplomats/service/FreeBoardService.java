package publicdata.hackathon.diplomats.service;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import publicdata.hackathon.diplomats.repository.FreeBoardRepository;
import publicdata.hackathon.diplomats.repository.FreeBoardCommentRepository;
import publicdata.hackathon.diplomats.repository.FreeBoardImageRepository;

@Service
@RequiredArgsConstructor
public class FreeBoardService {
	
	private final FreeBoardRepository freeBoardRepository;
	private final FreeBoardCommentRepository freeBoardCommentRepository;
	private final FreeBoardImageRepository freeBoardImageRepository;

}
