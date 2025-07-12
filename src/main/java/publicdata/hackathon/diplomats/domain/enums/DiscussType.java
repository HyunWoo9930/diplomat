package publicdata.hackathon.diplomats.domain.enums;

public enum DiscussType {
	ENVIRONMENT("환경외교"), 
	CULTURE("문화외교"), 
	PEACE("평화외교"), 
	ECONOMY("경제외교");
	
	private final String displayName;
	
	DiscussType(String displayName) {
		this.displayName = displayName;
	}
	
	public String getDisplayName() {
		return displayName;
	}
}
