package publicdata.hackathon.diplomats.domain.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@NoArgsConstructor
public class FreeBoardImage {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private String imagePath;

	private String originalFileName; // 원본 파일명
	private String savedFileName;    // 저장된 파일명 (UUID)
	private Long fileSize;           // 파일 크기 (bytes)
	private String contentType;      // MIME 타입 (image/jpeg, image/png 등)

	@Column(nullable = false)
	private Integer imageOrder;

	private LocalDateTime uploadedAt; // 업로드 시간

	@ManyToOne
	@JoinColumn(name = "free_board_id")
	private FreeBoard freeBoard;

	@Builder
	public FreeBoardImage(String imagePath, String originalFileName, String savedFileName,
		Long fileSize, String contentType, Integer imageOrder,
		FreeBoard freeBoard, LocalDateTime uploadedAt) {
		this.imagePath = imagePath;
		this.originalFileName = originalFileName;
		this.savedFileName = savedFileName;
		this.fileSize = fileSize;
		this.contentType = contentType;
		this.imageOrder = imageOrder;
		this.freeBoard = freeBoard;
		this.uploadedAt = uploadedAt;
	}
}