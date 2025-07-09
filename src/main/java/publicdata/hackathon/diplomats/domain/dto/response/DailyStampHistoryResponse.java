package publicdata.hackathon.diplomats.domain.dto.response;

import java.time.LocalDate;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DailyStampHistoryResponse {
    
    private LocalDate date;
    private Integer dailyStampCount;
    private List<UserStampResponse> stamps;
}
