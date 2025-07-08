package publicdata.hackathon.diplomats.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import publicdata.hackathon.diplomats.domain.entity.Diary;
import publicdata.hackathon.diplomats.domain.entity.User;

@Repository
public interface DiaryRepository extends JpaRepository<Diary, Long> {
	
	Page<Diary> findAllByOrderByCreatedAtDesc(Pageable pageable);
	Page<Diary> findAllByOrderByViewCountDesc(Pageable pageable);
	Page<Diary> findAllByOrderByLikesDesc(Pageable pageable);
	
	@Query("SELECT d FROM Diary d WHERE d.createdAt >= :startDate AND d.createdAt <= :endDate " +
		   "ORDER BY (d.viewCount + d.likes * 2) DESC")
	List<Diary> findTopDiariesByMonth(@Param("startDate") LocalDateTime startDate, 
									  @Param("endDate") LocalDateTime endDate, 
									  Pageable pageable);
	
	// 내 일지 조회
	Page<Diary> findByWriterOrderByCreatedAtDesc(User writer, Pageable pageable);
}
