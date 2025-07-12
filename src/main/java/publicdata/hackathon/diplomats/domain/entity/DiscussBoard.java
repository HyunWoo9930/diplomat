package publicdata.hackathon.diplomats.domain.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import lombok.Builder;
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
	private int viewCount;

	private DiscussType discussType;

	@ManyToOne
	@JoinColumn(name = "user_id")
	private User user;
	
	@OneToMany(mappedBy = "discussBoard", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<DiscussBoardImage> images = new ArrayList<>();
	
	@OneToMany(mappedBy = "discussBoard", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<DiscussBoardComment> comments = new ArrayList<>();
	
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;

	@Builder
	public DiscussBoard(String title, String content, DiscussType discussType, User user) {
		this.title = title;
		this.content = content;
		this.discussType = discussType;
		this.user = user;
		this.createdAt = LocalDateTime.now();
		this.updatedAt = LocalDateTime.now();
		this.likes = 0;
		this.viewCount = 0;
		this.images = new ArrayList<>();
		this.comments = new ArrayList<>();
	}
}
