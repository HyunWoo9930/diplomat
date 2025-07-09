package publicdata.hackathon.diplomats.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import publicdata.hackathon.diplomats.domain.entity.PressRelease;

@Repository
public interface PressReleaseRepository extends JpaRepository<PressRelease, Long> {

	List<PressRelease> findTop5ByCitizenTypeOrderByMatchScoreDescPublishDateDesc(String citizenType);

	void deleteByCitizenType(String citizenType);

	@Query("SELECT pr.citizenType, COUNT(pr) FROM PressRelease pr GROUP BY pr.citizenType")
	List<Object[]> countByEachType();

	// PressReleaseRepository.java에 추가
	Optional<PressRelease> findTopByOrderByCreatedAtDesc();

	List<PressRelease> findTop3ByCitizenTypeOrderByMatchScoreDesc(String citizenType);

	Page<PressRelease> findAllByOrderByPublishDateDescCreatedAtDesc(Pageable pageable);

	// 제목이나 내용에 특정 키워드가 포함된 뉴스 조회
	@Query("SELECT pr FROM PressRelease pr WHERE " +
		"LOWER(pr.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
		"LOWER(pr.content) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
		"ORDER BY pr.publishDate DESC, pr.createdAt DESC")
	Page<PressRelease> findByKeywordContaining(@Param("keyword") String keyword, Pageable pageable);

	// 여러 키워드 중 하나라도 포함된 뉴스 조회 (수정된 버전)
	@Query("SELECT DISTINCT pr FROM PressRelease pr WHERE " +
		"(:keyword1 IS NULL OR LOWER(pr.title) LIKE LOWER(CONCAT('%', :keyword1, '%')) OR LOWER(pr.content) LIKE LOWER(CONCAT('%', :keyword1, '%'))) OR " +
		"(:keyword2 IS NULL OR LOWER(pr.title) LIKE LOWER(CONCAT('%', :keyword2, '%')) OR LOWER(pr.content) LIKE LOWER(CONCAT('%', :keyword2, '%'))) OR " +
		"(:keyword3 IS NULL OR LOWER(pr.title) LIKE LOWER(CONCAT('%', :keyword3, '%')) OR LOWER(pr.content) LIKE LOWER(CONCAT('%', :keyword3, '%'))) OR " +
		"(:keyword4 IS NULL OR LOWER(pr.title) LIKE LOWER(CONCAT('%', :keyword4, '%')) OR LOWER(pr.content) LIKE LOWER(CONCAT('%', :keyword4, '%'))) OR " +
		"(:keyword5 IS NULL OR LOWER(pr.title) LIKE LOWER(CONCAT('%', :keyword5, '%')) OR LOWER(pr.content) LIKE LOWER(CONCAT('%', :keyword5, '%'))) " +
		"ORDER BY pr.publishDate DESC, pr.createdAt DESC")
	Page<PressRelease> findByKeywordsContaining(
		@Param("keyword1") String keyword1,
		@Param("keyword2") String keyword2,
		@Param("keyword3") String keyword3,
		@Param("keyword4") String keyword4,
		@Param("keyword5") String keyword5,
		Pageable pageable
	);

	// 특정 키워드 포함 개수 조회
	@Query("SELECT COUNT(pr) FROM PressRelease pr WHERE " +
		"LOWER(pr.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
		"LOWER(pr.content) LIKE LOWER(CONCAT('%', :keyword, '%'))")
	long countByKeywordContaining(@Param("keyword") String keyword);

	List<PressRelease> findAllByOrderByPublishDateDesc(Pageable pageable);
}