package com.upsc.upsctracker.controller;

import com.upsc.upsctracker.entity.User;
import com.upsc.upsctracker.repository.UserRepository;
import com.upsc.upsctracker.security.PasswordHasher;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.sql.Timestamp;
import java.util.Locale;

@Controller
public class SignupController {

    private final UserRepository userRepository;

    public SignupController(
            UserRepository userRepository) {

        this.userRepository = userRepository;
    }

    @GetMapping("/signup")
    public String signup() {
        return "signup";
    }

    @PostMapping("/signup")
    public String createAccount(
            @RequestParam String name,
            @RequestParam String email,
            @RequestParam String password,
            @RequestParam String confirmPassword,
            @RequestParam Integer attemptYear,
            @RequestParam(required = false) String whyUpsc,
            @RequestParam(required = false) String dreamPost) {

        name = name.trim();

        email = email
                .trim()
                .toLowerCase(Locale.ROOT);

        if (whyUpsc != null) {
            whyUpsc = whyUpsc.trim();
        }

        if (dreamPost != null) {
            dreamPost = dreamPost.trim();
        }

        // ================= PASSWORD MATCH =================

        if (!password.equals(confirmPassword)) {

            return "redirect:/signup?error=passwordMismatch";
        }

        // ================= STRONG PASSWORD =================

        boolean strongPassword =
                password.length() >= 6
                        && password.matches(".*[A-Z].*")
                        && password.matches(".*\\d.*")
                        && password.matches(
                        ".*[^a-zA-Z0-9].*"
                );

        if (!strongPassword) {

            return "redirect:/signup?error=weakPassword";
        }

        // ================= EMAIL CHECK =================

        if (userRepository
                .findByEmail(email)
                .isPresent()) {

            return "redirect:/signup?error=emailExists";
        }

        // ================= ATTEMPT YEAR CHECK =================

        if (attemptYear == null) {

            return "redirect:/signup?error=attemptYearRequired";
        }

        if (attemptYear < 2026 ||
                attemptYear > 2050) {

            return "redirect:/signup?error=invalidAttemptYear";
        }

        // ================= CREATE USER =================

        User user = new User();

        user.setName(name);

        user.setEmail(email);

        // Store a secure password hash.
        user.setPassword(
                PasswordHasher.hash(password)
        );

        user.setAttemptYear(
                attemptYear
        );

        // ================= UPSC INFORMATION =================

        user.setWhyUpsc(
                whyUpsc == null ||
                        whyUpsc.isBlank()
                        ? null
                        : whyUpsc
        );

        user.setDreamPost(
                dreamPost == null ||
                        dreamPost.isBlank()
                        ? null
                        : dreamPost
        );

        user.setCreatedAt(
                new Timestamp(
                        System.currentTimeMillis()
                )
        );

        // ================= SAVE USER =================

        userRepository.save(user);

        return "redirect:/login?error=signupSuccess";
    }
}