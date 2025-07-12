package publicdata.hackathon.diplomats.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import publicdata.hackathon.diplomats.domain.entity.Like;
import publicdata.hackathon.diplomats.domain.entity.User;

@Repository
public interface LikeRepository extends JpaRepository<Like, Long> {
    
    // 특정 사용자가 특정 대상에 좋아요를 눌렀는지 확인
    Optional<Like> findByUserAndTargetTypeAndTargetId(User user, String targetType, Long targetId);
    
    // 특정 대상의 좋아요 개수 조회
    long countByTargetTypeAndTargetId(String targetType, Long targetId);
    
    // 좋아요 삭제 (좋아요 취소용)
    void deleteByUserAndTargetTypeAndTargetId(User user, String targetType, Long targetId);
    
    // 특정 대상과 관련된 모든 좋아요 삭제 (게시글 삭제시 사용)
    void deleteByTargetTypeAndTargetId(String targetType, Long targetId);
    
    // 특정 사용자의 모든 좋아요 삭제 (회원 탈퇴시 사용)
    void deleteByUser(User user);
    
    // 사용자가 좋아요를 눌렀는지 확인
    boolean existsByUserAndTargetTypeAndTargetId(User user, String targetType, Long targetId);
}
