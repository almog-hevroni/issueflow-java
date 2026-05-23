package com.att.tdp.issueflow.project.dto;

import jakarta.validation.constraints.Size;

public record UpdateProjectRequest(
		@Size(max = 200) String name,
		@Size(max = 2000) String description
) {
}
