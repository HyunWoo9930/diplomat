package publicdata.hackathon.diplomats.domain.enums;

import java.util.List;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum NewsFilter {
	ALL("전체", null),
	ESG("ESG", List.of("ESG", "지속가능", "환경", "사회", "거버넌스")),
	CLIMATE("기후", List.of("기후", "탄소", "녹색", "온실가스", "탄소중립", "재생에너지")),
	CULTURE("문화", List.of("문화", "예술", "한류", "K-pop", "축제", "전시", "공연")),
	ODA("ODA", List.of("ODA", "개발협력", "개발원조", "국제개발", "원조", "지원"));

	private final String displayName;
	private final List<String> keywords;
}