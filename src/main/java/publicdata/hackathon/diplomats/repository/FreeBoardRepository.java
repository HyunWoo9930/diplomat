package publicdata.hackathon.diplomats.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import publicdata.hackathon.diplomats.domain.entity.FreeBoard;
import publicdata.hackathon.diplomats.domain.entity.User;

@Repository
public interface FreeBoardRepository extends JpaRepository<FreeBoard, Long> {
	// 최신순 정렬
	Page<FreeBoard> findAllByOrderByCreatedAtDesc(Pageable pageable);
	
	// 조회수순 정렬 (높은 순)
	Page<FreeBoard> findAllByOrderByViewCountDesc(Pageable pageable);
	
	// 좋아요순 정렬 (높은 순)
	Page<FreeBoard> findAllByOrderByLikesDesc(Pageable pageable);
	
	// 커뮤니티용 - 좋아요순 상위 3개
	List<FreeBoard> findTop3ByOrderByLikesDesc();
	
	// 메인페이지용 - 좋아요순 정렬 후 최신순 서브정렬
	List<FreeBoard> findAllByOrderByLikesDescCreatedAtDesc(Pageable pageable);
	
	// 내 게시글 조회
	Page<FreeBoard> findByUserOrderByCreatedAtDesc(User user, Pageable pageable);
}
