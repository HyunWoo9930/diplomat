package publicdata.hackathon.diplomats.domain.entity;

import java.time.LocalDateTime;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
public class Question {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String content;
	private Integer questionOrder;
	private LocalDateTime createdAt;

	@OneToMany(mappedBy = "question", cascade = CascadeType.ALL)
	@OrderBy("optionOrder ASC")
	private List<QuestionOption> options;
}