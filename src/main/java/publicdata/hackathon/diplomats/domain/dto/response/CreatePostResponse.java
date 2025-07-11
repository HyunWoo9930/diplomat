package publicdata.hackathon.diplomats.domain.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreatePostResponse {
    private Long postId;
    private String message;
    
    public static CreatePostResponse of(Long postId, String message) {
        return CreatePostResponse.builder()
                .postId(postId)
                .message(message)
                .build();
    }
}
