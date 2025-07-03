package publicdata.hackathon.diplomats.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import publicdata.hackathon.diplomats.domain.entity.DiscussBoardImage;

@Repository
public interface DiscussBoardImageRepository extends JpaRepository<DiscussBoardImage, Long> {

}
