package publicdata.hackathon.diplomats.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import publicdata.hackathon.diplomats.domain.entity.DiscussBoard;
import publicdata.hackathon.diplomats.domain.entity.User;

@Repository
public interface DiscussBoardRepository extends JpaRepository<DiscussBoard, Long> {
	
	Page<DiscussBoard> findAllByOrderByCreatedAtDesc(Pageable pageable);
	Page<DiscussBoard> findAllByOrderByViewCountDesc(Pageable pageable);
	Page<DiscussBoard> findAllByOrderByLikesDesc(Pageable pageable);
	
	// 커뮤니티용 - 좋아요순 상위 3개
	List<DiscussBoard> findTop3ByOrderByLikesDesc();
	
	// 내 게시글 조회
	Page<DiscussBoard> findByUserOrderByCreatedAtDesc(User user, Pageable pageable);
}
