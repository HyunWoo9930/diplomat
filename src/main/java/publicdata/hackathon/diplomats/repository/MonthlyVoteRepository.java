package publicdata.hackathon.diplomats.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import publicdata.hackathon.diplomats.domain.entity.MonthlyVote;
import publicdata.hackathon.diplomats.domain.enums.VoteStatus;

@Repository
public interface MonthlyVoteRepository extends JpaRepository<MonthlyVote, Long> {
	
	Optional<MonthlyVote> findByYearAndMonth(Integer year, Integer month);
	
	Optional<MonthlyVote> findByStatus(VoteStatus status);
	
	@Query("SELECT mv FROM MonthlyVote mv WHERE mv.status = 'ACTIVE' ORDER BY mv.year DESC, mv.month DESC LIMIT 1")
	Optional<MonthlyVote> findCurrentActiveVote();
	
	boolean existsByYearAndMonth(Integer year, Integer month);
}
