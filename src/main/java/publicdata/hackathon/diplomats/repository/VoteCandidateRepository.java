package publicdata.hackathon.diplomats.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import publicdata.hackathon.diplomats.domain.entity.Diary;
import publicdata.hackathon.diplomats.domain.entity.MonthlyVote;
import publicdata.hackathon.diplomats.domain.entity.VoteCandidate;

@Repository
public interface VoteCandidateRepository extends JpaRepository<VoteCandidate, Long> {
	
	List<VoteCandidate> findByMonthlyVoteOrderByRanking(MonthlyVote monthlyVote);
	
	@Query("SELECT vc FROM VoteCandidate vc WHERE vc.monthlyVote = :monthlyVote ORDER BY vc.voteCount DESC, vc.ranking ASC")
	List<VoteCandidate> findByMonthlyVoteOrderByVoteCountDesc(@Param("monthlyVote") MonthlyVote monthlyVote);
	
	// 특정 일지에 대한 투표 후보 조회 (일지 삭제시 사용)
	List<VoteCandidate> findByDiary(Diary diary);
	
	// 특정 일지에 대한 투표 후보 삭제 (일지 삭제시 사용)
	void deleteByDiary(Diary diary);
}
