package publicdata.hackathon.diplomats.domain.enums;

import lombok.Getter;

@Getter
public enum StampType {
    DIARY_WRITE("실천일지 작성", 1),
    VOTE("투표", 1),
    LIKE("좋아요", 1);

    private final String description;
    private final int stampCount;

    StampType(String description, int stampCount) {
        this.description = description;
        this.stampCount = stampCount;
    }
}
