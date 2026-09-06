package org.jakubmiczek.nodenotes.controller.dto;

import org.jakubmiczek.nodenotes.entity.TaskStatus;
import org.jakubmiczek.nodenotes.entity.TaskType;

import java.time.LocalDateTime;
import java.util.List;

public record TaskResponse(Long id, String title, String description, TaskStatus status, TaskType type, LocalDateTime createdAt, LocalDateTime updatedAt, List<SubItemResponse> children, String username) {
}
