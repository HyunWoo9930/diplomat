package publicdata.hackathon.diplomats.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import publicdata.hackathon.diplomats.domain.entity.FreeBoard;
import publicdata.hackathon.diplomats.domain.entity.FreeBoardComment;

@Repository
public interface FreeBoardCommentRepository extends JpaRepository<FreeBoardComment, Long> {
	List<FreeBoardComment> findAllByFreeBoard(FreeBoard freeBoard);
}
