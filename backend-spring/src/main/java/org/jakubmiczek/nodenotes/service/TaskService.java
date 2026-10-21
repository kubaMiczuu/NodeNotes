package org.jakubmiczek.nodenotes.service;

import lombok.RequiredArgsConstructor;
import org.jakubmiczek.nodenotes.controller.dto.*;
import org.jakubmiczek.nodenotes.entity.*;
import org.jakubmiczek.nodenotes.exception.TaskAccessDeniedException;
import org.jakubmiczek.nodenotes.exception.TaskDoesNotExistException;
import org.jakubmiczek.nodenotes.exception.UserDoesNotExistException;
import org.jakubmiczek.nodenotes.repository.TaskRepository;
import org.jakubmiczek.nodenotes.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;


@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TaskService {

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;

    @Transactional
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

    @Transactional
    public void updateTask(TaskUpdateRequest taskUpdateRequest, Long taskId, String currentUsername) {
        Task taskToUpdate = taskRepository.findById(taskId)
                .orElseThrow(() -> new TaskDoesNotExistException(taskId));

        if(!taskToUpdate.getUser().getUsername().equals(currentUsername)) throw new TaskAccessDeniedException();

        taskToUpdate.setTitle(taskUpdateRequest.title());
        taskToUpdate.setDescription(taskUpdateRequest.description());
        if(taskToUpdate.getType() == TaskType.NOTE) {
            taskToUpdate.setStatus(taskUpdateRequest.status());
        }
    }

    @Transactional
    public void deleteTask(Long taskId, String currentUsername) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new TaskDoesNotExistException(taskId));

        if(!task.getUser().getUsername().equals(currentUsername)) throw new TaskAccessDeniedException();

        taskRepository.delete(task);
    }

    @Transactional
    public void importTask(TaskImportRequest taskImportRequest, String currentUsername) {
        Task task = new Task();
        task.setTitle(taskImportRequest.title());
        task.setDescription(taskImportRequest.description());
        task.setStatus(taskImportRequest.status());
        task.setType(taskImportRequest.type());

        List<SubItem> newTaskItems = new ArrayList<>();
        if(taskImportRequest.children() != null) {

            for(SubItemResponse child: taskImportRequest.children()) {
                SubItem rootItem = mapDtoToSubItemEntity(child, task, null);
                newTaskItems.add(rootItem);
            }

        }

        task.setItems(newTaskItems);

        User user = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new UserDoesNotExistException(currentUsername));

        task.setUser(user);

        taskRepository.save(task);

    }

    @Transactional
    public void importAllTasks(List<TaskImportRequest> taskImportRequests, String currentUsername) {
        for (TaskImportRequest importRequest : taskImportRequests) {
            importTask(importRequest, currentUsername);
        }
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

    public List<TaskResponse> getAllTasks(String currentUsername) {
        List<Task> allTasks= taskRepository.findAllByUser_Username(currentUsername);

        List<TaskResponse> taskResponses = new ArrayList<>();
        allTasks.forEach(task -> taskResponses.add(mapSingleTaskToResponse(task)));

        return taskResponses;
    }

    private SubItem mapDtoToSubItemEntity(SubItemResponse dto, Task task, SubItem parent) {
        SubItem subItem = new SubItem();
        subItem.setText(dto.text());
        subItem.setDone(dto.isDone());
        subItem.setTask(task);
        subItem.setParent(parent);

        List<SubItem> newChildrenList = new ArrayList<>();

        if(dto.children() != null) {
            for(SubItemResponse child: dto.children()) {
                SubItem mappedChild = mapDtoToSubItemEntity(child, task, subItem);

                newChildrenList.add(mappedChild);
            }
        }

        subItem.setChildren(newChildrenList);

        return subItem;
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
