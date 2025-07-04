package publicdata.hackathon.diplomats.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import publicdata.hackathon.diplomats.domain.entity.PublicDiplomacyProgram;

@Repository
public interface PublicDiplomacyProgramRepository extends JpaRepository<PublicDiplomacyProgram, Long> {

	List<PublicDiplomacyProgram> findTop5ByCitizenTypeOrderByMatchScoreDescBusinessYearDesc(String citizenType);

	@Query("SELECT p.citizenType, COUNT(p) FROM PublicDiplomacyProgram p GROUP BY p.citizenType")
	List<Object[]> countByEachType();

	List<PublicDiplomacyProgram> findTop3ByCitizenTypeOrderByMatchScoreDesc(String citizenType);

	Optional<PublicDiplomacyProgram> findTopByOrderByCreatedAtDesc();
	Optional<PublicDiplomacyProgram> findTopByOrderByBusinessYearDesc();
}