package publicdata.hackathon.diplomats.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import publicdata.hackathon.diplomats.domain.dto.response.LikeResponse;
import publicdata.hackathon.diplomats.domain.dto.response.StampEarnedResponse;
import publicdata.hackathon.diplomats.domain.entity.DiscussBoard;
import publicdata.hackathon.diplomats.domain.entity.Diary;
import publicdata.hackathon.diplomats.domain.entity.FreeBoard;
import publicdata.hackathon.diplomats.domain.entity.Like;
import publicdata.hackathon.diplomats.domain.entity.User;
import publicdata.hackathon.diplomats.repository.DiscussBoardRepository;
import publicdata.hackathon.diplomats.repository.DiaryRepository;
import publicdata.hackathon.diplomats.repository.FreeBoardRepository;
import publicdata.hackathon.diplomats.repository.LikeRepository;
import publicdata.hackathon.diplomats.repository.UserRepository;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class LikeService {
    
    private final LikeRepository likeRepository;
    private final UserRepository userRepository;
    private final FreeBoardRepository freeBoardRepository;
    private final DiscussBoardRepository discussBoardRepository;
    private final DiaryRepository diaryRepository;
    private final StampService stampService;
    
    public LikeResponse toggleLike(String username, String targetType, Long targetId) {
        User user = userRepository.findByUserId(username)
            .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다."));
        
        // 게시글 존재 여부 확인
        validateTargetExists(targetType, targetId);
        
        // 이미 좋아요를 눌렀는지 확인
        boolean alreadyLiked = likeRepository.existsByUserAndTargetTypeAndTargetId(user, targetType, targetId);
        
        if (alreadyLiked) {
            // 좋아요 취소
            likeRepository.deleteByUserAndTargetTypeAndTargetId(user, targetType, targetId);
            updateLikeCount(targetType, targetId, -1);
            
            long likeCount = likeRepository.countByTargetTypeAndTargetId(targetType, targetId);
            return LikeResponse.builder()
                .isLiked(false)
                .likeCount(likeCount)
                .message("좋아요를 취소했습니다.")
                .build();
        } else {
            // 좋아요 추가
            Like like = Like.builder()
                .user(user)
                .targetType(targetType)
                .targetId(targetId)
                .build();
            
            likeRepository.save(like);
            updateLikeCount(targetType, targetId, 1);
            
            // 🎯 실천일기에 좋아요를 받았을 때 작성자에게 스탬프 지급 (자기 글이 아닌 경우에만)
            if ("Diary".equals(targetType)) {
                try {
                    Diary diary = diaryRepository.findById(targetId)
                        .orElseThrow(() -> new EntityNotFoundException("일지를 찾을 수 없습니다."));
                    
                    // 자기 글이 아닌 경우에만 스탬프 지급
                    if (!diary.getWriter().getId().equals(user.getId())) {
                        StampEarnedResponse stampResponse = stampService.earnDiaryLikeStamp(diary.getWriter(), diary.getId());
                        if (stampResponse.isSuccess()) {
                            log.info("실천일기 좋아요 받기 스탬프 지급 완료: authorId={}, diaryId={}, likerId={}, leveledUp={}", 
                                diary.getWriter().getUserId(), diary.getId(), username, stampResponse.isLeveledUp());
                        }
                    }
                } catch (Exception e) {
                    log.error("실천일기 좋아요 받기 스탬프 지급 실패: targetId={}, likerId={}", targetId, username, e);
                }
            }
            
            long likeCount = likeRepository.countByTargetTypeAndTargetId(targetType, targetId);
            return LikeResponse.builder()
                .isLiked(true)
                .likeCount(likeCount)
                .message("좋아요를 눌렀습니다.")
                .build();
        }
    }
    
    public LikeResponse getLikeStatus(String username, String targetType, Long targetId) {
        User user = userRepository.findByUserId(username)
            .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다."));
        
        boolean isLiked = likeRepository.existsByUserAndTargetTypeAndTargetId(user, targetType, targetId);
        long likeCount = likeRepository.countByTargetTypeAndTargetId(targetType, targetId);
        
        return LikeResponse.builder()
            .isLiked(isLiked)
            .likeCount(likeCount)
            .message("좋아요 상태 조회 성공")
            .build();
    }
    
    private void validateTargetExists(String targetType, Long targetId) {
        switch (targetType) {
            case "FreeBoard":
                if (!freeBoardRepository.existsById(targetId)) {
                    throw new EntityNotFoundException("자유게시판 게시글을 찾을 수 없습니다.");
                }
                break;
            case "DiscussBoard":
                if (!discussBoardRepository.existsById(targetId)) {
                    throw new EntityNotFoundException("토론게시판 게시글을 찾을 수 없습니다.");
                }
                break;
            case "Diary":
                if (!diaryRepository.existsById(targetId)) {
                    throw new EntityNotFoundException("일지를 찾을 수 없습니다.");
                }
                break;
            default:
                throw new RuntimeException("지원하지 않는 타겟 타입입니다: " + targetType);
        }
    }
    
    private void updateLikeCount(String targetType, Long targetId, int delta) {
        switch (targetType) {
            case "FreeBoard":
                FreeBoard freeBoard = freeBoardRepository.findById(targetId)
                    .orElseThrow(() -> new EntityNotFoundException("게시글을 찾을 수 없습니다."));
                freeBoard.setLikes(freeBoard.getLikes() + delta);
                freeBoardRepository.save(freeBoard);
                break;
                
            case "DiscussBoard":
                DiscussBoard discussBoard = discussBoardRepository.findById(targetId)
                    .orElseThrow(() -> new EntityNotFoundException("게시글을 찾을 수 없습니다."));
                discussBoard.setLikes(discussBoard.getLikes() + delta);
                discussBoardRepository.save(discussBoard);
                break;
                
            case "Diary":
                Diary diary = diaryRepository.findById(targetId)
                    .orElseThrow(() -> new EntityNotFoundException("게시글을 찾을 수 없습니다."));
                diary.setLikes(diary.getLikes() + delta);
                diaryRepository.save(diary);
                break;
                
            default:
                throw new RuntimeException("지원하지 않는 타겟 타입입니다: " + targetType);
        }
    }
}
