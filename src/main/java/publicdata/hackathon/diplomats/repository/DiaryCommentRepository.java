package publicdata.hackathon.diplomats.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import publicdata.hackathon.diplomats.domain.entity.Diary;
import publicdata.hackathon.diplomats.domain.entity.DiaryComment;

@Repository
public interface DiaryCommentRepository extends JpaRepository<DiaryComment, Long> {
	
	List<DiaryComment> findAllByDiary(Diary diary);
	
	// 댓글 수 조회
	long countByDiary(Diary diary);
}
