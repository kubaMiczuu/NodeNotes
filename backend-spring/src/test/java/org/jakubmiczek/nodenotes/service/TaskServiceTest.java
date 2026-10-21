package org.jakubmiczek.nodenotes.service;

import org.jakubmiczek.nodenotes.controller.dto.*;
import org.jakubmiczek.nodenotes.entity.*;
import org.jakubmiczek.nodenotes.exception.TaskAccessDeniedException;
import org.jakubmiczek.nodenotes.exception.TaskDoesNotExistException;
import org.jakubmiczek.nodenotes.exception.UserDoesNotExistException;
import org.jakubmiczek.nodenotes.repository.TaskRepository;
import org.jakubmiczek.nodenotes.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class TaskServiceTest {

    @Mock
    TaskRepository taskRepository;

    @Mock
    UserRepository userRepository;

    @InjectMocks
    TaskService taskService;

    private User createUser() {
        User user = new User();
        user.setUserId(1L);
        user.setUsername("username");
        user.setPassword("password");

        return user;
    }

    private Task createTask(TaskType type, User user) {
        String description = type == TaskType.NOTE ? "test task" : "";

        Task task = new Task();
        task.setTaskId(1L);
        task.setTitle("task");
        task.setDescription(description);
        task.setStatus(TaskStatus.TODO);
        task.setType(type);
        task.setUser(user);

        return task;
    }

    @Test
    void shouldAddTaskCorrectly() {
        User user = createUser();

        TaskRequest taskRequest = new TaskRequest("task", "test task", TaskType.NOTE);

        when(userRepository.findByUsername("username")).thenReturn(Optional.of(user));

        taskService.addTask(taskRequest, "username");

        ArgumentCaptor<Task> captor = ArgumentCaptor.forClass(Task.class);

        verify(taskRepository).save(captor.capture());

        assertThat(captor.getValue().getTitle()).isEqualTo("task");
        assertThat(captor.getValue().getDescription()).isEqualTo("test task");
        assertThat(captor.getValue().getStatus()).isEqualTo(TaskStatus.TODO);
        assertThat(captor.getValue().getType()).isEqualTo(TaskType.NOTE);
        assertThat(captor.getValue().getUser()).isEqualTo(user);
    }

    @Test
    void shouldThrowUserDoesNotExistExceptionWhenAddingTask() {
        when(userRepository.findByUsername("username")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> taskService.addTask(new TaskRequest("task", "test task", TaskType.NOTE),"username"))
                .isInstanceOf(UserDoesNotExistException.class);
    }

    @Test
    void shouldUpdateTaskCorrectly() {
        User user = createUser();
        Task task = createTask(TaskType.NOTE, user);

        TaskUpdateRequest taskUpdateRequest = new TaskUpdateRequest("updated task", "updated test task", TaskStatus.DONE);

        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
        taskService.updateTask(taskUpdateRequest, 1L, "username");

        assertThat(task.getTitle()).isEqualTo("updated task");
        assertThat(task.getDescription()).isEqualTo("updated test task");
        assertThat(task.getStatus()).isEqualTo(TaskStatus.DONE);
    }

    @Test
    void shouldNotUpdateStatusWhenTaskIsTree() {
        User user = createUser();
        Task task = createTask(TaskType.TREE, user);

        TaskUpdateRequest taskUpdateRequest = new TaskUpdateRequest("updated task", "updated test task", TaskStatus.DONE);

        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
        taskService.updateTask(taskUpdateRequest, 1L, "username");

        assertThat(task.getStatus()).isEqualTo(TaskStatus.TODO);
    }

    @Test
    void shouldThrowTaskDoesNotExistExceptionWhenUpdatingTask() {
        TaskUpdateRequest taskUpdateRequest = new TaskUpdateRequest("task", "test task", TaskStatus.DONE);

        when(taskRepository.findById(1L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> taskService.updateTask(taskUpdateRequest, 1L, "username"))
                .isInstanceOf(TaskDoesNotExistException.class);
    }

    @Test
    void shouldThrowTaskAccessDeniedExceptionWhenUpdatingTask() {
        User user = createUser();
        Task task = createTask(TaskType.NOTE, user);

        TaskUpdateRequest taskUpdateRequest = new TaskUpdateRequest("updated task", "updated test task", TaskStatus.DONE);

        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
        assertThatThrownBy(() -> taskService.updateTask(taskUpdateRequest, 1L, "user"))
                .isInstanceOf(TaskAccessDeniedException.class);
    }

    @Test
    void shouldDeleteTaskCorrectly() {
        User user = createUser();
        Task task = createTask(TaskType.NOTE, user);

        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
        taskService.deleteTask(1L, "username");

        verify(taskRepository).delete(task);
    }

    @Test
    void shouldThrowTaskDoesNotExistExceptionWhenDeletingTask() {
        when(taskRepository.findById(1L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> taskService.deleteTask(1L, "username"))
                .isInstanceOf(TaskDoesNotExistException.class);
    }

    @Test
    void shouldThrowTaskAccessDeniedExceptionWhenDeletingTask() {
        User user = createUser();
        Task task = createTask(TaskType.NOTE, user);

        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
        assertThatThrownBy(() -> taskService.deleteTask(1L, "user"))
                .isInstanceOf(TaskAccessDeniedException.class);
    }

    @Test
    void shouldImportAllTasksCorrectly() {
        User user = createUser();
        SubItemResponse childDto = new SubItemResponse(null, "child item", false, List.of(), null, null);
        SubItemResponse parentDto = new SubItemResponse(null, "parent item", false, List.of(childDto), null, null);
        TaskImportRequest importRequest = new TaskImportRequest("Imported Task","", TaskStatus.TODO, TaskType.TREE, List.of(parentDto));

        when(userRepository.findByUsername("username")).thenReturn(Optional.of(user));
        taskService.importAllTasks(List.of(importRequest), "username");

        ArgumentCaptor<Task> captor = ArgumentCaptor.forClass(Task.class);
        verify(taskRepository).save(captor.capture());
        Task savedTask = captor.getValue();
        SubItem parentEntity = savedTask.getItems().getFirst();
        SubItem childEntity = parentEntity.getChildren().getFirst();

        assertThat(savedTask.getTitle()).isEqualTo("Imported Task");
        assertThat(savedTask.getUser()).isEqualTo(user);
        assertThat(savedTask.getItems().size()).isEqualTo(1);
        assertThat(parentEntity.getText()).isEqualTo("parent item");
        assertThat(parentEntity.getChildren().size()).isEqualTo(1);
        assertThat(childEntity.getText()).isEqualTo("child item");
        assertThat(childEntity.getParent()).isEqualTo(parentEntity);
    }

    @Test
    void shouldThrowUserDoesNotExistExceptionWhenImportingTasks() {
        TaskImportRequest importRequest = new TaskImportRequest("task", "", TaskStatus.TODO, TaskType.NOTE, null);

        when(userRepository.findByUsername("username")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> taskService.importAllTasks(List.of(importRequest), "username"))
                .isInstanceOf(UserDoesNotExistException.class);

    }

    @Test
    void shouldReturnDesiredTasks() {
        User user = createUser();
        Task task = createTask(TaskType.TREE, user);

        Pageable pageable = PageRequest.of(0, 6);
        Page<Task> pagedTasks = new PageImpl<>(List.of(task), pageable, 1);

        when(taskRepository.findTaskWithFilters("username", task.getStatus(), task.getType(), task.getTitle(), pageable)).thenReturn(pagedTasks);
        Page<TaskResponse> userTasks = taskService.getTasks("username", task.getStatus(), task.getType(), task.getTitle(), pageable);

        assertThat(userTasks.getContent().size()).isEqualTo(1);
        assertThat(userTasks.getContent().getFirst().id()).isEqualTo(1L);
        assertThat(userTasks.getContent().getFirst().title()).isEqualTo(task.getTitle());
        assertThat(userTasks.getContent().getFirst().status()).isEqualTo(task.getStatus());
        assertThat(userTasks.getContent().getFirst().type()).isEqualTo(task.getType());
    }

    @Test
    void shouldReturnTaskById() {
        User user = createUser();
        Task task = createTask(TaskType.TREE, user);

        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
        TaskResponse userTask = taskService.getTaskById(1L, "username");

        assertThat(userTask.id()).isEqualTo(task.getTaskId());
        assertThat(userTask.title()).isEqualTo(task.getTitle());
    }

    @Test
    void shouldThrowTaskDoesNotExistExceptionWhenSearchingById() {
        when(taskRepository.findById(1L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> taskService.getTaskById(1L, "username"))
                .isInstanceOf(TaskDoesNotExistException.class);
    }

    @Test
    void shouldThrowTaskAccessDeniedExceptionWhenSearchingById() {
        User user = createUser();
        Task task = createTask(TaskType.TREE, user);

        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
        assertThatThrownBy(() -> taskService.getTaskById(1L, "user"))
                .isInstanceOf(TaskAccessDeniedException.class);
    }


    @Test
    void shouldReturnAllTasksByUser() {
        User user = createUser();
        Task task = createTask(TaskType.TREE, user);

        SubItem parentItem = new SubItem();
        parentItem.setSubItemId(1L);
        parentItem.setText("parent item");
        parentItem.setDone(false);
        parentItem.setTask(task);

        SubItem childItem = new SubItem();
        childItem.setSubItemId(2L);
        childItem.setText("child item");
        childItem.setDone(false);
        childItem.setParent(parentItem);
        childItem.setTask(task);

        parentItem.setChildren(List.of(childItem));
        task.setItems(List.of(parentItem));

        when(taskRepository.findAllByUser_Username("username")).thenReturn(List.of(task));

        List<TaskResponse> userTasks = taskService.getAllTasks("username");

        assertThat(userTasks.size()).isEqualTo(1);
        assertThat(userTasks.getFirst().id()).isEqualTo(1L);
        assertThat(userTasks.getFirst().title()).isEqualTo("task");

        List<SubItemResponse> mappedRoots = userTasks.getFirst().children();
        assertThat(mappedRoots.size()).isEqualTo(1);
        assertThat(mappedRoots.getFirst().text()).isEqualTo("parent item");

        assertThat(mappedRoots.getFirst().children().size()).isEqualTo(1);
        assertThat(mappedRoots.getFirst().children().getFirst().text()).isEqualTo("child item");}
}
