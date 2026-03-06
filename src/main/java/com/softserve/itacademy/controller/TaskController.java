package com.softserve.itacademy.controller;

import com.softserve.itacademy.dto.TaskDto;
import com.softserve.itacademy.model.TaskPriority;
import com.softserve.itacademy.service.TaskService;
import com.softserve.itacademy.dto.TaskTransformer;
import com.softserve.itacademy.model.Task;
import com.softserve.itacademy.service.StateService;
import com.softserve.itacademy.service.ToDoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping("/tasks")
@RequiredArgsConstructor
@Slf4j
public class TaskController {

    private final TaskService taskService;
    private final ToDoService todoService;
    private final StateService stateService;
    private final TaskTransformer taskTransformer;

    @GetMapping("/create/todos/{todo_id}")
    public String create(@PathVariable("todo_id") Long todoId, Model model) {
        log.info("GET /tasks/create/todos/{}", todoId);
        TaskDto taskDto = TaskDto.builder()
                .todoId(todoId)
                .build();

        model.addAttribute("task", taskDto);
        model.addAttribute("todo", todoService.readById(todoId));
        model.addAttribute("priorities", TaskPriority.values());

        return "create-task";
    }

    @PostMapping("/create/todos/{todo_id}")
    public String create(@PathVariable("todo_id") Long todoId,
                         @Valid @ModelAttribute("task") TaskDto taskDto,
                         BindingResult bindingResult,
                         Model model) {
        log.info("POST /tasks/create/todos/{} : taskName={}", todoId, taskDto.getName());
        if (bindingResult.hasErrors()) {
            log.warn("Validation errors in task creation: {}", bindingResult.getAllErrors());
            model.addAttribute("todo", todoService.readById(todoId));
            model.addAttribute("priorities", TaskPriority.values());
            return "create-task";
        }

        try {
            taskDto.setTodoId(todoId);
            taskService.create(taskDto);
            log.info("Task created successfully in ToDo ID: {}", todoId);
        } catch (IllegalArgumentException e) {
            log.error("Error creating task: {}", e.getMessage());
            bindingResult.rejectValue("name", "error.task", e.getMessage());
            model.addAttribute("todo", todoService.readById(todoId));
            model.addAttribute("priorities", TaskPriority.values());
            return "create-task";
        }

        return "redirect:/todos/" + todoId + "/tasks";
    }

    @GetMapping("/{task_id}/update/todos/{todo_id}")
    public String taskUpdateForm(@PathVariable("task_id") Long taskId,
                                 @PathVariable("todo_id") Long todoId,
                                 Model model) {
        log.info("GET /tasks/{}/update/todos/{}", taskId, todoId);
        Task task = taskService.readById(taskId);
        TaskDto taskDto = taskTransformer.convertToDto(task);

        model.addAttribute("task", taskDto);
        model.addAttribute("todo", todoService.readById(todoId));
        model.addAttribute("priorities", TaskPriority.values());
        model.addAttribute("states", stateService.getAll());

        return "update-task";
    }

    @PostMapping("/{task_id}/update/todos/{todo_id}")
    public String update(@PathVariable("task_id") Long taskId,
                         @PathVariable("todo_id") Long todoId,
                         @Valid @ModelAttribute("task") TaskDto taskDto,
                         BindingResult bindingResult,
                         Model model) {
        log.info("POST /tasks/{}/update/todos/{}", taskId, todoId);
        if (bindingResult.hasErrors()) {
            log.warn("Validation errors in task update ID {}: {}", taskId, bindingResult.getAllErrors());
            model.addAttribute("todo", todoService.readById(todoId));
            model.addAttribute("priorities", TaskPriority.values());
            model.addAttribute("states", stateService.getAll());
            return "update-task";
        }

        try {
            Task task = taskService.readById(taskId);
            Task updatedTask = taskTransformer.fillEntityFields(
                    task,
                    taskDto,
                    todoService.readById(todoId),
                    stateService.readById(taskDto.getStateId())
            );

            taskService.update(updatedTask);
            log.info("Task updated successfully: ID={}", taskId);
        } catch (IllegalArgumentException e) {
            log.error("Error updating task ID {}: {}", taskId, e.getMessage());
            bindingResult.rejectValue("name", "error.task", e.getMessage());
            model.addAttribute("todo", todoService.readById(todoId));
            model.addAttribute("priorities", TaskPriority.values());
            model.addAttribute("states", stateService.getAll());
            return "update-task";
        }

        return "redirect:/todos/" + todoId + "/tasks";
    }

    @GetMapping("/{task_id}/delete/todos/{todo_id}")
    public String delete(@PathVariable("task_id") Long taskId,
                         @PathVariable("todo_id") Long todoId) {
        log.info("GET /tasks/{}/delete/todos/{}", taskId, todoId);
        taskService.delete(taskId);
        log.info("Task deleted successfully: ID={}", taskId);
        return "redirect:/todos/" + todoId + "/tasks";
    }

    @ExceptionHandler(EntityNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ModelAndView handleEntityNotFoundException(EntityNotFoundException ex) {
        log.error("EntityNotFoundException in TaskController: {}", ex.getMessage());
        ModelAndView modelAndView = new ModelAndView("error/404");
        modelAndView.addObject("message", ex.getMessage());
        return modelAndView;
    }
}