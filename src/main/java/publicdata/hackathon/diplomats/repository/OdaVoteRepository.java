package publicdata.hackathon.diplomats.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import publicdata.hackathon.diplomats.domain.entity.OdaVote;
import publicdata.hackathon.diplomats.domain.enums.VoteStatus;

@Repository
public interface OdaVoteRepository extends JpaRepository<OdaVote, Long> {
	
	Optional<OdaVote> findByYearAndMonth(Integer year, Integer month);
	
	Optional<OdaVote> findByStatus(VoteStatus status);
	
	@Query("SELECT ov FROM OdaVote ov WHERE ov.status = 'ACTIVE' ORDER BY ov.createdAt DESC")
	Optional<OdaVote> findCurrentActiveVote();
	
	@Query("SELECT ov FROM OdaVote ov WHERE ov.year = :year AND ov.month = :month AND ov.status = 'ACTIVE'")
	Optional<OdaVote> findActiveVoteByYearAndMonth(@Param("year") Integer year, @Param("month") Integer month);
	
	boolean existsByYearAndMonth(Integer year, Integer month);
}
