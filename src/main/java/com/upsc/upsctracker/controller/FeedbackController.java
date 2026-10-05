package com.upsc.upsctracker.controller;

import com.upsc.upsctracker.entity.Feedback;
import com.upsc.upsctracker.entity.User;
import com.upsc.upsctracker.repository.FeedbackRepository;
import com.upsc.upsctracker.repository.UserRepository;

import jakarta.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@Controller
public class FeedbackController {

    private final FeedbackRepository feedbackRepository;
    private final UserRepository userRepository;

    public FeedbackController(
            FeedbackRepository feedbackRepository,
            UserRepository userRepository) {

        this.feedbackRepository = feedbackRepository;
        this.userRepository = userRepository;
    }

    // =========================================================
    // FEEDBACK PAGE
    // =========================================================

    @GetMapping("/feedback")
    public String feedback(
            HttpSession session,
            Model model) {

        Object userIdObject =
                session.getAttribute("loggedInUserId");

        // -----------------------------------------------------
        // Check login
        // -----------------------------------------------------

        if (userIdObject == null) {
            return "redirect:/login";
        }

        Integer userId;

        try {

            userId =
                    ((Number) userIdObject).intValue();

        } catch (Exception e) {

            session.invalidate();

            return "redirect:/login";
        }

        // -----------------------------------------------------
        // Get current user
        // -----------------------------------------------------

        User user =
                userRepository.findById(userId)
                        .orElse(null);

        if (user == null) {

            session.invalidate();

            return "redirect:/login";
        }

        // -----------------------------------------------------
        // User details
        // -----------------------------------------------------

        String userName =
                user.getName() == null
                        ? ""
                        : user.getName();

        String userEmail =
                user.getEmail() == null
                        ? ""
                        : user.getEmail();

        // -----------------------------------------------------
        // Attempt year
        // -----------------------------------------------------

        Integer attemptYear =
                user.getAttemptYear();

        if (attemptYear == null) {
            attemptYear = 2027;
        }

        // -----------------------------------------------------
        // Keep session information updated
        // -----------------------------------------------------

        session.setAttribute(
                "attemptYear",
                attemptYear
        );

        session.setAttribute(
                "loggedInUserName",
                userName
        );

        session.setAttribute(
                "loggedInUserEmail",
                userEmail
        );

        // -----------------------------------------------------
        // Get previous feedback
        // -----------------------------------------------------

        Long feedbackUserId =
                userId.longValue();

        List<Feedback> feedbackList =
                feedbackRepository
                        .findByUserIdOrderByCreatedAtDesc(
                                feedbackUserId
                        );

        // -----------------------------------------------------
        // Send data to feedback.html
        // -----------------------------------------------------

        model.addAttribute(
                "userName",
                userName
        );

        model.addAttribute(
                "userEmail",
                userEmail
        );

        model.addAttribute(
                "attemptYear",
                attemptYear
        );

        model.addAttribute(
                "feedbackList",
                feedbackList
        );

        return "feedback";
    }


    // =========================================================
    // SUBMIT FEEDBACK
    // =========================================================

    @PostMapping("/feedback")
    public String submitFeedback(
            @RequestParam Integer rating,
            @RequestParam String category,
            @RequestParam String message,
            HttpSession session) {

        Object userIdObject =
                session.getAttribute("loggedInUserId");

        // -----------------------------------------------------
        // Check login
        // -----------------------------------------------------

        if (userIdObject == null) {
            return "redirect:/login";
        }

        Integer userId;

        try {

            userId =
                    ((Number) userIdObject).intValue();

        } catch (Exception e) {

            session.invalidate();

            return "redirect:/login";
        }

        // -----------------------------------------------------
        // Get current user
        // -----------------------------------------------------

        User user =
                userRepository.findById(userId)
                        .orElse(null);

        if (user == null) {

            session.invalidate();

            return "redirect:/login";
        }

        Long feedbackUserId =
                userId.longValue();

        String userName =
                user.getName() == null
                        ? ""
                        : user.getName();

        String userEmail =
                user.getEmail() == null
                        ? ""
                        : user.getEmail();

        // =====================================================
        // VALIDATE CATEGORY
        // =====================================================

        if (category == null ||
                category.isBlank()) {

            return "redirect:/feedback?error=category";
        }

        String normalizedCategory =
                category.trim().toUpperCase();

        boolean validCategory =
                normalizedCategory.equals("SUGGESTIONS")
                        || normalizedCategory.equals("REPORT_ISSUE")
                        || normalizedCategory.equals("LIKES")
                        || normalizedCategory.equals("FUTURE_IDEAS");

        if (!validCategory) {

            return "redirect:/feedback?error=category";
        }

        // =====================================================
        // VALIDATE RATING
        // =====================================================

        if (rating == null ||
                rating < 1 ||
                rating > 5) {

            return "redirect:/feedback?error=rating";
        }

        // =====================================================
        // VALIDATE MESSAGE
        // =====================================================

        if (message == null ||
                message.trim().isEmpty()) {

            return "redirect:/feedback?error=message";
        }

        // =====================================================
        // CREATE FEEDBACK
        // =====================================================

        Feedback feedback =
                new Feedback();

        feedback.setUserId(
                feedbackUserId
        );

        feedback.setUserName(
                userName
        );

        feedback.setUserEmail(
                userEmail
        );

        feedback.setRating(
                rating
        );

        feedback.setCategory(
                normalizedCategory
        );

        feedback.setMessage(
                message.trim()
        );

        feedback.setCreatedAt(
                LocalDateTime.now()
        );

        // =====================================================
        // SAVE FEEDBACK
        // =====================================================

        feedbackRepository.save(
                feedback
        );

        return "redirect:/feedback?success=true";
    }


    // =========================================================
    // DELETE FEEDBACK
    // =========================================================

    @PostMapping("/feedback/delete/{id}")
    public String deleteFeedback(
            @PathVariable Long id,
            HttpSession session) {

        Object userIdObject =
                session.getAttribute("loggedInUserId");

        // -----------------------------------------------------
        // Check login
        // -----------------------------------------------------

        if (userIdObject == null) {
            return "redirect:/login";
        }

        Integer userId;

        try {

            userId =
                    ((Number) userIdObject).intValue();

        } catch (Exception e) {

            session.invalidate();

            return "redirect:/login";
        }

        Long feedbackUserId =
                userId.longValue();

        // -----------------------------------------------------
        // Find feedback
        // -----------------------------------------------------

        Feedback feedback =
                feedbackRepository.findById(id)
                        .orElse(null);

        if (feedback == null) {

            return "redirect:/feedback?error=notfound";
        }

        // =====================================================
        // SECURITY CHECK
        // =====================================================

        if (feedback.getUserId() == null ||
                !feedback.getUserId().equals(feedbackUserId)) {

            return "redirect:/feedback?error=unauthorized";
        }

        // -----------------------------------------------------
        // Delete
        // -----------------------------------------------------

        feedbackRepository.deleteById(id);

        return "redirect:/feedback?deleted=true";
    }
}