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

@Entity
@Data
@NoArgsConstructor
public class FreeBoard {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	private String title;
	private String content;
	private int likes;
	private int viewCount;

	@ManyToOne
	@JoinColumn(name = "user_id")
	private User user;
	
	@OneToMany(mappedBy = "freeBoard", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<FreeBoardImage> images = new ArrayList<>();
	
	@OneToMany(mappedBy = "freeBoard", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<FreeBoardComment> comments = new ArrayList<>();
	
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;

	@Builder
	public FreeBoard(User user, String title, String content) {
		this.user = user;
		this.title = title;
		this.content = content;
		this.createdAt = LocalDateTime.now();
		this.updatedAt = LocalDateTime.now();
		this.likes = 0;
		this.viewCount = 0;
		this.images = new ArrayList<>();
		this.comments = new ArrayList<>();
	}
}
