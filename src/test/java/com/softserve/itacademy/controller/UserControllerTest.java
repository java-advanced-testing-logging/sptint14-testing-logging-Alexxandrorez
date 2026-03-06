package com.softserve.itacademy.controller;

import com.softserve.itacademy.dto.userDto.UserDtoConverter;
import com.softserve.itacademy.model.User;
import com.softserve.itacademy.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Slf4j
@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;


    @Test
    void getAllUsersTest() throws Exception {
        log.info("Running test: getAllUsersTest");

        when(userService.getAll()).thenReturn(List.of(new User()));

        mockMvc.perform(MockMvcRequestBuilders.get("/users/all"))
                .andExpect(status().isOk())
                .andExpect(view().name("users-list"))
                .andExpect(model().attributeExists("users"));

        log.info("Finished getAllUsersTest - success");
    }

    @Test
    void createGetTest() throws Exception {
        log.info("Running test: createGetTest");

        mockMvc.perform(get("/users/create"))
                .andExpect(status().isOk())
                .andExpect(view().name("create-user"))
                .andExpect(model().attributeExists("user"));
    }

    @Test
    void createPostValidationError() throws Exception {
        log.info("Running test: createPostValidationError");

        mockMvc.perform(post("/users/create")
                        .param("firstName", "")
                        .param("email", "not-an-email")
                        .param("password", "123"))
                .andExpect(status().isOk())
                .andExpect(view().name("create-user"))
                .andExpect(model().hasErrors());

        log.info("createPostValidationError passed: validation handled correctly");
    }
}