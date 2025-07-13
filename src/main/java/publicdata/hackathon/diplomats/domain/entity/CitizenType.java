package publicdata.hackathon.diplomats.domain.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "citizen_types")
@Data
@NoArgsConstructor
public class CitizenType {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String typeName;
	private String displayName;
	private String description;
	private String imageUrl;
	
	@Column(columnDefinition = "TEXT")
	private String detailedDescription;
	
	@Column(columnDefinition = "TEXT")
	private String characteristics;
	
	@Column(columnDefinition = "TEXT")
	private String keywords;
	
	@Column(columnDefinition = "TEXT")
	private String summary;
	
	private LocalDateTime createdAt;
}