package publicdata.hackathon.diplomats.domain.enums;

import lombok.Getter;

@Getter
public enum StampType {
    DIARY_WRITE("실천일기 작성", 1),
    DIARY_LIKE_RECEIVED("실천일기 좋아요 받기", 1),
    VOTE_PARTICIPATE("투표 참여", 1);

    private final String description;
    private final int stampCount;

    StampType(String description, int stampCount) {
        this.description = description;
        this.stampCount = stampCount;
    }
}
