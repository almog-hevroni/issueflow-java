package com.att.tdp.issueflow.project.repository;

import com.att.tdp.issueflow.project.entity.ProjectMember;
import com.att.tdp.issueflow.project.entity.ProjectMemberId;
import com.att.tdp.issueflow.user.entity.User;
import com.att.tdp.issueflow.user.enums.Role;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProjectMemberRepository extends JpaRepository<ProjectMember, ProjectMemberId> {

	boolean existsByProject_IdAndUser_Id(Long projectId, Long userId);

	@Query("""
			select pm.user
			from ProjectMember pm
			where pm.project.id = :projectId
			order by pm.user.createdAt asc, pm.user.id asc
			""")
	List<User> findProjectUsersByProjectIdOrderByUserCreatedAtAsc(
			@Param("projectId") Long projectId
	);

	@Query("""
			select pm.user
			from ProjectMember pm
			where pm.project.id = :projectId
			  and pm.user.role = :role
			order by pm.user.createdAt asc, pm.user.id asc
			""")
	List<User> findProjectUsersByProjectIdAndRoleOrderByUserCreatedAtAsc(
			@Param("projectId") Long projectId,
			@Param("role") Role role
	);
}
