package publicdata.hackathon.diplomats.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import publicdata.hackathon.diplomats.domain.entity.OdaProject;

@Repository
public interface OdaProjectRepository extends JpaRepository<OdaProject, Long> {
	
	// 투표용 - 각 분야별 상위 프로젝트 조회
	List<OdaProject> findTop5ByCategoryOrderByMatchScoreDescPublishDateDesc(String category);
	
	// 상태 조회용 - 분야별 카운트
	@Query("SELECT o.category, COUNT(o) FROM OdaProject o GROUP BY o.category")
	List<Object[]> countByEachCategory();
}
