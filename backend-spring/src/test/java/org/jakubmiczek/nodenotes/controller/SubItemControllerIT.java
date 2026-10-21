package org.jakubmiczek.nodenotes.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import org.jakubmiczek.nodenotes.controller.dto.SubItemRequest;
import org.jakubmiczek.nodenotes.entity.*;
import org.jakubmiczek.nodenotes.repository.SubItemRepository;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class SubItemControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private SubItemRepository subItemRepository;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EntityManager entityManager;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private User createUser() {
        User user = new User();
        user.setUsername("TestUser");
        user.setPassword("password");
        userRepository.save(user);
        return user;
    }

    private Task createTask(User user) {
        Task task = new Task();
        task.setTitle("Test Task");
        task.setDescription("");
        task.setStatus(TaskStatus.TODO);
        task.setType(TaskType.TREE);
        task.setUser(user);
        return taskRepository.save(task);
    }

    private SubItem createSubItemParent(Task task, SubItem parent, String text) {
        SubItem subItem = new SubItem();
        subItem.setText(text);
        subItem.setTask(task);
        subItem.setParent(parent);
        subItem.setDone(false);
        subItem.setChildren(null);
        subItemRepository.save(subItem);
        return subItem;
    }

    @Test
    @WithMockUser(username = "TestUser")
    void shouldCreateSubItemParentAndPersistInDatabase() throws Exception {
        User user = createUser();
        Task task =  createTask(user);
        SubItemRequest request = new SubItemRequest("Test SubItem");

        mockMvc.perform(post("/api/tasks/"+task.getTaskId()+"/subitems")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
        entityManager.flush();
        entityManager.clear();

        Task userTask = taskRepository.findById(task.getTaskId()).orElseThrow();
        SubItem subItem = userTask.getItems().getFirst();

        assertThat(userTask.getItems()).hasSize(1);
        assertThat(subItem.getText()).isEqualTo("Test SubItem");
        assertThat(subItem.getParent()).isNull();
    }

    @Test
    @WithMockUser(username = "TestUser")
    void shouldCreateSubItemChildAndPersistInDatabase() throws Exception {
        User user = createUser();
        Task task =  createTask(user);
        SubItem parent = createSubItemParent(task, null, "Test SubItem Parent");
        SubItemRequest request = new SubItemRequest("Test SubItem Child");

        mockMvc.perform(post("/api/subitems/"+parent.getSubItemId()+"/children")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
        entityManager.flush();
        entityManager.clear();

        Task userTask = taskRepository.findById(task.getTaskId()).orElseThrow();
        SubItem parentSubItem = userTask.getItems().getFirst();
        SubItem childSubItem = parentSubItem.getChildren().getFirst();

        assertThat(userTask.getItems()).hasSize(2);
        assertThat(parentSubItem.getChildren()).hasSize(1);
        assertThat(childSubItem.getText()).isEqualTo("Test SubItem Child");
        assertThat(childSubItem.getParent().getSubItemId()).isEqualTo(parent.getSubItemId());
        assertThat(childSubItem.getTask().getTaskId()).isEqualTo(task.getTaskId());
    }

    @Test
    @WithMockUser(username = "TestUser")
    void shouldUpdateSubItemsTextAndPersistInDatabase() throws Exception {
        User user = createUser();
        Task task =  createTask(user);
        SubItem subItem = createSubItemParent(task, null,"Test SubItem");

        mockMvc.perform(patch("/api/subitems/"+subItem.getSubItemId()+"/text")
                .contentType(MediaType.APPLICATION_JSON)
                .param("text", "New Test SubItem"))
                .andExpect(status().isNoContent());
        entityManager.flush();
        entityManager.clear();

        Task userTask = taskRepository.findById(task.getTaskId()).orElseThrow();
        SubItem taskSubItem = userTask.getItems().getFirst();
        assertThat(taskSubItem.getText()).isEqualTo("New Test SubItem");
    }

    @Test
    @WithMockUser(username = "TestUser")
    void shouldUpdateSubItemsIsDoneAndPersistInDatabase() throws Exception {
        User user = createUser();
        Task task =  createTask(user);
        SubItem parentSubItem = createSubItemParent(task, null,"Test SubItem Parent");
        SubItem childSubItem = createSubItemParent(task, parentSubItem,"Test SubItem Child");

        entityManager.flush();
        entityManager.clear();
        mockMvc.perform(patch("/api/subitems/"+childSubItem.getSubItemId()+"/status")
                .contentType(MediaType.APPLICATION_JSON)
                .param("isDone", "false"))
                .andExpect(status().isNoContent());
        entityManager.flush();
        entityManager.clear();

        Task userTask = taskRepository.findById(task.getTaskId()).orElseThrow();
        SubItem taskParentSubItem = userTask.getItems().getFirst();
        SubItem taskChildSubItem = taskParentSubItem.getChildren().getFirst();
        assertThat(taskParentSubItem.isDone()).isTrue();
        assertThat(taskChildSubItem.isDone()).isTrue();
        assertThat(userTask.getStatus()).isEqualTo(TaskStatus.DONE);
    }

    @Test
    @WithMockUser(username = "TestUser")
    void shouldDeleteSubItemAndPersistInDatabase() throws Exception {
        User user = createUser();
        Task task =  createTask(user);
        SubItem parentSubItem = createSubItemParent(task, null,"Test SubItem Parent");
        createSubItemParent(task, parentSubItem,"Test SubItem Child");

        entityManager.flush();
        entityManager.clear();
        mockMvc.perform(delete("/api/subitems/"+parentSubItem.getSubItemId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
        entityManager.flush();
        entityManager.clear();

        Task userTask = taskRepository.findById(task.getTaskId()).orElseThrow();
        assertThat(userTask.getItems()).hasSize(0);

    }
}
