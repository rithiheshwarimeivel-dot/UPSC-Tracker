package com.upsc.upsctracker.controller;

import com.upsc.upsctracker.entity.User;
import com.upsc.upsctracker.repository.UserRepository;
import com.upsc.upsctracker.security.PasswordHasher;

import jakarta.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Locale;

@Controller
public class LoginController {

    private final UserRepository userRepository;

    public LoginController(
            UserRepository userRepository) {

        this.userRepository = userRepository;
    }

    @GetMapping("/login")
    public String login(
            @RequestParam(required = false) String error,
            Model model) {

        if ("invalid".equals(error)) {

            model.addAttribute(
                    "errorMessage",
                    "Invalid email or password."
            );
        }

        if ("logout".equals(error)) {

            model.addAttribute(
                    "successMessage",
                    "You have been logged out successfully."
            );
        }

        if ("signupSuccess".equals(error)) {

            model.addAttribute(
                    "successMessage",
                    "Account created successfully. Please log in."
            );
        }

        return "login";
    }

    @PostMapping("/login")
    public String authenticate(
            @RequestParam String email,
            @RequestParam String password,
            HttpSession session) {

        email =
                email.trim()
                        .toLowerCase(Locale.ROOT);

        User user =
                userRepository
                        .findByEmail(email)
                        .orElse(null);

        if (user == null ||
                user.getPassword() == null) {

            return "redirect:/login?error=invalid";
        }

        String storedPassword =
                user.getPassword();

        boolean passwordMatches =
                false;

        // =====================================================
        // NEW SECURE PASSWORD
        // =====================================================

        if (storedPassword.startsWith(
                "pbkdf2_sha256$")) {

            passwordMatches =
                    PasswordHasher.matches(
                            password,
                            storedPassword
                    );
        }

        // =====================================================
        // OLD PLAIN-TEXT PASSWORD
        // =====================================================
        // Existing users can still log in.
        // Their password is upgraded automatically.

        else if (storedPassword.equals(password)) {

            passwordMatches = true;

            user.setPassword(
                    PasswordHasher.hash(password)
            );

            userRepository.save(user);
        }

        // =====================================================
        // INVALID PASSWORD
        // =====================================================

        if (!passwordMatches) {

            return "redirect:/login?error=invalid";
        }

        // =====================================================
        // LOGIN SESSION
        // =====================================================

        session.setAttribute(
                "loggedInUserId",
                user.getId()
        );

        session.setAttribute(
                "loggedInUserName",
                user.getName()
        );

        session.setAttribute(
                "loggedInUserEmail",
                user.getEmail()
        );

        session.setAttribute(
                "attemptYear",
                user.getAttemptYear()
        );

        return "redirect:/dashboard";
    }

    @GetMapping("/logout")
    public String logout(
            HttpSession session) {

        session.invalidate();

        return "redirect:/login?error=logout";
    }
}