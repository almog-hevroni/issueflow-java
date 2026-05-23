package com.att.tdp.issueflow.project.repository;

import com.att.tdp.issueflow.project.entity.Project;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectRepository extends JpaRepository<Project, Long> {

	Optional<Project> findByIdAndDeletedAtIsNull(Long id);

	List<Project> findAllByDeletedAtIsNull();

	List<Project> findAllByDeletedAtIsNotNull();

	boolean existsByIdAndDeletedAtIsNull(Long id);
}
