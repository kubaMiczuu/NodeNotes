package org.jakubmiczek.nodenotes.service;

import lombok.RequiredArgsConstructor;
import org.jakubmiczek.nodenotes.controller.dto.SubItemRequest;
import org.jakubmiczek.nodenotes.entity.SubItem;
import org.jakubmiczek.nodenotes.entity.Task;
import org.jakubmiczek.nodenotes.entity.TaskStatus;
import org.jakubmiczek.nodenotes.exception.SubItemDoesNotExistException;
import org.jakubmiczek.nodenotes.exception.TaskAccessDeniedException;
import org.jakubmiczek.nodenotes.exception.TaskDoesNotExistException;
import org.jakubmiczek.nodenotes.repository.SubItemRepository;
import org.jakubmiczek.nodenotes.repository.TaskRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SubItemService {

    private final SubItemRepository subItemRepository;
    private final TaskRepository taskRepository;

    @Transactional
    public void addSubItem(SubItemRequest subItemRequest, Long parentId, Long taskId, String currentUsername) {
        SubItem subItem = new SubItem();
        subItem.setText(subItemRequest.text());
        subItem.setDone(false);

        Task task;

        if(parentId != null) {
            SubItem subItemParent =  subItemRepository.findById(parentId)
                    .orElseThrow(() -> new SubItemDoesNotExistException(parentId));

            task = subItemParent.getTask();

            subItem.setParent(subItemParent);
        } else {
            subItem.setParent(null);

            task = taskRepository.findById(taskId)
                .orElseThrow(() -> new TaskDoesNotExistException(taskId));
        }

        if(task != null && !task.getUser().getUsername().equals(currentUsername)) throw new TaskAccessDeniedException();
        subItem.setTask(task);

        subItemRepository.save(subItem);
    }

    @Transactional
    public void updateSubItemText(String text, Long subItemId, String currentUsername) {
        SubItem subItem =  subItemRepository.findById(subItemId)
                .orElseThrow(() -> new SubItemDoesNotExistException(subItemId));

        Task task = subItem.getTask();
        if(!task.getUser().getUsername().equals(currentUsername)) throw new TaskAccessDeniedException();

        subItem.setText(text);
    }

    @Transactional
    public void updateSubItemStatus(boolean isDone, Long subItemId, String currentUsername) {
        SubItem subItem =  subItemRepository.findById(subItemId)
                .orElseThrow(() -> new SubItemDoesNotExistException(subItemId));

        Task task = subItem.getTask();
        if(!task.getUser().getUsername().equals(currentUsername)) throw new TaskAccessDeniedException();

        subItem.setDone(!isDone);

        if(!isDone) markAllChildrenAsDone(subItem);

        task.setStatus(returnCalculatedTaskStatus(subItem.getTask()));
    }

    @Transactional
    public void deleteSubItem(Long subItemId, String currentUsername) {
        SubItem subItem =  subItemRepository.findById(subItemId)
                .orElseThrow(() -> new SubItemDoesNotExistException(subItemId));

        Task task = subItem.getTask();
        if(!task.getUser().getUsername().equals(currentUsername)) throw new TaskAccessDeniedException();

        subItemRepository.delete(subItem);
    }

    private void markAllChildrenAsDone(SubItem subItem) {
        for(SubItem child : subItem.getChildren()) {
            child.setDone(true);
            markAllChildrenAsDone(child);
        }
    }

    private TaskStatus returnCalculatedTaskStatus(Task task) {
        List<SubItem> items = task.getItems();

        int totalItems = 0;
        int doneCount = 0;

        for(SubItem subItem : items) {
            totalItems++;
            if(subItem.isDone()) doneCount++;

        }

        if(totalItems == doneCount) return TaskStatus.DONE;
        else if(totalItems == 0 || doneCount == 0) return TaskStatus.TODO;
        else return TaskStatus.IN_PROGRESS;
    }

}
