package publicdata.hackathon.diplomats.service;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import publicdata.hackathon.diplomats.repository.DiaryRepository;
import publicdata.hackathon.diplomats.repository.DiaryCommentRepository;
import publicdata.hackathon.diplomats.repository.DiaryImageRepository;

@Service
@RequiredArgsConstructor
public class DiaryService {
	
	private final DiaryRepository diaryRepository;
	private final DiaryCommentRepository diaryCommentRepository;
	private final DiaryImageRepository diaryImageRepository;

}
