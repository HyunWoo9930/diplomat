package publicdata.hackathon.diplomats.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import publicdata.hackathon.diplomats.domain.entity.User;

public interface UserRepository extends JpaRepository<User, Long> {
	Optional<User> findByUserId(String userId);
	boolean existsByUserId(String userId);
}
