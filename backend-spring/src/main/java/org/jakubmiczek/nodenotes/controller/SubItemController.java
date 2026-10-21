package org.jakubmiczek.nodenotes.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.jakubmiczek.nodenotes.controller.dto.SubItemRequest;
import org.jakubmiczek.nodenotes.entity.SubItem;
import org.jakubmiczek.nodenotes.service.SubItemService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
class SubItemController {
    private final SubItemService subItemService;

    @PostMapping("/tasks/{taskId}/subitems")
    public ResponseEntity<SubItem> createSubItemParent(@Valid @RequestBody SubItemRequest subItemRequest, @PathVariable("taskId") Long taskId, Principal principal) {
        subItemService.addSubItem(subItemRequest, null, taskId, principal.getName());

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/subitems/{parentId}/children")
    public ResponseEntity<SubItem> createSubItemChild(@Valid @RequestBody SubItemRequest subItemRequest, @PathVariable("parentId") Long parentId, Principal principal) {
        subItemService.addSubItem(subItemRequest, parentId, null, principal.getName());

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PatchMapping("/subitems/{id}/text")
    public ResponseEntity<SubItem> updateSubItemText(@RequestParam String text, @PathVariable("id") Long id, Principal principal) {
        subItemService.updateSubItemText(text, id, principal.getName());

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @PatchMapping("/subitems/{id}/status")
    public ResponseEntity<SubItem> updateSubItemStatus(@RequestParam boolean isDone, @PathVariable("id") Long id, Principal principal) {
        subItemService.updateSubItemStatus(isDone, id, principal.getName());

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }


    @DeleteMapping("/subitems/{id}")
    public ResponseEntity<SubItem> deleteSubItem(@PathVariable("id") Long id, Principal principal) {
        subItemService.deleteSubItem(id, principal.getName());

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
