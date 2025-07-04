package publicdata.hackathon.diplomats.repository;

import java.util.List;
import java.util.Optional;

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
}