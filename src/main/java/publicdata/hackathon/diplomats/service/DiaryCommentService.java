package publicdata.hackathon.diplomats.service;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import publicdata.hackathon.diplomats.repository.DiaryCommentRepository;

@Service
@RequiredArgsConstructor
public class DiaryCommentService {
	
	private final DiaryCommentRepository diaryCommentRepository;

}
