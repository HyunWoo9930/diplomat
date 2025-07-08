package publicdata.hackathon.diplomats.domain.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import publicdata.hackathon.diplomats.domain.enums.DiscussCommentType;

@Entity
@Data
@NoArgsConstructor
public class DiscussBoardComment {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String content;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;

	@Enumerated(EnumType.STRING)
	private DiscussCommentType commentType;

	@ManyToOne
	@JoinColumn(name = "user_id")
	private User user;

	@ManyToOne
	@JoinColumn(name = "discuss_board_id")
	private DiscussBoard discussBoard;

	@Builder
	public DiscussBoardComment(DiscussBoard discussBoard, User user, String content, DiscussCommentType commentType) {
		this.discussBoard = discussBoard;
		this.user = user;
		this.content = content;
		this.commentType = commentType;
		this.createdAt = LocalDateTime.now();
		this.updatedAt = LocalDateTime.now();
	}
}
