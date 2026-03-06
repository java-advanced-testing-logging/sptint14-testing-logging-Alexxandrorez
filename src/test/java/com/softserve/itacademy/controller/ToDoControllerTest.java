package com.softserve.itacademy.controller;

import com.softserve.itacademy.dto.todoDto.ToDoDtoConverter;
import com.softserve.itacademy.model.ToDo;
import com.softserve.itacademy.model.Task;
import com.softserve.itacademy.model.User;
import com.softserve.itacademy.service.ToDoService;
import com.softserve.itacademy.service.UserService;
import com.softserve.itacademy.service.TaskService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashSet;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Slf4j
//@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@WebMvcTest(ToDoController.class)
@AutoConfigureMockMvc(addFilters = false)
class ToDoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ToDoService toDoService;

    @MockBean
    private UserService userService;

    @MockBean
    private TaskService taskService;

    @MockBean
    private ToDoDtoConverter toDoDtoConverter;

    @Test
    void getTasks_ShouldReturnModelWithTasksAndCollaborators() throws Exception {
        log.info("Starting test: getTasks_ShouldReturnModelWithTasksAndCollaborators");

        long todoId = 1L;

        User owner = new User();
        owner.setId(2L);
        owner.setFirstName("OwnerName");

        ToDo todo = new ToDo();
        todo.setId(todoId);
        todo.setTitle("My List");
        todo.setOwner(owner);
        todo.setTasks(new HashSet<>());

        List<Task> tasks = List.of(new Task());
        List<User> potentialCollaborators = List.of(new User());

        when(toDoService.readById(todoId)).thenReturn(todo);
        when(taskService.getByTodoId(todoId)).thenReturn(tasks);
        when(userService.getAll()).thenReturn(potentialCollaborators);

        mockMvc.perform(get("/todos/" + todoId + "/tasks"))
                .andExpect(status().isOk())
                .andExpect(view().name("todo-tasks"))
                .andExpect(model().attributeExists("todo"))
                .andExpect(model().attributeExists("tasks"))
                .andExpect(model().attributeExists("users"));

        log.info("Finished test: getTasks successfully verified");
    }

    @Test
    void getAllUserToDos_ShouldReturnUserToDos() throws Exception {
        log.info("Starting test: getAllUserToDos_ShouldReturnUserToDos");

        long userId = 5L;
        User user = new User();
        user.setId(userId);
        user.setFirstName("John");

        List<ToDo> todos = List.of(new ToDo());

        when(userService.readById(userId)).thenReturn(user);
        when(toDoService.getByUserId(userId)).thenReturn(todos);

        mockMvc.perform(get("/todos/all/users/" + userId))
                .andExpect(status().isOk())
                .andExpect(view().name("todos-user"))
                .andExpect(model().attribute("todos", todos))
                .andExpect(model().attributeExists("user"));

        log.info("Finished test: getAllUserToDos successfully verified");
    }
}