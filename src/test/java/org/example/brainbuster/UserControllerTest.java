package org.example.brainbuster;

import org.example.brainbuster.controller.UserController;
import org.example.brainbuster.dto.user.UserRequest;
import org.example.brainbuster.dto.user.UserResponse;
import org.example.brainbuster.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserControllerTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getAllUsers_shouldReturnListOfUsers() {
        List<UserResponse> mockUsers = Arrays.asList(
                new UserResponse(1L,"Alice","user",100,LocalDateTime.now()),
                new UserResponse(2L,"Bob","user",200,LocalDateTime.now())
        );

        when(userService.getAllUsers()).thenReturn(mockUsers);

        List<UserResponse> result = userController.getAllUsers();

        assertEquals(2, result.size());
        verify(userService).getAllUsers();
    }

    @Test
    void getUserById_existingId_shouldReturnUser() {
        UserResponse mockUser = new UserResponse(1L,"Alice","user",100,LocalDateTime.now());

        when(userService.getUserById(1L)).thenReturn(mockUser);

        ResponseEntity<UserResponse> response = userController.getUserById(1L);

        assertEquals(200, response.getStatusCode().value());
        assertEquals(mockUser, response.getBody());
        verify(userService).getUserById(1L);
    }

    @Test
    void getUserById_nonExistingId_shouldReturn404() {
        when(userService.getUserById(99L)).thenThrow(new RuntimeException("Not found"));

        ResponseEntity<UserResponse> response = userController.getUserById(99L);

        assertEquals(404, response.getStatusCode().value());
    }

    @Test
    void createUser_shouldReturnCreatedUser() {
        UserRequest request = new UserRequest("Alice", "password");
        UserResponse created = new UserResponse(1L, "Alice", "user", 0, LocalDateTime.now());

        when(userService.createUser(request)).thenReturn(created);

        ResponseEntity<UserResponse> result = userController.createUser(request);

        assertEquals(201, result.getStatusCode().value());
        assertEquals("Alice", result.getBody().getUsername());
        assertEquals("user", result.getBody().getRole());
}
 
    @Test
    void updateUser_existingId_shouldReturnUpdatedUser() {
        UserRequest update = new UserRequest("Alice", "password");
        UserResponse updated = new UserResponse(1L, "Alice", "Admin", 0, LocalDateTime.now());

        when(userService.updateUser(1L, update)).thenReturn(updated);

        ResponseEntity<UserResponse> result = userController.updateUser(1L, update);

        assertEquals(200, result.getStatusCode().value());
        assertEquals("Alice", result.getBody().getUsername());
        assertEquals("Admin", result.getBody().getRole());
    }


    @Test
    void updateUser_nonExistingId_shouldReturn404() {
        UserRequest request = new UserRequest("Alice","NonExistent");

        when(userService.updateUser(99L, request)).thenThrow(new RuntimeException("Not found"));

        ResponseEntity<UserResponse> result = userController.updateUser(99L, request);

        assertEquals(404, result.getStatusCode().value());
    }
 
    @Test
    void deleteUser_existingId_shouldReturnOk() {
        doNothing().when(userService).deleteUser(1L);

        ResponseEntity<Void> response = userController.deleteUser(1L);

        assertEquals(200, response.getStatusCode().value());
        verify(userService).deleteUser(1L);
    }

    @Test
    void deleteUser_nonExistingId_shouldReturn404() {
        doThrow(new RuntimeException("Not found")).when(userService).deleteUser(99L);

        ResponseEntity<Void> response = userController.deleteUser(99L);

        assertEquals(404, response.getStatusCode().value());
    }
        
}
