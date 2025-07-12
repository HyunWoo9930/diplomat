package publicdata.hackathon.diplomats.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import publicdata.hackathon.diplomats.domain.entity.MonthlyVote;
import publicdata.hackathon.diplomats.domain.entity.User;
import publicdata.hackathon.diplomats.domain.entity.UserVote;

@Repository
public interface UserVoteRepository extends JpaRepository<UserVote, Long> {

	Optional<UserVote> findByUserAndMonthlyVote(User user, MonthlyVote monthlyVote);

	boolean existsByUserAndMonthlyVote(User user, MonthlyVote monthlyVote);

	long countByMonthlyVote(MonthlyVote monthlyVote);

	void deleteByUser(User user);
}