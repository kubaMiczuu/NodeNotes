package org.jakubmiczek.nodenotes.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.jakubmiczek.nodenotes.controller.dto.SubItemResponse;
import org.jakubmiczek.nodenotes.controller.dto.TaskImportRequest;
import org.jakubmiczek.nodenotes.controller.dto.TaskRequest;
import org.jakubmiczek.nodenotes.controller.dto.TaskUpdateRequest;
import org.jakubmiczek.nodenotes.entity.Task;
import org.jakubmiczek.nodenotes.entity.TaskStatus;
import org.jakubmiczek.nodenotes.entity.TaskType;
import org.jakubmiczek.nodenotes.entity.User;
import org.jakubmiczek.nodenotes.repository.TaskRepository;
import org.jakubmiczek.nodenotes.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class TaskControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private UserRepository userRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private User createUser() {
        User user = new User();
        user.setUsername("TestUser");
        user.setPassword("password");
        userRepository.save(user);
        return user;
    }

    private Task createTask(User user, String title, TaskStatus status, TaskType type) {
        Task task = new Task();
        task.setTitle(title);
        task.setDescription("");
        task.setStatus(status);
        task.setType(type);
        task.setUser(user);
        return taskRepository.save(task);
    }

    @Test
    @WithMockUser(username = "TestUser")
    void shouldCreateNewTaskAndPersistInDatabase() throws Exception {
        createUser();
        TaskRequest taskRequest = new TaskRequest("Test Task", "", TaskType.TREE);

        mockMvc.perform(post("/api/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(taskRequest)))
                .andExpect(status().isCreated());

        List<Task> userTasks = taskRepository.findAllByUser_Username("TestUser");
        assertThat(userTasks).hasSize(1);
        assertThat(userTasks.getFirst().getTitle()).isEqualTo("Test Task");
    }

    @Test
    @WithMockUser(username = "TestUser")
    void shouldImportTaskAndPersistInDatabase() throws Exception {
        createUser();
        SubItemResponse child = new SubItemResponse(null, "subitem", false, List.of(), null, null);
        TaskImportRequest taskImportRequest = new TaskImportRequest("Test Task", "", TaskStatus.IN_PROGRESS, TaskType.TREE, List.of(child));

        mockMvc.perform(post("/api/tasks/import")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(taskImportRequest)))
                .andExpect(status().isCreated());

        List<Task> userTasks = taskRepository.findAllByUser_Username("TestUser");
        assertThat(userTasks).hasSize(1);
        assertThat(userTasks.getFirst().getItems()).hasSize(1);
        assertThat(userTasks.getFirst().getTitle()).isEqualTo("Test Task");
    }

    @Test
    @WithMockUser(username = "TestUser")
    void shouldImportAllTasksAndPersistInDatabase() throws Exception {
        createUser();
        SubItemResponse child = new SubItemResponse(null, "subitem", false, List.of(), null, null);
        TaskImportRequest taskImportRequest1 = new TaskImportRequest("Test Task 1", "", TaskStatus.IN_PROGRESS, TaskType.TREE, List.of(child));
        TaskImportRequest taskImportRequest2 = new TaskImportRequest("Test Task 2", "", TaskStatus.DONE, TaskType.TREE, List.of());

        mockMvc.perform(post("/api/tasks/import/all")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(List.of(taskImportRequest1, taskImportRequest2))))
                .andExpect(status().isCreated());

        List<Task> userTasks = taskRepository.findAllByUser_Username("TestUser");
        assertThat(userTasks).hasSize(2);
        assertThat(userTasks.getFirst().getItems()).hasSize(1);
        assertThat(userTasks.getFirst().getTitle()).isEqualTo("Test Task 1");
        assertThat(userTasks.getLast().getTitle()).isEqualTo("Test Task 2");
    }

    @Test
    @WithMockUser(username = "TestUser")
    void shouldUpdateTaskAndPersistInDatabase() throws Exception {
        User user = createUser();
        Task task = createTask(user, "Test Task", TaskStatus.TODO, TaskType.NOTE);
        TaskUpdateRequest taskUpdateRequest = new TaskUpdateRequest("New Test Title", "", TaskStatus.DONE);

        mockMvc.perform(put("/api/tasks/"+task.getTaskId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(taskUpdateRequest)))
                .andExpect(status().isNoContent());

        List<Task> userTasks = taskRepository.findAllByUser_Username("TestUser");
        assertThat(userTasks.getFirst().getTitle()).isEqualTo("New Test Title");
        assertThat(userTasks.getLast().getStatus()).isEqualTo(TaskStatus.DONE);
    }

    @Test
    @WithMockUser(username = "TestUser")
    void shouldDeleteTaskAndPersistInDatabase() throws Exception {
        User user = createUser();
        Task task = createTask(user, "Test Task", TaskStatus.TODO, TaskType.TREE);

        mockMvc.perform(delete("/api/tasks/"+task.getTaskId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        List<Task> userTasks = taskRepository.findAllByUser_Username("TestUser");
        assertThat(userTasks).hasSize(0);
    }

    @Test
    @WithMockUser(username = "TestUser")
    void shouldReturnTasksWithFiltersForUser() throws Exception {
        User user = createUser();
        createTask(user, "Test Task", TaskStatus.TODO, TaskType.TREE);
        createTask(user, "Test Task", TaskStatus.TODO, TaskType.NOTE);
        createTask(user, "Test Task", TaskStatus.DONE, TaskType.TREE);

        mockMvc.perform(get("/api/tasks")
                .param("status", "TODO")
                .param("type", "TREE")
                .param("title", "Test Task"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].title").value("Test Task"));
    }

    @Test
    @WithMockUser(username = "TestUser")
    void shouldReturnTaskById() throws Exception {
        User user = createUser();
        Task task = createTask(user, "Test Task", TaskStatus.TODO, TaskType.TREE);

        mockMvc.perform(get("/api/tasks/"+task.getTaskId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Test Task"))
                .andExpect(jsonPath("$.status").value("TODO"))
                .andExpect(jsonPath("$.type").value("TREE"));
    }

    @Test
    @WithMockUser(username = "TestUser")
    void shouldExportAllTasksForUser() throws Exception {
        User user = createUser();
        createTask(user, "Test Task 1", TaskStatus.TODO, TaskType.TREE);
        createTask(user, "Test Task 2", TaskStatus.TODO, TaskType.NOTE);

        mockMvc.perform(get("/api/tasks/export/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].title").value("Test Task 1"))
                .andExpect(jsonPath("$[1].title").value("Test Task 2"));
    }
}
