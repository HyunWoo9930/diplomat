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
public class CommunityPostResponse {
    
    private Long id;
    private String title;
    private String content;
    private String summary; // 요약본 (150자 제한)
    private String authorName;
    private String authorId;
    private Integer likes;
    private Integer commentCount;
    private Integer viewCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String boardType; // "FREE" 또는 "DISCUSS"
    private String boardTypeName; // "자유게시판" 또는 "토론게시판"
}
