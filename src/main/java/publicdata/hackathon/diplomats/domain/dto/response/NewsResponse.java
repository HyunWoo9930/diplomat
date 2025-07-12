package publicdata.hackathon.diplomats.domain.dto.response;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NewsResponse {
    
    private Long id;
    private String title;
    private String content;
    private String summary; // 요약본 (150자 제한)
    private String source; // 뉴스 출처
    private String url; // 원본 뉴스 URL
    private String imageUrl; // 뉴스 이미지
    private String category; // 뉴스 카테고리
    private boolean scrapped; // 현재 사용자의 스크랩 상태
    private LocalDateTime publishDate;
    private LocalDateTime createdAt;
    private Integer viewCount;
}
