package org.jakubmiczek.nodenotes.controller.dto;

import jakarta.validation.constraints.NotBlank;
import org.jakubmiczek.nodenotes.entity.TaskType;

public record TaskRequest(@NotBlank String title, String description, TaskType type) {
}
