package publicdata.hackathon.diplomats.domain.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Data;
import lombok.NoArgsConstructor;
import publicdata.hackathon.diplomats.domain.enums.DiscussType;

@Entity
@Data
@NoArgsConstructor
public class DiscussBoard {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	private String title;
	private String content;
	private int likes;

	private DiscussType discussType;

	@ManyToOne
	@JoinColumn(name = "user_id")
	private User user;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;
}
