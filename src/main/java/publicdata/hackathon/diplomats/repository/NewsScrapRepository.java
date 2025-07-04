package publicdata.hackathon.diplomats.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import publicdata.hackathon.diplomats.domain.entity.NewsScrap;
import publicdata.hackathon.diplomats.domain.entity.PressRelease;
import publicdata.hackathon.diplomats.domain.entity.User;

@Repository
public interface NewsScrapRepository extends JpaRepository<NewsScrap, Long> {

	// 특정 사용자가 특정 뉴스를 스크랩했는지 확인
	Optional<NewsScrap> findByUserAndPressRelease(User user, PressRelease pressRelease);

	// 사용자가 스크랩했는지 여부 확인
	boolean existsByUserAndPressRelease(User user, PressRelease pressRelease);

	// 특정 사용자의 스크랩 목록 조회 (최신순)
	Page<NewsScrap> findByUserOrderByScrapedAtDesc(User user, Pageable pageable);

	// 특정 사용자의 스크랩 개수
	long countByUser(User user);

	// 특정 뉴스의 스크랩 개수
	long countByPressRelease(PressRelease pressRelease);

	// 사용자와 뉴스로 스크랩 삭제
	void deleteByUserAndPressRelease(User user, PressRelease pressRelease);
}