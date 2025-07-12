package publicdata.hackathon.diplomats.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import publicdata.hackathon.diplomats.domain.entity.User;
import publicdata.hackathon.diplomats.domain.entity.UserStamp;
import publicdata.hackathon.diplomats.domain.enums.StampType;

@Repository
public interface UserStampRepository extends JpaRepository<UserStamp, Long> {
    
    /**
     * 사용자의 모든 스탬프 조회 (최신순)
     */
    List<UserStamp> findByUserOrderByEarnedAtDesc(User user);

    /**
     * 사용자의 총 스탬프 개수 조회
     */
    @Query("SELECT COALESCE(SUM(us.stampCount), 0) FROM UserStamp us WHERE us.user = :user")
    Integer getTotalStampCountByUser(@Param("user") User user);

    /**
     * 사용자의 특정 스탬프 타입별 개수 조회
     */
    @Query("SELECT COALESCE(SUM(us.stampCount), 0) FROM UserStamp us WHERE us.user = :user AND us.stampType = :stampType")
    Integer getStampCountByUserAndType(@Param("user") User user, @Param("stampType") StampType stampType);

    /**
     * 특정 엔티티에 대한 스탬프가 이미 존재하는지 확인
     */
    Optional<UserStamp> findByUserAndStampTypeAndRelatedEntityTypeAndRelatedEntityId(
        User user, StampType stampType, String relatedEntityType, Long relatedEntityId);

    /**
     * 사용자의 최근 스탬프 내역 조회 (제한된 개수)
     */
    List<UserStamp> findTop10ByUserOrderByEarnedAtDesc(User user);
    
    /**
     * 사용자의 최근 스탬프 내역 조회 (20개)
     */
    List<UserStamp> findTop20ByUserOrderByEarnedAtDesc(User user);

    /**
     * 특정 날짜 이후 획득한 스탬프 조회
     */
    List<UserStamp> findByUserAndEarnedAtAfterOrderByEarnedAtDesc(User user, LocalDateTime afterDate);

    /**
     * 특정 기간 동안 획득한 스탬프 조회
     */
    @Query("SELECT us FROM UserStamp us WHERE us.user = :user AND us.earnedAt BETWEEN :startDate AND :endDate ORDER BY us.earnedAt DESC")
    List<UserStamp> findByUserAndEarnedAtBetween(
        @Param("user") User user, 
        @Param("startDate") LocalDateTime startDate, 
        @Param("endDate") LocalDateTime endDate);

    /**
     * 중복 스탬프 방지를 위한 체크 (같은 활동에 대해 한 번만 스탬프 지급)
     */
    boolean existsByUserAndStampTypeAndRelatedEntityTypeAndRelatedEntityId(
        User user, StampType stampType, String relatedEntityType, Long relatedEntityId);
    
    /**
     * 특정 사용자의 모든 스탬프 삭제 (회원 탈퇴시 사용)
     */
    void deleteByUser(User user);
}
