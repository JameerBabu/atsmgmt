package com.ats.service;

import com.ats.model.User;
import com.ats.model.UserRole;
import com.ats.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.*;

public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void createUser_Success() {
        User user = new User();
        user.setUsername("testuser");
        user.setEmail("test@test.com");
        user.setRole(UserRole.APPLICANT);

        when(userRepository.save(any(User.class))).thenReturn(user);

        User savedUser = userService.createUser(user);
        assertNotNull(savedUser);
        assertEquals("testuser", savedUser.getUsername());
    }

    @Test
    void getUserById_Success() {
        User user = new User();
        user.setId(1L);
        user.setUsername("testuser");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        User foundUser = userService.getUserById(1L);
        assertNotNull(foundUser);
        assertEquals(1L, foundUser.getId());
    }

    @Test
    void getUserByUsername_Success() {
        User user = new User();
        user.setUsername("testuser");

        when(userRepository.findByUsername("testuser")).thenReturn(user);

        User foundUser = userService.getUserByUsername("testuser");
        assertNotNull(foundUser);
        assertEquals("testuser", foundUser.getUsername());
    }
} 