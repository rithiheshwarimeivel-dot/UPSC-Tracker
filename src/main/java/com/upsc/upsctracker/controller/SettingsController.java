package com.upsc.upsctracker.controller;

import com.upsc.upsctracker.entity.User;
import com.upsc.upsctracker.repository.UserRepository;
import com.upsc.upsctracker.security.PasswordHasher;

import jakarta.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class SettingsController {

    private final UserRepository userRepository;

    public SettingsController(
            UserRepository userRepository) {

        this.userRepository = userRepository;
    }

    // =====================================================
    // SHOW SETTINGS PAGE
    // =====================================================

    @GetMapping("/settings")
    public String settings(
            HttpSession session,
            Model model) {

        Object userIdObject =
                session.getAttribute(
                        "loggedInUserId"
                );

        if (userIdObject == null) {

            return "redirect:/login";
        }

        Integer userId =
                ((Number) userIdObject)
                        .intValue();

        User user =
                userRepository
                        .findById(userId)
                        .orElse(null);

        if (user == null) {

            session.invalidate();

            return "redirect:/login";
        }

        Integer attemptYear =
                user.getAttemptYear();

        model.addAttribute(
                "user",
                user
        );

        model.addAttribute(
                "attemptYear",
                attemptYear
        );

        return "settings";
    }

    // =====================================================
    // SAVE ACCOUNT SETTINGS
    // =====================================================

    @PostMapping("/settings")
    public String updateSettings(
            @RequestParam String name,
            @RequestParam Integer attemptYear,
            HttpSession session) {

        Object userIdObject =
                session.getAttribute(
                        "loggedInUserId"
                );

        if (userIdObject == null) {

            return "redirect:/login";
        }

        Integer userId =
                ((Number) userIdObject)
                        .intValue();

        User user =
                userRepository
                        .findById(userId)
                        .orElse(null);

        if (user == null) {

            session.invalidate();

            return "redirect:/login";
        }

        // =================================================
        // UPDATE NAME
        // =================================================

        if (name != null &&
                !name.trim().isEmpty()) {

            user.setName(
                    name.trim()
            );

        } else {

            return "redirect:/settings?error=Name+cannot+be+empty";
        }

        // =================================================
        // UPDATE ATTEMPT YEAR
        // =================================================

        if (attemptYear == null) {

            return "redirect:/settings?error=Please+select+an+attempt+year";
        }

        if (attemptYear < 2026 ||
                attemptYear > 2050) {

            return "redirect:/settings?error=Invalid+attempt+year";
        }

        user.setAttemptYear(
                attemptYear
        );

        userRepository.save(user);

        // =================================================
        // UPDATE SESSION
        // =================================================

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

        return "redirect:/settings?success=true";
    }

    // =====================================================
    // CHANGE PASSWORD
    // =====================================================

    @PostMapping("/settings/change-password")
    public String changePassword(
            @RequestParam String currentPassword,
            @RequestParam String newPassword,
            @RequestParam String confirmPassword,
            HttpSession session) {

        // =================================================
        // CHECK LOGIN
        // =================================================

        Object userIdObject =
                session.getAttribute(
                        "loggedInUserId"
                );

        if (userIdObject == null) {

            return "redirect:/login";
        }

        Integer userId =
                ((Number) userIdObject)
                        .intValue();

        // =================================================
        // FIND USER
        // =================================================

        User user =
                userRepository
                        .findById(userId)
                        .orElse(null);

        if (user == null) {

            session.invalidate();

            return "redirect:/login";
        }

        // =================================================
        // CURRENT PASSWORD
        // =================================================

        if (currentPassword == null ||
                currentPassword.isBlank()) {

            return "redirect:/settings?passwordError=Current+password+is+required";
        }

        String storedPassword =
                user.getPassword();

        boolean currentPasswordMatches =
                false;

        // =================================================
        // SECURE PASSWORD
        // =================================================

        if (storedPassword != null &&
                storedPassword.startsWith(
                        "pbkdf2_sha256$")) {

            currentPasswordMatches =
                    PasswordHasher.matches(
                            currentPassword,
                            storedPassword
                    );
        }

        // =================================================
        // OLD PLAIN-TEXT PASSWORD
        // =================================================

        else if (storedPassword != null &&
                storedPassword.equals(
                        currentPassword
                )) {

            currentPasswordMatches = true;
        }

        if (!currentPasswordMatches) {

            return "redirect:/settings?passwordError=Current+password+is+incorrect";
        }

        // =================================================
        // NEW PASSWORD
        // =================================================

        if (newPassword == null ||
                newPassword.isBlank()) {

            return "redirect:/settings?passwordError=New+password+is+required";
        }

        boolean strongPassword =
                newPassword.length() >= 6
                        && newPassword.matches(".*[A-Z].*")
                        && newPassword.matches(".*\\d.*")
                        && newPassword.matches(
                        ".*[^a-zA-Z0-9].*"
                );

        if (!strongPassword) {

            return "redirect:/settings?passwordError=New+password+must+contain+6+characters,+an+uppercase,+a+number+and+a+special+character";
        }

        // =================================================
        // CONFIRM PASSWORD
        // =================================================

        if (confirmPassword == null ||
                confirmPassword.isBlank()) {

            return "redirect:/settings?passwordError=Please+confirm+your+new+password";
        }

        if (!newPassword.equals(
                confirmPassword
        )) {

            return "redirect:/settings?passwordError=New+password+and+confirm+password+do+not+match";
        }

        // =================================================
        // PREVENT SAME PASSWORD
        // =================================================

        if (storedPassword != null &&
                storedPassword.startsWith(
                        "pbkdf2_sha256$")) {

            if (PasswordHasher.matches(
                    newPassword,
                    storedPassword
            )) {

                return "redirect:/settings?passwordError=New+password+must+be+different+from+current+password";
            }

        } else if (newPassword.equals(
                currentPassword
        )) {

            return "redirect:/settings?passwordError=New+password+must+be+different+from+current+password";
        }

        // =================================================
        // SAVE NEW PASSWORD
        // =================================================

        user.setPassword(
                PasswordHasher.hash(
                        newPassword
                )
        );

        userRepository.save(user);

        return "redirect:/settings?passwordChanged=true";
    }
}