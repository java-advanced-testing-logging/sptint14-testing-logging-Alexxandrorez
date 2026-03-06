package com.softserve.itacademy.service;

import com.softserve.itacademy.model.ToDo;
import com.softserve.itacademy.model.User;
import com.softserve.itacademy.repository.ToDoRepository;
import com.softserve.itacademy.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashSet;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@Slf4j
@ExtendWith(MockitoExtension.class)
class ToDoServiceTest {

    @Mock
    private ToDoRepository todoRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ToDoService todoService;

    @Test
    void createSuccess() {
        ToDo todo = new ToDo();
        todo.setTitle("New Task");

        when(todoRepository.existsByTitle("New Task")).thenReturn(false);
        when(todoRepository.save(any(ToDo.class))).thenReturn(todo);

        ToDo result = todoService.create(todo);

        assertNotNull(result);
        assertEquals("New Task", result.getTitle());
        verify(todoRepository).save(todo);
    }

    @Test
    void createDuplicateTitleFail() {
        ToDo todo = new ToDo();
        todo.setTitle("Duplicate");

        when(todoRepository.existsByTitle("Duplicate")).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> todoService.create(todo));
    }

    @Test
    void addCollaboratorTest() {
        ToDo todo = new ToDo();
        todo.setId(1L);
        todo.setCollaborators(new HashSet<>());

        User user = new User();
        user.setId(5L);

        when(todoRepository.findById(1L)).thenReturn(Optional.of(todo));
        when(userRepository.findById(5L)).thenReturn(Optional.of(user));
        when(todoRepository.save(any(ToDo.class))).thenReturn(todo);

        todoService.addCollaborator(1L, 5L);

        assertTrue(todo.getCollaborators().contains(user));
        verify(todoRepository).save(todo);
    }

    @Test
    void removeCollaboratorTest() {
        User user = new User();
        user.setId(5L);

        ToDo todo = new ToDo();
        todo.setId(1L);
        todo.setCollaborators(new HashSet<>());
        todo.getCollaborators().add(user);

        when(todoRepository.findById(1L)).thenReturn(Optional.of(todo));
        when(userRepository.findById(5L)).thenReturn(Optional.of(user));
        when(todoRepository.save(any(ToDo.class))).thenReturn(todo);

        todoService.removeCollaborator(1L, 5L);

        assertFalse(todo.getCollaborators().contains(user));
        verify(todoRepository).save(todo);
    }
}