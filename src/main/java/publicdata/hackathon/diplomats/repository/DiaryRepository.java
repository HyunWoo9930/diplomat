package publicdata.hackathon.diplomats.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import publicdata.hackathon.diplomats.domain.entity.Diary;

@Repository
public interface DiaryRepository extends JpaRepository<Diary, Long> {
	
	Page<Diary> findAllByOrderByCreatedAtDesc(Pageable pageable);
	Page<Diary> findAllByOrderByViewCountDesc(Pageable pageable);
	Page<Diary> findAllByOrderByLikesDesc(Pageable pageable);
}
