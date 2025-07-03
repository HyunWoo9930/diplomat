package publicdata.hackathon.diplomats.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import publicdata.hackathon.diplomats.domain.entity.FreeBoard;

@Repository
public interface FreeBoardRepository extends JpaRepository<FreeBoard, Long> {
	// 최신순 정렬
	Page<FreeBoard> findAllByOrderByCreatedAtDesc(Pageable pageable);
	
	// 조회수순 정렬 (높은 순)
	Page<FreeBoard> findAllByOrderByViewCountDesc(Pageable pageable);
	
	// 좋아요순 정렬 (높은 순)
	Page<FreeBoard> findAllByOrderByLikesDesc(Pageable pageable);
}
