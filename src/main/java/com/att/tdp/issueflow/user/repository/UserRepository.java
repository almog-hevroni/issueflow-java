package com.att.tdp.issueflow.user.repository;

import com.att.tdp.issueflow.user.entity.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

	Optional<User> findByUsername(String username);

	Optional<User> findByEmailIgnoreCase(String email);
}
