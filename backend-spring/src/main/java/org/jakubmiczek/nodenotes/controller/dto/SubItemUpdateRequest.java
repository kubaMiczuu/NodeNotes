package org.jakubmiczek.nodenotes.controller.dto;

import jakarta.validation.constraints.NotBlank;

public record SubItemUpdateRequest(@NotBlank String text, boolean isDone) {
}
