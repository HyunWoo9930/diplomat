package publicdata.hackathon.diplomats.domain.dto.response;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import publicdata.hackathon.diplomats.domain.enums.StampType;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserStampResponse {
    private Long id;
    private StampType stampType;
    private String stampTypeDescription;
    private Integer stampCount;
    private String relatedEntityType;
    private Long relatedEntityId;
    private String description;
    private LocalDateTime earnedAt;
}
