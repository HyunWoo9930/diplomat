package publicdata.hackathon.diplomats.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import publicdata.hackathon.diplomats.domain.entity.DiscussBoard;
import publicdata.hackathon.diplomats.domain.entity.DiscussBoardComment;

@Repository
public interface DiscussBoardCommentRepository extends JpaRepository<DiscussBoardComment, Long> {
	
	List<DiscussBoardComment> findAllByDiscussBoard(DiscussBoard discussBoard);
}
