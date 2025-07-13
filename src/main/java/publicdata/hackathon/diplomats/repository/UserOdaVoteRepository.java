package publicdata.hackathon.diplomats.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import publicdata.hackathon.diplomats.domain.entity.User;
import publicdata.hackathon.diplomats.domain.entity.UserOdaVote;

@Repository
public interface UserOdaVoteRepository extends JpaRepository<UserOdaVote, Long> {
	
	Optional<UserOdaVote> findByUserIdAndOdaVoteId(Long userId, Long odaVoteId);
	
	boolean existsByUserIdAndOdaVoteId(Long userId, Long odaVoteId);
	
	@Query("SELECT uov FROM UserOdaVote uov WHERE uov.user.userId = :userId AND uov.odaVote.id = :voteId")
	Optional<UserOdaVote> findByUserIdStringAndOdaVoteId(@Param("userId") String userId, @Param("voteId") Long voteId);
	
	// 🔧 추가: String userId로 존재 여부 체크하는 메서드
	@Query("SELECT CASE WHEN COUNT(uov) > 0 THEN true ELSE false END FROM UserOdaVote uov WHERE uov.user.userId = :userId AND uov.odaVote.id = :voteId")
	boolean existsByUserIdStringAndOdaVoteId(@Param("userId") String userId, @Param("voteId") Long voteId);
	
	@Query("SELECT COUNT(uov) FROM UserOdaVote uov WHERE uov.odaVote.id = :voteId")
	long countByOdaVoteId(@Param("voteId") Long voteId);
	
	@Query("SELECT COUNT(uov) FROM UserOdaVote uov WHERE uov.odaVoteCandidate.id = :candidateId")
	long countByOdaVoteCandidateId(@Param("candidateId") Long candidateId);
	
	void deleteByUser(User user);
}
