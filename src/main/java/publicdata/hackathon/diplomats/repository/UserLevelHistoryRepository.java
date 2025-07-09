package publicdata.hackathon.diplomats.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import publicdata.hackathon.diplomats.domain.entity.User;
import publicdata.hackathon.diplomats.domain.entity.UserLevelHistory;

@Repository
public interface UserLevelHistoryRepository extends JpaRepository<UserLevelHistory, Long> {
    
    /**
     * 사용자의 레벨업 히스토리 조회 (최신순)
     */
    List<UserLevelHistory> findByUserOrderByLevelUpAtDesc(User user);

    /**
     * 사용자의 최근 레벨업 히스토리 조회 (제한된 개수)
     */
    List<UserLevelHistory> findTop5ByUserOrderByLevelUpAtDesc(User user);
    
    /**
     * 사용자의 최근 레벨업 히스토리 조회 (10개)
     */
    List<UserLevelHistory> findTop10ByUserOrderByLevelUpAtDesc(User user);
}
