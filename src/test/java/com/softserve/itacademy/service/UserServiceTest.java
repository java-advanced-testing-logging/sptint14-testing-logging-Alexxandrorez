package com.softserve.itacademy.service;

import com.softserve.itacademy.config.exception.NullEntityReferenceException;
import com.softserve.itacademy.dto.userDto.CreateUserDto;
import com.softserve.itacademy.dto.userDto.UpdateUserDto;
import com.softserve.itacademy.dto.userDto.UserDto;
import com.softserve.itacademy.dto.userDto.UserDtoConverter;
import com.softserve.itacademy.model.User;
import com.softserve.itacademy.model.UserRole;
import com.softserve.itacademy.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@Slf4j
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserDtoConverter userDtoConverter;

    @InjectMocks
    private UserService userService;

    @Test
    void registerTest() {
        log.info("Testing user registration logic...");
        CreateUserDto dto = new CreateUserDto();
        dto.setPassword("12345");
        dto.setEmail("test@mail.com");

        User userFromConverter = new User();
        userFromConverter.setEmail("test@mail.com");
        userFromConverter.setPassword("12345");

        when(userDtoConverter.convertToUser(dto)).thenReturn(userFromConverter);
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User result = userService.register(dto);

        assertNotNull(result);
        assertEquals("{noop}12345", result.getPassword(), "Password should be encoded with {noop}");
        assertEquals(UserRole.USER, result.getRole(), "Default role should be USER");

        verify(userRepository, times(1)).save(any(User.class));
        log.info("Registration test passed for: {}", result.getEmail());
    }

    @Test
    void readByIdTest() {
        log.info("Testing readById for existing user...");
        User user = new User();
        user.setId(12L);

        when(userRepository.findById(12L)).thenReturn(Optional.of(user));
        User result = userService.readById(12L);

        assertEquals(user, result);
        verify(userRepository, times(1)).findById(12L);
        log.info("readById found user with ID: {}", result.getId());
    }

    @Test
    void readByIdNotFoundTest() {
        log.info("Testing readById for non-existing user (expecting exception)...");
        long userId = 100L;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> userService.readById(userId));
        log.warn("EntityNotFoundException correctly thrown for ID: {}", userId);
    }

    @Test
    void createDuplicateEmailTest() {
        log.info("Testing creation with duplicate email...");
        User user = new User();
        user.setEmail("duplicate@mail.com");

        when(userRepository.findByEmail("duplicate@mail.com"))
                .thenReturn(Optional.of(user));

        assertThrows(IllegalArgumentException.class,
                () -> userService.create(user));
        verify(userRepository, never()).save(any(User.class));
        log.warn("Duplicate email protection verified for: {}", user.getEmail());
    }

    @Test
    void createNullUserTest() {
        log.info("Testing creation with null entity reference...");
        assertThrows(NullEntityReferenceException.class,
                () -> userService.create(null));
    }

    @Test
    void update_ShouldAllowRoleChange_WhenUserIsAdmin() {
        log.info("Testing role update by Admin user...");
        UpdateUserDto updateDto = new UpdateUserDto();
        updateDto.setId(1L);
        updateDto.setRole(UserRole.USER);

        User adminInDb = new User();
        adminInDb.setId(1L);
        adminInDb.setRole(UserRole.ADMIN);

        when(userRepository.findById(1L)).thenReturn(Optional.of(adminInDb));
        when(userDtoConverter.toDto(any())).thenReturn(new UserDto());

        userService.update(updateDto);

        assertEquals(UserRole.USER, adminInDb.getRole());
        verify(userRepository).save(adminInDb);
        log.info("Admin successfully changed role for user ID: {}", adminInDb.getId());
    }

    @Test
    void update_ShouldNotAllowRoleChange_WhenUserIsNotAdmin() {
        log.info("Testing role update restriction for regular User...");
        UpdateUserDto updateDto = new UpdateUserDto();
        updateDto.setId(2L);
        updateDto.setRole(UserRole.ADMIN);

        User userInDb = new User();
        userInDb.setId(2L);
        userInDb.setRole(UserRole.USER);

        when(userRepository.findById(2L)).thenReturn(Optional.of(userInDb));
        when(userDtoConverter.toDto(any())).thenReturn(new UserDto());

        userService.update(updateDto);

        assertEquals(UserRole.USER, userInDb.getRole());
        verify(userRepository).save(userInDb);
        log.warn("Role update was correctly ignored for non-admin user ID: {}", userInDb.getId());
    }
}