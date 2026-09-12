package org.jakubmiczek.nodenotes.service;

import org.jakubmiczek.nodenotes.controller.dto.SubItemResponse;
import org.jakubmiczek.nodenotes.controller.dto.TaskRequest;
import org.jakubmiczek.nodenotes.controller.dto.TaskResponse;
import org.jakubmiczek.nodenotes.controller.dto.TaskUpdateRequest;
import org.jakubmiczek.nodenotes.entity.*;
import org.jakubmiczek.nodenotes.exception.TaskAccessDeniedException;
import org.jakubmiczek.nodenotes.exception.TaskDoesNotExistException;
import org.jakubmiczek.nodenotes.exception.UserDoesNotExistException;
import org.jakubmiczek.nodenotes.repository.TaskRepository;
import org.jakubmiczek.nodenotes.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class TaskService {

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;

    public TaskService(TaskRepository taskRepository, UserRepository userRepository) {
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
    }

    public void addTask(TaskRequest taskRequest, String currentUsername) {
        Task newTask = new Task();
        newTask.setTitle(taskRequest.title());
        newTask.setDescription(taskRequest.description());
        newTask.setType(taskRequest.type());
        newTask.setStatus(TaskStatus.TODO);
        newTask.setItems(List.of());

        User user = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new UserDoesNotExistException(currentUsername));

        newTask.setUser(user);

        taskRepository.save(newTask);
    }

    public void updateTask(TaskUpdateRequest taskUpdateRequest, Long taskId, String currentUsername) {
        Task taskToUpdate = taskRepository.findById(taskId)
                .orElseThrow(() -> new TaskDoesNotExistException(taskId));

        if(!taskToUpdate.getUser().getUsername().equals(currentUsername)) throw new TaskAccessDeniedException();

        taskToUpdate.setTitle(taskUpdateRequest.title());
        taskToUpdate.setDescription(taskUpdateRequest.description());
        taskToUpdate.setStatus(taskUpdateRequest.status());
        taskRepository.save(taskToUpdate);
    }

    public void deleteTask(Long taskId, String currentUsername) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new TaskDoesNotExistException(taskId));

        if(!task.getUser().getUsername().equals(currentUsername)) throw new TaskAccessDeniedException();

        taskRepository.delete(task);
    }
    public Page<TaskResponse> getTasks(String username, TaskStatus taskStatus, TaskType type, String title, Pageable pageable) {
        Page<Task> desiredTasks = taskRepository.findTaskWithFilters(username, taskStatus, type, title, pageable);

        return mapTaskToTaskResponse(desiredTasks);
    }

    public TaskResponse getTaskById(Long taskId, String currentUsername) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new TaskDoesNotExistException(taskId));

        if(!task.getUser().getUsername().equals(currentUsername)) throw new TaskAccessDeniedException();

        return mapSingleTaskToResponse(task);
    }

    private TaskResponse mapSingleTaskToResponse(Task task) {
        List<SubItemResponse> subItems = task.getItems() != null
                ? task.getItems().stream()
                .filter(subItem -> subItem.getParent() == null)
                .map(this::mapSubItemToSubItemResponse)
                .toList()
                : List.of();

        return new TaskResponse(
                task.getTaskId(),
                task.getTitle(),
                task.getDescription(),
                task.getStatus(),
                task.getType(),
                task.getCreatedAt(),
                task.getUpdatedAt(),
                subItems,
                task.getUser().getUsername()
        );
    }

    private Page<TaskResponse> mapTaskToTaskResponse(Page<Task> tasks) {
        return tasks.map(this::mapSingleTaskToResponse);
    }

    private SubItemResponse mapSubItemToSubItemResponse(SubItem item) {
        List<SubItem> children = item.getChildren() != null ? item.getChildren() : List.of();

        List<SubItemResponse> mappedChildren = children.stream().
                map(this::mapSubItemToSubItemResponse)
                .toList();

        Long parentId = item.getParent() != null ? item.getParent().getSubItemId() : null;

        return new SubItemResponse(
                item.getSubItemId(),
                item.getText(),
                item.isDone(),
                mappedChildren,
                parentId,
                item.getTask().getTaskId()
        );
    }
}
