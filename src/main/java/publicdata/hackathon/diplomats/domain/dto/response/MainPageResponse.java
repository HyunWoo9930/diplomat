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
public class MainPageResponse {
    
    // 외교일지 최신순 3개
    private List<DiaryResponse> recentDiaries;
    
    // 외교뉴스 최신순 3개  
    private List<NewsResponse> recentNews;
    
    // 커뮤니티 인기글 3개
    private List<CommunityPostResponse> popularCommunityPosts;
}
