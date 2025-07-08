package publicdata.hackathon.diplomats.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import publicdata.hackathon.diplomats.domain.entity.DiscussBoard;

@Repository
public interface DiscussBoardRepository extends JpaRepository<DiscussBoard, Long> {
	
	Page<DiscussBoard> findAllByOrderByCreatedAtDesc(Pageable pageable);
	Page<DiscussBoard> findAllByOrderByViewCountDesc(Pageable pageable);
	Page<DiscussBoard> findAllByOrderByLikesDesc(Pageable pageable);
}
