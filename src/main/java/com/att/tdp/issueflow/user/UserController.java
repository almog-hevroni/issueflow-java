package com.att.tdp.issueflow.user;

import com.att.tdp.issueflow.user.dto.CreateUserRequest;
import com.att.tdp.issueflow.user.dto.UpdateUserRequest;
import com.att.tdp.issueflow.user.dto.UserResponse;
import com.att.tdp.issueflow.user.dto.UserMentionsResponse;
import com.att.tdp.issueflow.common.api.ApiErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")
@Tag(name = "Users", description = "User management and mentions endpoints")
public class UserController {

	private final UserService userService;
	private final MentionService mentionService;

	public UserController(UserService userService, MentionService mentionService) {
		this.userService = userService;
		this.mentionService = mentionService;
	}

	@GetMapping
	@Operation(summary = "Get all users")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Users retrieved successfully"),
			@ApiResponse(
					responseCode = "401",
					description = "Unauthorized",
					content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
			)
	})
	public ResponseEntity<List<UserResponse>> getAllUsers() {
		return ResponseEntity.ok(userService.getAllUsers());
	}

	@GetMapping("/{userId}")
	@Operation(summary = "Get user by ID")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "User retrieved successfully"),
			@ApiResponse(
					responseCode = "401",
					description = "Unauthorized",
					content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
			),
			@ApiResponse(
					responseCode = "404",
					description = "User not found",
					content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
			)
	})
	public ResponseEntity<UserResponse> getUserById(@PathVariable Long userId) {
		return ResponseEntity.ok(userService.getUserById(userId));
	}

	@GetMapping("/{userId}/mentions")
	@Operation(summary = "Get comments where user was mentioned")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Mentions retrieved successfully"),
			@ApiResponse(
					responseCode = "401",
					description = "Unauthorized",
					content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
			),
			@ApiResponse(
					responseCode = "404",
					description = "User not found",
					content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
			)
	})
	public ResponseEntity<UserMentionsResponse> getMentions(
			@PathVariable Long userId,
			@RequestParam(required = false) Integer page,
			@RequestParam(required = false) Integer pageSize
	) {
		return ResponseEntity.ok(mentionService.getMentionsForUser(userId, page, pageSize));
	}

	@PostMapping
	@Operation(summary = "Create a user")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "User created successfully"),
			@ApiResponse(
					responseCode = "400",
					description = "Invalid request body",
					content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
			),
			@ApiResponse(
					responseCode = "401",
					description = "Unauthorized",
					content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
			),
			@ApiResponse(
					responseCode = "409",
					description = "Duplicate username or email",
					content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
			)
	})
	public ResponseEntity<UserResponse> createUser(@Valid @RequestBody CreateUserRequest request) {
		return ResponseEntity.ok(userService.createUser(request));
	}

	@PostMapping("/update/{userId}")
	@Operation(summary = "Update a user")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "User updated successfully"),
			@ApiResponse(
					responseCode = "400",
					description = "Invalid request body",
					content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
			),
			@ApiResponse(
					responseCode = "401",
					description = "Unauthorized",
					content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
			),
			@ApiResponse(
					responseCode = "404",
					description = "User not found",
					content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
			)
	})
	public ResponseEntity<Void> updateUser(@PathVariable Long userId, @Valid @RequestBody UpdateUserRequest request) {
		userService.updateUser(userId, request);
		return ResponseEntity.ok().build();
	}

	@DeleteMapping("/{userId}")
	@Operation(summary = "Delete a user")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "User deleted successfully"),
			@ApiResponse(
					responseCode = "401",
					description = "Unauthorized",
					content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
			),
			@ApiResponse(
					responseCode = "404",
					description = "User not found",
					content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
			)
	})
	public ResponseEntity<Void> deleteUser(@PathVariable Long userId) {
		userService.deleteUser(userId);
		return ResponseEntity.ok().build();
	}
}
