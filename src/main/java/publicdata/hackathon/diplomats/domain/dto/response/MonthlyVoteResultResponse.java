package publicdata.hackathon.diplomats.domain.dto.response;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MonthlyVoteResultResponse {
    
    private Integer year;
    private Integer month;
    
    // 일지 투표 결과
    private MonthlyVoteResponse diaryVote;
    
    // ODA 투표 결과
    private OdaVoteResponse odaVote;
    
    // 해당 월 인기 일지 목록
    private List<DiaryResponse> topDiaries;
    
    /**
     * 성공적인 응답 생성
     */
    public static MonthlyVoteResultResponse of(Integer year, Integer month, 
                                             MonthlyVoteResponse diaryVote, 
                                             OdaVoteResponse odaVote,
                                             List<DiaryResponse> topDiaries) {
        return MonthlyVoteResultResponse.builder()
                .year(year)
                .month(month)
                .diaryVote(diaryVote)
                .odaVote(odaVote)
                .topDiaries(topDiaries)
                .build();
    }
    
    /**
     * 일지 투표만 있는 경우
     */
    public static MonthlyVoteResultResponse ofDiaryOnly(Integer year, Integer month, 
                                                      MonthlyVoteResponse diaryVote,
                                                      List<DiaryResponse> topDiaries) {
        return MonthlyVoteResultResponse.builder()
                .year(year)
                .month(month)
                .diaryVote(diaryVote)
                .odaVote(null)
                .topDiaries(topDiaries)
                .build();
    }
    
    /**
     * ODA 투표만 있는 경우
     */
    public static MonthlyVoteResultResponse ofOdaOnly(Integer year, Integer month, 
                                                    OdaVoteResponse odaVote,
                                                    List<DiaryResponse> topDiaries) {
        return MonthlyVoteResultResponse.builder()
                .year(year)
                .month(month)
                .diaryVote(null)
                .odaVote(odaVote)
                .topDiaries(topDiaries)
                .build();
    }
    
    /**
     * 투표 결과 없이 인기 일지만 있는 경우
     */
    public static MonthlyVoteResultResponse ofDiariesOnly(Integer year, Integer month, 
                                                        List<DiaryResponse> topDiaries) {
        return MonthlyVoteResultResponse.builder()
                .year(year)
                .month(month)
                .diaryVote(null)
                .odaVote(null)
                .topDiaries(topDiaries)
                .build();
    }
}
