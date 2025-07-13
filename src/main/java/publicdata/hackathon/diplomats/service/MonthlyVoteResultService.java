package publicdata.hackathon.diplomats.service;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import publicdata.hackathon.diplomats.domain.dto.response.DiaryResponse;
import publicdata.hackathon.diplomats.domain.dto.response.MonthlyVoteResponse;
import publicdata.hackathon.diplomats.domain.dto.response.MonthlyVoteResultResponse;
import publicdata.hackathon.diplomats.domain.dto.response.OdaVoteResponse;
import publicdata.hackathon.diplomats.exception.CustomException;
import publicdata.hackathon.diplomats.exception.ErrorCode;
import publicdata.hackathon.diplomats.repository.DiaryRepository;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class MonthlyVoteResultService {

    private final MonthlyVoteService monthlyVoteService;
    private final OdaVoteService odaVoteService;
    private final DiaryRepository diaryRepository;

    /**
     * 특정 월 통합 투표 결과 조회 (일지 투표 + ODA 투표 + 인기 일지)
     */
    public MonthlyVoteResultResponse getMonthlyVoteResult(Integer year, Integer month) {
        log.info("월별 통합 투표 결과 조회: year={}, month={}", year, month);
        
        try {
            // 입력값 검증
            validateYearMonth(year, month);
            
            // 해당 월 인기 일지 조회
            List<DiaryResponse> topDiaries = getTopDiariesByMonth(year, month);
            
            // 일지 투표 결과 조회
            MonthlyVoteResponse diaryVoteResult = null;
            try {
                diaryVoteResult = monthlyVoteService.getVoteResultByMonth(year, month);
                log.debug("일지 투표 결과 조회 성공: year={}, month={}", year, month);
            } catch (Exception e) {
                log.warn("일지 투표 결과 조회 실패: year={}, month={}, error={}", year, month, e.getMessage());
            }
            
            // ODA 투표 결과 조회
            OdaVoteResponse odaVoteResult = null;
            try {
                odaVoteResult = odaVoteService.getOdaVoteResultByMonth(year, month);
                log.debug("ODA 투표 결과 조회 성공: year={}, month={}", year, month);
            } catch (Exception e) {
                log.warn("ODA 투표 결과 조회 실패: year={}, month={}, error={}", year, month, e.getMessage());
            }
            
            // 통합 응답 생성
            MonthlyVoteResultResponse response;
            if (diaryVoteResult != null && odaVoteResult != null) {
                response = MonthlyVoteResultResponse.of(year, month, diaryVoteResult, odaVoteResult, topDiaries);
            } else if (diaryVoteResult != null) {
                response = MonthlyVoteResultResponse.ofDiaryOnly(year, month, diaryVoteResult, topDiaries);
            } else if (odaVoteResult != null) {
                response = MonthlyVoteResultResponse.ofOdaOnly(year, month, odaVoteResult, topDiaries);
            } else {
                response = MonthlyVoteResultResponse.ofDiariesOnly(year, month, topDiaries);
            }
            
            log.info("월별 통합 투표 결과 조회 완료: year={}, month={}, hasDiaryVote={}, hasOdaVote={}, diaryCount={}", 
                    year, month, 
                    response.getDiaryVote() != null, 
                    response.getOdaVote() != null, 
                    topDiaries.size());
            
            return response;
            
        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            log.error("월별 통합 투표 결과 조회 실패: year={}, month={}, error={}", year, month, e.getMessage(), e);
            throw new CustomException(ErrorCode.DATABASE_ERROR, "월별 투표 결과 조회 중 오류가 발생했습니다.");
        }
    }
    
    /**
     * 특정 월 인기 일지 조회
     */
    private List<DiaryResponse> getTopDiariesByMonth(Integer year, Integer month) {
        try {
            YearMonth targetMonth = YearMonth.of(year, month);
            LocalDateTime startOfMonth = targetMonth.atDay(1).atStartOfDay();
            LocalDateTime endOfMonth = targetMonth.atEndOfMonth().atTime(23, 59, 59);

            Pageable top10 = PageRequest.of(0, 10);
            List<publicdata.hackathon.diplomats.domain.entity.Diary> topDiaries = 
                    diaryRepository.findTopDiariesByMonth(startOfMonth, endOfMonth, top10);

            return topDiaries.stream()
                    .map(diary -> DiaryResponse.builder()
                            .id(diary.getId())
                            .title(diary.getTitle())
                            .description(diary.getDescription())
                            .action(diary.getAction())
                            .likes(diary.getLikes())
                            .createdAt(diary.getCreatedAt())
                            .updatedAt(diary.getUpdatedAt())
                            .userId(diary.getWriter().getUserId())
                            .build())
                    .toList();
                    
        } catch (Exception e) {
            log.error("월별 인기 일지 조회 실패: year={}, month={}, error={}", year, month, e.getMessage(), e);
            // 일지 조회 실패시 빈 리스트 반환
            return List.of();
        }
    }
    
    /**
     * 년도와 월 유효성 검증
     */
    private void validateYearMonth(Integer year, Integer month) {
        if (year == null || year < 2020 || year > 2030) {
            throw new CustomException(ErrorCode.INVALID_INPUT, "유효하지 않은 년도입니다. (2020-2030)");
        }
        
        if (month == null || month < 1 || month > 12) {
            throw new CustomException(ErrorCode.INVALID_INPUT, "유효하지 않은 월입니다. (1-12)");
        }
    }
}
