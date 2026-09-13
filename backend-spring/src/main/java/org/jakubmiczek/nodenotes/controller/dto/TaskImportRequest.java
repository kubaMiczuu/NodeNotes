package org.jakubmiczek.nodenotes.controller.dto;

import org.jakubmiczek.nodenotes.entity.TaskStatus;
import org.jakubmiczek.nodenotes.entity.TaskType;

import java.util.List;

public record TaskImportRequest(String title, String description, TaskStatus status, TaskType type, List<SubItemResponse> children) {}
