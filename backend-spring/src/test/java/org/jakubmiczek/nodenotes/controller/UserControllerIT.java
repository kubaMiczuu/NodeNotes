package org.jakubmiczek.nodenotes.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.jakubmiczek.nodenotes.controller.dto.UserInfoUpdateRequest;
import org.jakubmiczek.nodenotes.controller.dto.UserPasswordUpdateRequest;
import org.jakubmiczek.nodenotes.entity.User;
import org.jakubmiczek.nodenotes.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class UserControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private User createUser() {
        User user = new User();
        user.setUsername("TestUser");
        user.setPassword(passwordEncoder.encode("password"));
        userRepository.save(user);
        return user;
    }

    @Test
    @WithMockUser(username = "TestUser")
    void shouldUpdateUserInfoAndPersistInDatabase() throws Exception {
        createUser();
        UserInfoUpdateRequest updateRequest = new UserInfoUpdateRequest("NewUsername");

        mockMvc.perform(put("/api/users/me")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isNoContent())
                .andExpect(cookie().maxAge("jwt_token", 0));

        Optional<User> previousUser = userRepository.findByUsername("TestUser");
        Optional<User> currentUser = userRepository.findByUsername("NewUsername");
        User user =  currentUser.orElseThrow();
        assertThat(previousUser).isEmpty();
        assertThat(user.getUsername()).isEqualTo("NewUsername");
    }


    @Test
    @WithMockUser(username = "TestUser")
    void shouldUpdateUserPasswordAndPersistInDatabase() throws Exception {
        createUser();
        UserPasswordUpdateRequest updateRequest = new UserPasswordUpdateRequest("password", "newPassword");

        mockMvc.perform(put("/api/users/me/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isNoContent())
                .andExpect(cookie().maxAge("jwt_token", 0));

        User updatedUser = userRepository.findByUsername("TestUser").orElseThrow();
        assertThat(passwordEncoder.matches("newPassword", updatedUser.getPassword())).isTrue();
    }

    @Test
    @WithMockUser(username = "TestUser")
    void shouldDeleteUserAndPersistInDatabase() throws Exception {
        createUser();

        mockMvc.perform(delete("/api/users/me"))
                .andExpect(status().isNoContent())
                .andExpect(cookie().maxAge("jwt_token", 0));

        Optional<User> user =  userRepository.findByUsername("TestUser");
        assertThat(user).isEmpty();
    }

    @Test
    @WithMockUser(username = "TestUser")
    void shouldReturnCurrentUserProfile() throws Exception {
        User user = createUser();

        mockMvc.perform(get("/api/users/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(user.getUserId()))
                .andExpect(jsonPath("$.username").value("TestUser"));
    }
}
