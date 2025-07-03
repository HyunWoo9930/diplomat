package publicdata.hackathon.diplomats.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import publicdata.hackathon.diplomats.domain.entity.FreeBoard;
import publicdata.hackathon.diplomats.domain.entity.FreeBoardImage;

@Repository
public interface FreeBoardImageRepository extends JpaRepository<FreeBoardImage, Long> {
	List<FreeBoardImage> findAllByFreeBoard(FreeBoard freeBoard);
}
