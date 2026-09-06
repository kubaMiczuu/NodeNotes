package org.jakubmiczek.nodenotes.controller.dto;

import jakarta.validation.constraints.NotBlank;
import org.jakubmiczek.nodenotes.entity.TaskStatus;

public record TaskUpdateRequest(@NotBlank String title, String description, TaskStatus status) {
}
