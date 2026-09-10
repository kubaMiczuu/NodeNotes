package org.jakubmiczek.nodenotes.service;

import org.jakubmiczek.nodenotes.controller.dto.SubItemRequest;
import org.jakubmiczek.nodenotes.controller.dto.SubItemUpdateRequest;
import org.jakubmiczek.nodenotes.entity.SubItem;
import org.jakubmiczek.nodenotes.entity.Task;
import org.jakubmiczek.nodenotes.entity.TaskStatus;
import org.jakubmiczek.nodenotes.exception.SubItemDoesNotExistException;
import org.jakubmiczek.nodenotes.exception.TaskAccessDeniedException;
import org.jakubmiczek.nodenotes.exception.TaskDoesNotExistException;
import org.jakubmiczek.nodenotes.repository.SubItemRepository;
import org.jakubmiczek.nodenotes.repository.TaskRepository;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class SubItemService {

    private final SubItemRepository subItemRepository;
    private final TaskRepository taskRepository;

    public SubItemService(SubItemRepository subItemRepository, TaskRepository taskRepository) {
        this.subItemRepository = subItemRepository;
        this.taskRepository = taskRepository;
    }

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

    public void updateSubItem(SubItemUpdateRequest updateRequest, Long subItemId, String currentUsername) {
        SubItem subItem =  subItemRepository.findById(subItemId)
                .orElseThrow(() -> new SubItemDoesNotExistException(subItemId));

        Task task = subItem.getTask();
        if(!task.getUser().getUsername().equals(currentUsername)) throw new TaskAccessDeniedException();

        subItem.setText(updateRequest.text());
        subItem.setDone(updateRequest.isDone());

        if(updateRequest.isDone()) markAllChildrenAsDone(subItem);

        task.setStatus(updateTaskStatus(subItem.getTask()));

        subItemRepository.save(subItem);
    }

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

    private TaskStatus updateTaskStatus(Task task) {
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
