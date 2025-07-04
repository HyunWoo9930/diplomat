package publicdata.hackathon.diplomats.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import publicdata.hackathon.diplomats.domain.entity.Question;

@Repository
public interface QuestionRepository extends JpaRepository<Question, Long> {

	@Query("SELECT q FROM Question q LEFT JOIN FETCH q.options ORDER BY q.questionOrder")
	List<Question> findAllWithOptionsOrderByQuestionOrder();
}