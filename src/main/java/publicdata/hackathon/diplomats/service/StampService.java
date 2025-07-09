package publicdata.hackathon.diplomats.service;

import java.time.LocalDate;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import publicdata.hackathon.diplomats.domain.dto.response.DailyStampHistoryResponse;
import publicdata.hackathon.diplomats.domain.dto.response.MyPageResponse;
import publicdata.hackathon.diplomats.domain.dto.response.StampEarnedResponse;
import publicdata.hackathon.diplomats.domain.dto.response.StampStatisticsResponse;
import publicdata.hackathon.diplomats.domain.dto.response.UserLevelHistoryResponse;
import publicdata.hackathon.diplomats.domain.dto.response.UserLevelResponse;
import publicdata.hackathon.diplomats.domain.dto.response.UserStampResponse;
import publicdata.hackathon.diplomats.domain.entity.User;
import publicdata.hackathon.diplomats.domain.entity.UserLevelHistory;
import publicdata.hackathon.diplomats.domain.entity.UserStamp;
import publicdata.hackathon.diplomats.domain.enums.StampType;
import publicdata.hackathon.diplomats.domain.enums.UserLevel;
import publicdata.hackathon.diplomats.repository.UserLevelHistoryRepository;
import publicdata.hackathon.diplomats.repository.UserRepository;
import publicdata.hackathon.diplomats.repository.UserStampRepository;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class StampService {

    private final UserStampRepository userStampRepository;
    private final UserLevelHistoryRepository userLevelHistoryRepository;
    private final UserRepository userRepository;

    /**
     * 스탬프 지급 (중복 체크 포함)
     */
    public StampEarnedResponse earnStamp(User user, StampType stampType, String relatedEntityType, Long relatedEntityId) {
        log.info("스탬프 지급 시작: userId={}, stampType={}, entityType={}, entityId={}", 
                user.getUserId(), stampType, relatedEntityType, relatedEntityId);

        // 중복 스탬프 체크
        if (isDuplicateStamp(user, stampType, relatedEntityType, relatedEntityId)) {
            log.info("중복 스탬프로 인한 지급 취소: userId={}, stampType={}", user.getUserId(), stampType);
            return StampEarnedResponse.builder()
                .success(false)
                .message("이미 해당 활동에 대한 스탬프를 받았습니다.")
                .build();
        }

        // 이전 레벨 저장
        UserLevel previousLevel = user.getCurrentLevel();

        // 스탬프 생성 및 저장
        UserStamp stamp = UserStamp.builder()
            .user(user)
            .stampType(stampType)
            .relatedEntityType(relatedEntityType)
            .relatedEntityId(relatedEntityId)
            .build();

        userStampRepository.save(stamp);

        // 사용자 스탬프 개수 업데이트 및 레벨 확인
        user.addStamps(stampType.getStampCount());
        userRepository.save(user);

        // 레벨업 확인 및 히스토리 저장
        boolean leveledUp = user.hasLeveledUp(previousLevel);
        if (leveledUp) {
            saveLevelUpHistory(user, previousLevel);
            log.info("레벨업 발생: userId={}, {} -> {}", 
                    user.getUserId(), previousLevel.getDisplayName(), user.getCurrentLevel().getDisplayName());
        }

        return StampEarnedResponse.builder()
            .success(true)
            .message("스탬프를 획득했습니다!")
            .stampType(stampType)
            .stampDescription(stampType.getDescription())
            .stampsEarned(stampType.getStampCount())
            .totalStamps(user.getTotalStamps())
            .currentLevel(user.getCurrentLevel())
            .currentLevelDisplay(user.getCurrentLevel().getDisplayName())
            .leveledUp(leveledUp)
            .previousLevel(leveledUp ? previousLevel : null)
            .previousLevelDisplay(leveledUp ? previousLevel.getDisplayName() : null)
            .stampsToNextLevel(user.getStampsToNextLevel())
            .build();
    }

    /**
     * 실천일기 작성 스탬프
     */
    public StampEarnedResponse earnDiaryWriteStamp(User user, Long diaryId) {
        return earnStamp(user, StampType.DIARY_WRITE, "DIARY", diaryId);
    }

    /**
     * 실천일기 좋아요 받기 스탬프
     */
    public StampEarnedResponse earnDiaryLikeStamp(User user, Long diaryId) {
        return earnStamp(user, StampType.DIARY_LIKE_RECEIVED, "DIARY_LIKE", diaryId);
    }

    /**
     * 투표 참여 스탬프
     */
    public StampEarnedResponse earnVoteStamp(User user, Long voteId, String voteType) {
        return earnStamp(user, StampType.VOTE_PARTICIPATE, voteType, voteId);
    }

    /**
     * 사용자 레벨 정보 조회 (기본)
     */
    @Transactional(readOnly = true)
    public UserLevelResponse getUserLevelInfo(User user) {
        List<UserStamp> recentStamps = userStampRepository.findTop10ByUserOrderByEarnedAtDesc(user);
        List<UserLevelHistory> levelHistory = userLevelHistoryRepository.findTop5ByUserOrderByLevelUpAtDesc(user);

        return UserLevelResponse.builder()
            .userName(user.getName())
            .totalStamps(user.getTotalStamps())
            .currentLevel(user.getCurrentLevel())
            .currentLevelDisplay(user.getCurrentLevel().getDisplayName())
            .stampsToNextLevel(user.getStampsToNextLevel())
            .isMaxLevel(user.getCurrentLevel() == UserLevel.LEVEL_5)
            .recentStamps(convertToStampResponseList(recentStamps))
            .levelHistory(convertToLevelHistoryResponseList(levelHistory))
            .build();
    }

    /**
     * 사용자 레벨 정보 조회 (상세)
     */
    @Transactional(readOnly = true)
    public UserLevelResponse getUserLevelInfoDetailed(User user) {
        List<UserStamp> recentStamps = userStampRepository.findTop20ByUserOrderByEarnedAtDesc(user);
        List<UserLevelHistory> levelHistory = userLevelHistoryRepository.findTop10ByUserOrderByLevelUpAtDesc(user);
        
        // 스탬프 통계 계산
        StampStatisticsResponse statistics = calculateStampStatistics(user);
        
        // 일자별 스탬프 히스토리 생성
        List<DailyStampHistoryResponse> dailyHistory = createDailyStampHistory(user);

        return UserLevelResponse.builder()
            .userName(user.getName())
            .totalStamps(user.getTotalStamps())
            .currentLevel(user.getCurrentLevel())
            .currentLevelDisplay(user.getCurrentLevel().getDisplayName())
            .stampsToNextLevel(user.getStampsToNextLevel())
            .isMaxLevel(user.getCurrentLevel() == UserLevel.LEVEL_5)
            .recentStamps(convertToStampResponseList(recentStamps))
            .levelHistory(convertToLevelHistoryResponseList(levelHistory))
            .stampStatistics(statistics)
            .dailyStampHistory(dailyHistory)
            .build();
    }

    /**
     * 마이페이지 전체 정보 조회
     */
    @Transactional(readOnly = true)
    public MyPageResponse getMyPageInfo(User user) {
        return MyPageResponse.builder()
            .userId(user.getUserId())
            .maskedPassword(maskPassword(user.getPassword()))
            .currentLevel(user.getCurrentLevel())
            .currentLevelDisplay(user.getCurrentLevel().getDisplayName())
            .totalStamps(user.getTotalStamps())
            .stampsToNextLevel(user.getStampsToNextLevel())
            .citizenType(user.getCitizenType() != null ? user.getCitizenType() : "미진단")
            .isMaxLevel(user.getCurrentLevel() == UserLevel.LEVEL_5)
            .build();
    }

    /**
     * 중복 스탬프 체크
     */
    private boolean isDuplicateStamp(User user, StampType stampType, String relatedEntityType, Long relatedEntityId) {
        if (relatedEntityType == null || relatedEntityId == null) {
            return false; // 관련 엔티티가 없는 경우 중복 체크 하지 않음
        }
        
        return userStampRepository.existsByUserAndStampTypeAndRelatedEntityTypeAndRelatedEntityId(
            user, stampType, relatedEntityType, relatedEntityId);
    }

    /**
     * 레벨업 히스토리 저장
     */
    private void saveLevelUpHistory(User user, UserLevel previousLevel) {
        UserLevelHistory history = UserLevelHistory.builder()
            .user(user)
            .previousLevel(previousLevel)
            .newLevel(user.getCurrentLevel())
            .stampCount(user.getTotalStamps())
            .build();

        userLevelHistoryRepository.save(history);
    }

    /**
     * UserStamp -> UserStampResponse 변환
     */
    private List<UserStampResponse> convertToStampResponseList(List<UserStamp> stamps) {
        return stamps.stream()
            .map(stamp -> UserStampResponse.builder()
                .id(stamp.getId())
                .stampType(stamp.getStampType())
                .stampTypeDescription(stamp.getStampType().getDescription())
                .stampCount(stamp.getStampCount())
                .relatedEntityType(stamp.getRelatedEntityType())
                .relatedEntityId(stamp.getRelatedEntityId())
                .description(stamp.getDescription())
                .earnedAt(stamp.getEarnedAt())
                .build())
            .collect(Collectors.toList());
    }

    /**
     * UserLevelHistory -> UserLevelHistoryResponse 변환
     */
    private List<UserLevelHistoryResponse> convertToLevelHistoryResponseList(List<UserLevelHistory> histories) {
        return histories.stream()
            .map(history -> UserLevelHistoryResponse.builder()
                .id(history.getId())
                .previousLevel(history.getPreviousLevel())
                .previousLevelDisplay(history.getPreviousLevel().getDisplayName())
                .newLevel(history.getNewLevel())
                .newLevelDisplay(history.getNewLevel().getDisplayName())
                .stampCount(history.getStampCount())
                .levelUpAt(history.getLevelUpAt())
                .build())
            .collect(Collectors.toList());
    }

    /**
     * 비밀번호 마스킹 처리
     */
    private String maskPassword(String password) {
        if (password == null || password.length() < 4) {
            return "****";
        }
        
        int visibleLength = Math.min(4, password.length() - 4);
        String visiblePart = password.substring(0, visibleLength);
        String maskedPart = "*".repeat(password.length() - visibleLength);
        
        return visiblePart + maskedPart;
    }

    /**
     * 스탬프 통계 계산
     */
    private StampStatisticsResponse calculateStampStatistics(User user) {
        List<UserStamp> allStamps = userStampRepository.findByUserOrderByEarnedAtDesc(user);
        
        int diaryWriteCount = 0;
        int diaryLikeCount = 0;
        int voteCount = 0;
        
        for (UserStamp stamp : allStamps) {
            switch (stamp.getStampType()) {
                case DIARY_WRITE -> diaryWriteCount++;
                case DIARY_LIKE_RECEIVED -> diaryLikeCount++;
                case VOTE_PARTICIPATE -> voteCount++;
            }
        }
        
        return StampStatisticsResponse.builder()
            .totalStamps(user.getTotalStamps())
            .diaryWriteStamps(diaryWriteCount)
            .diaryLikeStamps(diaryLikeCount)
            .voteStamps(voteCount)
            .build();
    }

    /**
     * 일자별 스탬프 히스토리 생성 (최근 30일)
     */
    private List<DailyStampHistoryResponse> createDailyStampHistory(User user) {
        List<UserStamp> stamps = userStampRepository.findByUserAndEarnedAtAfterOrderByEarnedAtDesc(
            user, LocalDateTime.now().minusDays(30));
        
        Map<LocalDate, List<UserStamp>> groupedByDate = stamps.stream()
            .collect(Collectors.groupingBy(
                stamp -> stamp.getEarnedAt().toLocalDate(),
                LinkedHashMap::new,
                Collectors.toList()
            ));
        
        return groupedByDate.entrySet().stream()
            .map(entry -> DailyStampHistoryResponse.builder()
                .date(entry.getKey())
                .dailyStampCount(entry.getValue().size())
                .stamps(convertToStampResponseList(entry.getValue()))
                .build())
            .collect(Collectors.toList());
    }
}
