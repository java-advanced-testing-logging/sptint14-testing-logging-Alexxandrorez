package com.softserve.itacademy.service;

import com.softserve.itacademy.config.exception.NullEntityReferenceException;
import com.softserve.itacademy.model.ToDo;
import com.softserve.itacademy.model.User;
import com.softserve.itacademy.repository.ToDoRepository;
import com.softserve.itacademy.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ToDoService {
    private final ToDoRepository todoRepository;
    private final UserRepository userRepository;

    @Transactional
    public ToDo create(ToDo todo) {
        if (todo != null) {
            if (todoRepository.existsByTitle(todo.getTitle())) {
                throw new IllegalArgumentException("ToDo with title '" + todo.getTitle() + "' already exists");
            }
            return todoRepository.save(todo);
        }
        throw new NullEntityReferenceException("ToDo cannot be 'null'");
    }

    @Transactional(readOnly = true)
    public ToDo readById(long id) {
        return todoRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException("ToDo with id " + id + " not found"));
    }

    @Transactional
    public ToDo update(ToDo todo) {
        if (todo != null) {
            if (todoRepository.existsByTitleAndIdNot(todo.getTitle(), todo.getId())) {
                throw new IllegalArgumentException("ToDo with title '" + todo.getTitle() + "' already exists");
            }
            readById(todo.getId());
            return todoRepository.save(todo);
        }
        throw new NullEntityReferenceException("ToDo cannot be 'null'");
    }

    @Transactional
    public void delete(long id) {
        ToDo todo = readById(id);
        todoRepository.delete(todo);
    }

    @Transactional(readOnly = true)
    public List<ToDo> getAll() {
        return todoRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<ToDo> getByUserId(long userId) {
        return todoRepository.getByUserId(userId);
    }

    @Transactional
    public void addCollaborator(long todoId, long userId) {
        ToDo todo = readById(todoId);
        User collaborator = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User with id " + userId + " not found"));
        todo.getCollaborators().add(collaborator);
        update(todo);
    }

    @Transactional
    public void removeCollaborator(long todoId, long userId) {
        ToDo todo = readById(todoId);
        User collaborator = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User with id " + userId + " not found"));
        todo.getCollaborators().remove(collaborator);
        update(todo);
    }
}
