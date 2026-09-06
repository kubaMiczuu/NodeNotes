package org.jakubmiczek.nodenotes.service;

import org.jakubmiczek.nodenotes.controller.dto.TaskRequest;
import org.jakubmiczek.nodenotes.controller.dto.TaskUpdateRequest;
import org.jakubmiczek.nodenotes.entity.Task;
import org.jakubmiczek.nodenotes.entity.TaskStatus;
import org.jakubmiczek.nodenotes.entity.User;
import org.jakubmiczek.nodenotes.exception.TaskAccessDeniedException;
import org.jakubmiczek.nodenotes.exception.TaskDoesNotExistException;
import org.jakubmiczek.nodenotes.exception.UserDoesNotExistException;
import org.jakubmiczek.nodenotes.repository.TaskRepository;
import org.jakubmiczek.nodenotes.repository.UserRepository;
import org.junit.jupiter.api.Disabled;
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
import org.jakubmiczek.nodenotes.controller.dto.TaskResponse;

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

    /*
    @Test
    void shouldAddTaskCorrectly() {
        User user = new User();
        user.setUserId(1L);
        user.setUsername("username");
        user.setPassword("password");

        TaskRequest taskRequest = new TaskRequest("task", "test task");

        when(userRepository.findByUsername("username")).thenReturn(Optional.of(user));
        String currentUsername = "username";

        taskService.addTask(taskRequest, currentUsername);

        ArgumentCaptor<Task> captor = ArgumentCaptor.forClass(Task.class);

        verify(taskRepository).save(captor.capture());

        assertThat(captor.getValue().getTitle()).isEqualTo("task");
        assertThat(captor.getValue().getDescription()).isEqualTo("test task");
        assertThat(captor.getValue().getStatus()).isEqualTo(TaskStatus.TODO);
        assertThat(captor.getValue().getUser()).isEqualTo(user);
    }

    @Test
    void shouldThrowUserDoesNotExistExceptionWhenAddingTask() {
        when(userRepository.findByUsername("username")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> taskService.addTask(new TaskRequest("task", "test task"),"username"))
                .isInstanceOf(UserDoesNotExistException.class);
    }

    @Test
    void shouldUpdateTaskCorrectly() {
        User user = new User();
        user.setUserId(1L);
        user.setUsername("username");
        user.setPassword("password");

        Task task = new Task();
        task.setTaskId(1L);
        task.setTitle("task");
        task.setDescription("test task");
        task.setStatus(TaskStatus.TODO);
        task.setUser(user);

        TaskUpdateRequest taskUpdateRequest = new TaskUpdateRequest(1L, "task1", "test task1", TaskStatus.DONE);
        String currentUsername = "username";

        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
        taskService.updateTask(taskUpdateRequest, currentUsername);

        ArgumentCaptor<Task> captor = ArgumentCaptor.forClass(Task.class);
        verify(taskRepository).save(captor.capture());

        assertThat(captor.getValue().getTitle()).isEqualTo("task1");
        assertThat(captor.getValue().getDescription()).isEqualTo("test task1");
        assertThat(captor.getValue().getStatus()).isEqualTo(TaskStatus.DONE);
    }

    @Test
    void shouldThrowTaskDoesNotExistExceptionWhenUpdatingTask() {
        TaskUpdateRequest taskUpdateRequest = new TaskUpdateRequest(1L, "task1", "test task1", TaskStatus.DONE);

        when(taskRepository.findById(1L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> taskService.updateTask(taskUpdateRequest, "user"))
                .isInstanceOf(TaskDoesNotExistException.class);
    }

    @Test
    void shouldThrowTaskAccessDeniedExceptionWhenUpdatingTask() {
        User user = new User();
        user.setUserId(1L);
        user.setUsername("user1");
        user.setPassword("password");

        Task task = new Task();
        task.setTaskId(1L);
        task.setTitle("task");
        task.setDescription("test task");
        task.setStatus(TaskStatus.TODO);
        task.setUser(user);

        TaskUpdateRequest taskUpdateRequest = new TaskUpdateRequest(1L, "task1", "test task1", TaskStatus.DONE);
        String currentUsername = "username";

        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
        assertThatThrownBy(() -> taskService.updateTask(taskUpdateRequest, currentUsername))
                .isInstanceOf(TaskAccessDeniedException.class);
    }

    @Test
    void shouldDeleteTaskCorrectly() {
        User user = new User();
        user.setUserId(1L);
        user.setUsername("user");
        user.setPassword("password");

        Task task = new Task();
        task.setTaskId(1L);
        task.setTitle("task");
        task.setDescription("test task");
        task.setStatus(TaskStatus.TODO);
        task.setUser(user);

        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
        taskService.deleteTask(1L, "user");

        verify(taskRepository).delete(task);
    }

    @Test
    void shouldThrowTaskDoesNotExistExceptionWhenDeletingTask() {
        when(taskRepository.findById(1L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> taskService.deleteTask(1L, "user"))
                .isInstanceOf(TaskDoesNotExistException.class);
    }

    @Test
    void shouldThrowTaskAccessDeniedExceptionWhenDeletingTask() {
        User user = new User();
        user.setUserId(1L);
        user.setUsername("user1");
        user.setPassword("password");

        Task task = new Task();
        task.setTaskId(1L);
        task.setTitle("task");
        task.setDescription("test task");
        task.setStatus(TaskStatus.TODO);
        task.setUser(user);

        String currentUsername = "username";

        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
        assertThatThrownBy(() -> taskService.deleteTask(1L, currentUsername))
                .isInstanceOf(TaskAccessDeniedException.class);
    }

    @Test
    void shouldReturnTasksByUserAndStatusAndTitle() {
        User user = new User();
        user.setUserId(1L);
        user.setUsername("user");

        Task task1 = new Task();
        task1.setTaskId(1L);
        task1.setTitle("hello world task");
        task1.setStatus(TaskStatus.TODO);
        task1.setUser(user);

        Pageable pageable = PageRequest.of(0, 1);
        Page<Task> pagedTasks = new PageImpl<>(List.of(task1), pageable, 1);

        when(taskRepository.findByUser_UsernameAndStatusAndTitleContainingIgnoreCase("user", TaskStatus.TODO, "world", pageable)).thenReturn(pagedTasks);
        Page<TaskResponse> userTasks = taskService.getTasks("user", TaskStatus.TODO, "world", pageable);

        assertThat(userTasks.getContent().size()).isEqualTo(1);
        assertThat(userTasks.getContent().getFirst().title()).isEqualTo("hello world task");
    }

    @Test
    void shouldReturnTasksByUserAndStatus() {
        User user = new User();
        user.setUserId(1L);
        user.setUsername("user");

        Task task1 = new Task();
        task1.setTaskId(1L);
        task1.setTitle("task");
        task1.setStatus(TaskStatus.TODO);
        task1.setUser(user);

        Pageable pageable = PageRequest.of(0, 1);
        Page<Task> pagedTasks = new PageImpl<>(List.of(task1), pageable, 1);

        when(taskRepository.findByUser_UsernameAndStatus("user", TaskStatus.TODO, pageable)).thenReturn(pagedTasks);
        Page<TaskResponse> userTasks = taskService.getTasks("user", TaskStatus.TODO, null, pageable);

        assertThat(userTasks.getContent().size()).isEqualTo(1);
        assertThat(userTasks.getContent().getFirst().title()).isEqualTo("task");
    }

    @Test
    void shouldReturnTasksByUserAndTitle() {
        User user = new User();
        user.setUserId(1L);
        user.setUsername("user");

        Task task1 = new Task();
        task1.setTaskId(1L);
        task1.setTitle("hello world task");
        task1.setStatus(TaskStatus.TODO);
        task1.setUser(user);

        Pageable pageable = PageRequest.of(0, 1);
        Page<Task> pagedTasks = new PageImpl<>(List.of(task1), pageable, 1);

        when(taskRepository.findByUser_UsernameAndTitleContainingIgnoreCase("user", "world", pageable)).thenReturn(pagedTasks);
        Page<TaskResponse> userTasks = taskService.getTasks("user", null, "world", pageable);

        assertThat(userTasks.getContent().size()).isEqualTo(1);
        assertThat(userTasks.getContent().getFirst().title()).isEqualTo("hello world task");
    }

    @Test
    void shouldReturnAllTasksByUser() {
        User user = new User();
        user.setUserId(1L);
        user.setUsername("user");

        Task task1 = new Task();
        task1.setTaskId(1L);
        task1.setTitle("task");
        task1.setStatus(TaskStatus.TODO);
        task1.setUser(user);

        Pageable pageable = PageRequest.of(0, 1);
        Page<Task> pagedTasks = new PageImpl<>(List.of(task1), pageable, 1);

        when(taskRepository.findByUser_Username("user", pageable)).thenReturn(pagedTasks);
        Page<TaskResponse> userTasks = taskService.getTasks("user", null, "", pageable);

        assertThat(userTasks.getContent().size()).isEqualTo(1);
        assertThat(userTasks.getContent().getFirst().title()).isEqualTo("task");
    }

     */

}
