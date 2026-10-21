package org.jakubmiczek.nodenotes.controller;

import org.jakubmiczek.nodenotes.controller.dto.UserRequest;
import org.jakubmiczek.nodenotes.entity.User;
import org.jakubmiczek.nodenotes.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class AuthControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void shouldRegisterNewUserAndPersistInDatabase() throws Exception {
        UserRequest userRequest = new UserRequest("TestUser", "password");

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userRequest)))
                .andExpect(status().isCreated());

        Optional<User> user = userRepository.findByUsername("TestUser");
        assertThat(user.isPresent()).isTrue();
    }

    @Test
    void shouldLoginUserCorrectly() throws Exception {
        User user = new User();
        user.setUsername("TestUser");
        user.setPassword(passwordEncoder.encode("password"));
        userRepository.save(user);
        UserRequest userRequest = new UserRequest("TestUser", "password");

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userRequest)))
                .andExpect(status().isOk())
                .andExpect(cookie().exists("jwt_token"));
    }

    @Test
    void shouldNotLoginUserWithInvalidCredentials() throws Exception {
        User user = new User();
        user.setUsername("TestUser");
        user.setPassword(passwordEncoder.encode("password"));
        userRepository.save(user);
        UserRequest userRequest = new UserRequest("TestUser", "wrongPassword");

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldLogoutUserSuccessfully() throws Exception {
        mockMvc.perform(post("/api/auth/logout"))
                .andExpect(status().isOk())
                .andExpect(cookie().maxAge("jwt_token", 0));
    }
}
