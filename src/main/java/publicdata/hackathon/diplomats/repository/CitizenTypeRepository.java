package publicdata.hackathon.diplomats.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import publicdata.hackathon.diplomats.domain.entity.CitizenType;

@Repository
public interface CitizenTypeRepository extends JpaRepository<CitizenType, Long> {
	Optional<CitizenType> findByTypeName(String typeName);
}