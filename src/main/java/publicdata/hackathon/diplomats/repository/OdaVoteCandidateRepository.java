package publicdata.hackathon.diplomats.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import publicdata.hackathon.diplomats.domain.entity.OdaVoteCandidate;

@Repository
public interface OdaVoteCandidateRepository extends JpaRepository<OdaVoteCandidate, Long> {
	
	List<OdaVoteCandidate> findByOdaVoteIdOrderByVoteCountDesc(Long odaVoteId);
	
	@Query("SELECT ovc FROM OdaVoteCandidate ovc WHERE ovc.odaVote = :odaVote ORDER BY ovc.voteCount DESC")
	List<OdaVoteCandidate> findByOdaVoteOrderByVoteCountDesc(@Param("odaVote") publicdata.hackathon.diplomats.domain.entity.OdaVote odaVote);
	
	Optional<OdaVoteCandidate> findByOdaVoteIdAndOdaProjectId(Long odaVoteId, Long odaProjectId);
	
	@Query("SELECT ovc FROM OdaVoteCandidate ovc WHERE ovc.odaVote.id = :voteId ORDER BY ovc.voteCount DESC, ovc.odaProject.matchScore DESC")
	List<OdaVoteCandidate> findByOdaVoteIdOrderByVoteCountDescMatchScoreDesc(@Param("voteId") Long voteId);
	
	@Query("SELECT COUNT(ovc) FROM OdaVoteCandidate ovc WHERE ovc.odaVote.id = :voteId")
	long countByOdaVoteId(@Param("voteId") Long voteId);
}
