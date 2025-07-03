package publicdata.hackathon.diplomats.domain.dto.request;

import lombok.Data;

@Data
public class FreeBoardUpdateRequest {
    private String title;
    private String content;
}
