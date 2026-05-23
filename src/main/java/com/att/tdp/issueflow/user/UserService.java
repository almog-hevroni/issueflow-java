package com.att.tdp.issueflow.user;

import com.att.tdp.issueflow.audit.AuditService;
import com.att.tdp.issueflow.audit.enums.AuditAction;
import com.att.tdp.issueflow.common.exception.ConflictException;
import com.att.tdp.issueflow.common.exception.NotFoundException;
import com.att.tdp.issueflow.user.dto.CreateUserRequest;
import com.att.tdp.issueflow.user.dto.UpdateUserRequest;
import com.att.tdp.issueflow.user.dto.UserResponse;
import com.att.tdp.issueflow.user.entity.User;
import com.att.tdp.issueflow.user.repository.UserRepository;
import java.util.List;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

	private static final String DEFAULT_PASSWORD = "Password123!";
	private static final String ENTITY_TYPE = "USER";

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final AuditService auditService;

	public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, AuditService auditService) {
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
		this.auditService = auditService;
	}

	@Transactional(readOnly = true)
	public List<UserResponse> getAllUsers() {
		return userRepository.findAll().stream().map(this::toResponse).toList();
	}

	@Transactional(readOnly = true)
	public UserResponse getUserById(Long userId) {
		User user = findById(userId);
		return toResponse(user);
	}

	@Transactional
	public UserResponse createUser(CreateUserRequest request) {
		if (userRepository.existsByUsernameIgnoreCase(request.username())) {
			throw new ConflictException("Username already exists: " + request.username());
		}
		if (userRepository.existsByEmailIgnoreCase(request.email())) {
			throw new ConflictException("Email already exists: " + request.email());
		}

		User user = new User();
		user.setUsername(request.username());
		user.setEmail(request.email());
		user.setFullName(request.fullName());
		user.setRole(request.role());
		user.setPasswordHash(passwordEncoder.encode(DEFAULT_PASSWORD));

		User saved = userRepository.save(user);
		auditService.recordUserAction(AuditAction.CREATE, ENTITY_TYPE, saved.getId(), "{\"source\":\"users-api\"}");
		return toResponse(saved);
	}

	@Transactional
	public void updateUser(Long userId, UpdateUserRequest request) {
		User user = findById(userId);
		user.setFullName(request.fullName());
		user.setRole(request.role());
		userRepository.save(user);
		auditService.recordUserAction(AuditAction.UPDATE, ENTITY_TYPE, userId, "{\"source\":\"users-api\"}");
	}

	@Transactional
	public void deleteUser(Long userId) {
		User user = findById(userId);
		userRepository.delete(user);
		auditService.recordUserAction(AuditAction.DELETE, ENTITY_TYPE, userId, "{\"source\":\"users-api\"}");
	}

	private User findById(Long userId) {
		return userRepository.findById(userId)
				.orElseThrow(() -> new NotFoundException("User not found: " + userId));
	}

	private UserResponse toResponse(User user) {
		return new UserResponse(
				user.getId(),
				user.getUsername(),
				user.getEmail(),
				user.getFullName(),
				user.getRole()
		);
	}
}
