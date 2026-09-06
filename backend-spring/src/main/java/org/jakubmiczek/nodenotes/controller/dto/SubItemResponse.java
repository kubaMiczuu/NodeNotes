package org.jakubmiczek.nodenotes.controller.dto;

import java.util.List;

public record SubItemResponse(Long id, String text, boolean isDone, List<SubItemResponse> children, Long parentId, Long taskId) {
}
