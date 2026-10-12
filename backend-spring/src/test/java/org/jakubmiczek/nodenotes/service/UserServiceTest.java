package org.jakubmiczek.nodenotes.service;

import org.jakubmiczek.nodenotes.controller.dto.UserPasswordUpdateRequest;
import org.jakubmiczek.nodenotes.controller.dto.UserRequest;
import org.jakubmiczek.nodenotes.controller.dto.UserResponse;
import org.jakubmiczek.nodenotes.entity.User;
import org.jakubmiczek.nodenotes.exception.UserAlreadyExistException;
import org.jakubmiczek.nodenotes.exception.UserDoesNotExistException;
import org.jakubmiczek.nodenotes.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    UserRepository userRepository;

    @Mock
    PasswordEncoder passwordEncoder;

    @InjectMocks
    UserService userService;

    @Test
    void shouldAddUserCorrectly() {
        UserRequest userRequest = new UserRequest("username", "password");
        String encodedPassword = "hashedPassword";

        when(userRepository.findByUsername("username")).thenReturn(Optional.empty());
        when(passwordEncoder.encode(userRequest.password())).thenReturn(encodedPassword);
        
        userService.addUser(userRequest);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());

        assertThat(captor.getValue().getUsername()).isEqualTo("username");
        assertThat(captor.getValue().getPassword()).isEqualTo(encodedPassword);
    }

    @Test
    void shouldThrowUserAlreadyExistsExceptionWhenCreatingNewUser() {
        User user = new User();
        user.setUserId(1L);
        user.setUsername("username");

        when(userRepository.findByUsername("username")).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> userService.addUser(new UserRequest("username", "password")))
                .isInstanceOf(UserAlreadyExistException.class);
    }

    @Test
    void shouldUpdateUserInfoCorrectly() {
        User user = new User();
        user.setUserId(1L);
        user.setUsername("oldUsername");

        when(userRepository.findByUsername("oldUsername")).thenReturn(Optional.of(user));
        when(userRepository.findByUsername("newUsername")).thenReturn(Optional.empty());

        userService.updateUserInfo("oldUsername", "newUsername");

        assertThat(user.getUsername()).isEqualTo("newUsername");
    }

    @Test
    void shouldThrowUserAlreadyExistsExceptionWhenUpdatingUserInfoToTakenUsername() {
        User currentUser = new User();
        currentUser.setUserId(1L);
        currentUser.setUsername("oldUsername");

        User otherUser = new User();
        otherUser.setUserId(2L);
        otherUser.setUsername("newUsername");

        when(userRepository.findByUsername("oldUsername")).thenReturn(Optional.of(currentUser));
        when(userRepository.findByUsername("newUsername")).thenReturn(Optional.of(otherUser));

        assertThatThrownBy(() -> userService.updateUserInfo("oldUsername", "newUsername"))
                .isInstanceOf(UserAlreadyExistException.class);
    }

    @Test
    void shouldUpdatePasswordCorrectly() {
        User user = new User();
        user.setUserId(1L);
        user.setUsername("username");
        user.setPassword("oldHashedPassword");

        UserPasswordUpdateRequest request = new UserPasswordUpdateRequest("oldRawPassword", "newRawPassword");
        String newHashedPassword = "newHashedPassword";

        when(userRepository.findByUsername("username")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(request.oldPassword(), user.getPassword())).thenReturn(true);
        when(passwordEncoder.encode(request.newPassword())).thenReturn(newHashedPassword);

        userService.updatePassword("username", request);

        assertThat(user.getPassword()).isEqualTo(newHashedPassword);
    }

    @Test
    void shouldThrowBadCredentialsExceptionWhenUpdatingPasswordWithWrongOldPassword() {
        User user = new User();
        user.setUserId(1L);
        user.setUsername("username");
        user.setPassword("oldHashedPassword");

        UserPasswordUpdateRequest request = new UserPasswordUpdateRequest("wrongOldPassword", "newRawPassword");

        when(userRepository.findByUsername("username")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(request.oldPassword(), user.getPassword())).thenReturn(false);

        assertThatThrownBy(() -> userService.updatePassword("username", request))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessage("Invalid old password");
    }

    @Test
    void shouldDeleteUserCorrectly() {
        User user = new User();
        user.setUserId(1L);
        user.setUsername("username");

        when(userRepository.findByUsername("username")).thenReturn(Optional.of(user));
        userService.deleteUser("username");

        verify(userRepository).delete(user);
    }

    @Test
    void shouldThrowUserNotFoundExceptionWhenDeletingNonExistentUser() {
        when(userRepository.findByUsername("username")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> userService.deleteUser("username"))
                .isInstanceOf(UserDoesNotExistException.class);
    }

    @Test
    void shouldGetUserProfileCorrectly() {
        User user = new User();
        user.setUserId(1L);
        user.setUsername("username");

        when(userRepository.findByUsername("username")).thenReturn(Optional.of(user));

        UserResponse response = userService.getUserProfile("username");

        assertThat(response.username()).isEqualTo("username");
    }
}
