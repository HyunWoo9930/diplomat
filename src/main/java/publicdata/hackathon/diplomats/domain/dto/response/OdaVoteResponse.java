package publicdata.hackathon.diplomats.domain.dto.response;

import java.time.LocalDateTime;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import publicdata.hackathon.diplomats.domain.enums.VoteStatus;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "ODA 투표 응답 정보")
public class OdaVoteResponse {
	
	@Schema(description = "투표 ID", example = "1")
	private Long id;
	
	@Schema(description = "투표 연도", example = "2025")
	private Integer year;
	
	@Schema(description = "투표 월", example = "7")
	private Integer month;
	
	@Schema(description = "투표 제목", example = "2025년 7월 의미있는 ODA 사업 투표")
	private String title;
	
	@Schema(description = "투표 설명")
	private String description;
	
	@Schema(description = "투표 상태")
	private VoteStatus status;
	
	@Schema(description = "투표 시작일시")
	private LocalDateTime startDate;
	
	@Schema(description = "투표 종료일시")
	private LocalDateTime endDate;
	
	@Schema(description = "투표 생성일시")
	private LocalDateTime createdAt;
	
	@Schema(description = "투표 후보 목록 (5개 ODA 프로젝트)")
	private List<OdaVoteCandidateResponse> candidates;
	
	@Schema(description = "전체 투표 참여자 수", example = "1523")
	private Long totalVoteCount;
	
	@Schema(description = "현재 사용자 투표 여부", example = "true")
	private boolean hasUserVoted;
	
	@Schema(description = "현재 사용자가 투표한 후보 ID (투표하지 않은 경우 null)", example = "3")
	private Long userVotedCandidateId;
}
