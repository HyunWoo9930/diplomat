package publicdata.hackathon.diplomats.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import publicdata.hackathon.diplomats.domain.entity.DiaryComment;

@Repository
public interface DiaryCommentRepository extends JpaRepository<DiaryComment, Long> {

}
