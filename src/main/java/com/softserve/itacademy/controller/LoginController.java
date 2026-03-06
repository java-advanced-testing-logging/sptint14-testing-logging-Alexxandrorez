package com.softserve.itacademy.controller;

import com.softserve.itacademy.service.UserService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Slf4j
@RequiredArgsConstructor
@Controller
public class LoginController {

    private final UserService userService;

    @GetMapping("/login")
    public String login(HttpSession session, Model model) {
        log.info("GET /login");
        if (session.getAttribute("user_id") != null) {
            log.info("User already logged in, redirecting to home");
            return "redirect:/";
        }
        return "login";
    }

    @PostMapping("/login")
    public String loginPost(@RequestParam("username") String email,
                            @RequestParam("password") String password,
                            HttpSession session
    ) {
        log.info("POST /login : email={}", email);
        var userOpt = userService.findByUsername(email);

        if (userOpt.isEmpty()) {
            log.warn("Login failed: user with email {} not found", email);
            return "redirect:/login?error=true";
        }

        var user = userOpt.get();
        if (user.getPassword().equals("{noop}" + password)) {
            session.setAttribute("username", user.getFirstName());
            session.setAttribute("user_id", user.getId());
            log.info("Login successful for user: ID={}, email={}", user.getId(), email);
            return "redirect:/";
        } else {
            log.warn("Login failed: incorrect password for email {}", email);
            return "redirect:/login?error=true";
        }
    }

    @PostMapping("/logout")
    public String logout(HttpSession session) {
        Object userId = session.getAttribute("user_id");
        log.info("POST /logout : user_id={}", userId);
        session.invalidate();
        log.info("Session invalidated successfully");
        return "redirect:/login?logout=true";
    }
}