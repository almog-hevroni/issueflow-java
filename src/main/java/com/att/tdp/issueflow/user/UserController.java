package com.att.tdp.issueflow.user;

import com.att.tdp.issueflow.user.dto.CreateUserRequest;
import com.att.tdp.issueflow.user.dto.UpdateUserRequest;
import com.att.tdp.issueflow.user.dto.UserResponse;
import com.att.tdp.issueflow.comment.dto.CommentResponse;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")
public class UserController {

	private final UserService userService;
	private final MentionService mentionService;

	public UserController(UserService userService, MentionService mentionService) {
		this.userService = userService;
		this.mentionService = mentionService;
	}

	@GetMapping
	public ResponseEntity<List<UserResponse>> getAllUsers() {
		return ResponseEntity.ok(userService.getAllUsers());
	}

	@GetMapping("/{userId}")
	public ResponseEntity<UserResponse> getUserById(@PathVariable Long userId) {
		return ResponseEntity.ok(userService.getUserById(userId));
	}

	@GetMapping("/{userId}/mentions")
	public ResponseEntity<List<CommentResponse>> getMentions(@PathVariable Long userId) {
		return ResponseEntity.ok(mentionService.getMentionsForUser(userId));
	}

	@PostMapping
	public ResponseEntity<UserResponse> createUser(@Valid @RequestBody CreateUserRequest request) {
		return ResponseEntity.ok(userService.createUser(request));
	}

	@PostMapping("/update/{userId}")
	public ResponseEntity<Void> updateUser(@PathVariable Long userId, @Valid @RequestBody UpdateUserRequest request) {
		userService.updateUser(userId, request);
		return ResponseEntity.ok().build();
	}

	@DeleteMapping("/{userId}")
	public ResponseEntity<Void> deleteUser(@PathVariable Long userId) {
		userService.deleteUser(userId);
		return ResponseEntity.ok().build();
	}
}
