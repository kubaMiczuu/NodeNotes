package org.jakubmiczek.nodenotes.controller.dto;

import jakarta.validation.constraints.NotBlank;

public record SubItemRequest(@NotBlank String text) {
}
