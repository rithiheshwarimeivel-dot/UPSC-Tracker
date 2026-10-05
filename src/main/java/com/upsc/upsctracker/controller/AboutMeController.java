package com.upsc.upsctracker.controller;

import com.upsc.upsctracker.entity.ExamType;
import com.upsc.upsctracker.entity.Subject;
import com.upsc.upsctracker.entity.Topic;
import com.upsc.upsctracker.entity.User;
import com.upsc.upsctracker.repository.SubjectRepository;
import com.upsc.upsctracker.repository.TopicRepository;
import com.upsc.upsctracker.repository.UserRepository;

import jakarta.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

@Controller
public class AboutMeController {

    private final UserRepository userRepository;
    private final SubjectRepository subjectRepository;
    private final TopicRepository topicRepository;

    private final Path uploadDirectory =
            Paths.get("uploads/profile");

    public AboutMeController(
            UserRepository userRepository,
            SubjectRepository subjectRepository,
            TopicRepository topicRepository) {

        this.userRepository = userRepository;
        this.subjectRepository = subjectRepository;
        this.topicRepository = topicRepository;
    }


    // ============================================================
    // ABOUT ME PAGE
    // ============================================================

    @GetMapping("/about-me")
    public String aboutMe(
            HttpSession session,
            Model model) {

        Object userIdObject =
                session.getAttribute("loggedInUserId");

        if (userIdObject == null) {
            return "redirect:/login";
        }

        Integer userId =
                ((Number) userIdObject).intValue();

        User user =
                userRepository.findById(userId)
                        .orElse(null);

        if (user == null) {

            session.invalidate();

            return "redirect:/login";
        }


        // ========================================================
        // ATTEMPT YEAR
        // ========================================================

        Integer attemptYear =
                user.getAttemptYear();


        // ========================================================
        // LOAD SUBJECTS AND TOPICS
        // ========================================================

        List<Subject> allSubjects =
                subjectRepository.findAll();

        List<Topic> allTopics =
                topicRepository.findAll();


        // ========================================================
        // PRELIMS
        // ========================================================

        List<Subject> prelimsSubjects =
                allSubjects.stream()
                        .filter(subject ->
                                subject.getExamType()
                                        == ExamType.PRELIMS)
                        .filter(subject ->
                                subject.getId() != null
                                        && !subject.getId().equals(8L)
                                        && !subject.getId().equals(15L))
                        .toList();

        List<Topic> prelimsTopics =
                getTopicsForSubjects(
                        allTopics,
                        prelimsSubjects
                );

        int prelimsTotal =
                prelimsTopics.size();

        long prelimsCompleted =
                countCompleted(prelimsTopics);

        int prelimsProgress =
                calculateProgress(
                        prelimsCompleted,
                        prelimsTotal
                );


        // ========================================================
        // MAINS
        // ========================================================

        List<Subject> mainsSubjects =
                allSubjects.stream()
                        .filter(subject ->
                                subject.getExamType()
                                        == ExamType.MAINS)
                        .toList();

        List<Topic> mainsTopics =
                getTopicsForSubjects(
                        allTopics,
                        mainsSubjects
                );

        int mainsTotal =
                mainsTopics.size();

        long mainsCompleted =
                countCompleted(mainsTopics);

        int mainsProgress =
                calculateProgress(
                        mainsCompleted,
                        mainsTotal
                );


        // ========================================================
        // CSAT
        // ========================================================

        List<Topic> csatTopics =
                allTopics.stream()
                        .filter(topic ->
                                topic.getSubject() != null
                                        && topic.getSubject()
                                        .getId()
                                        .equals(15L))
                        .toList();

        int csatTotal =
                csatTopics.size();

        long csatCompleted =
                countCompleted(csatTopics);

        int csatProgress =
                calculateProgress(
                        csatCompleted,
                        csatTotal
                );


        // ========================================================
        // CURRENT AFFAIRS
        // ========================================================

        List<Topic> currentAffairsTopics =
                allTopics.stream()
                        .filter(topic ->
                                topic.getSubject() != null
                                        && topic.getSubject()
                                        .getId()
                                        .equals(8L))
                        .toList();

        int currentAffairsTotal =
                currentAffairsTopics.size();

        long currentAffairsCompleted =
                countCompleted(
                        currentAffairsTopics
                );

        int currentAffairsProgress =
                calculateProgress(
                        currentAffairsCompleted,
                        currentAffairsTotal
                );


        // ========================================================
        // OVERALL
        // ========================================================

        int totalTopics =
                prelimsTotal
                        + mainsTotal
                        + csatTotal
                        + currentAffairsTotal;

        long totalCompleted =
                prelimsCompleted
                        + mainsCompleted
                        + csatCompleted
                        + currentAffairsCompleted;

        int overallProgress =
                calculateProgress(
                        totalCompleted,
                        totalTopics
                );


        // ========================================================
        // TOTAL SUBJECTS
        // ========================================================

        int totalSubjects =
                prelimsSubjects.size()
                        + mainsSubjects.size();


        // ========================================================
        // MODEL
        // ========================================================

        model.addAttribute(
                "user",
                user
        );

        model.addAttribute(
                "attemptYear",
                attemptYear
        );

        model.addAttribute(
                "totalSubjects",
                totalSubjects
        );

        model.addAttribute(
                "overallProgress",
                overallProgress
        );

        model.addAttribute(
                "totalTopics",
                totalTopics
        );

        model.addAttribute(
                "totalCompleted",
                totalCompleted
        );

        model.addAttribute(
                "prelimsProgress",
                prelimsProgress
        );

        model.addAttribute(
                "mainsProgress",
                mainsProgress
        );

        model.addAttribute(
                "csatProgress",
                csatProgress
        );

        model.addAttribute(
                "currentAffairsProgress",
                currentAffairsProgress
        );

        return "about-me";
    }


    // ============================================================
    // UPDATE PROFILE
    // ============================================================

    @PostMapping("/about-me/update-profile")
    public String updateProfile(
            HttpSession session,

            @RequestParam("name")
            String name,

            @RequestParam("attemptYear")
            Integer attemptYear,

            @RequestParam(
                    value = "whyUpsc",
                    required = false
            )
            String whyUpsc,

            @RequestParam(
                    value = "dreamPost",
                    required = false
            )
            String dreamPost,

            @RequestParam(
                    value = "education",
                    required = false
            )
            String education,

            @RequestParam(
                    value = "profession",
                    required = false
            )
            String profession,

            @RequestParam(
                    value = "location",
                    required = false
            )
            String location,

            @RequestParam(
                    value = "bio",
                    required = false
            )
            String bio,

            @RequestParam(
                    value = "profilePicture",
                    required = false
            )
            MultipartFile profilePicture,

            RedirectAttributes redirectAttributes) {


        // ========================================================
        // CHECK LOGIN
        // ========================================================

        Object userIdObject =
                session.getAttribute("loggedInUserId");

        if (userIdObject == null) {

            return "redirect:/login";
        }


        Integer userId =
                ((Number) userIdObject).intValue();


        // ========================================================
        // FIND USER
        // ========================================================

        User user =
                userRepository.findById(userId)
                        .orElse(null);

        if (user == null) {

            session.invalidate();

            return "redirect:/login";
        }


        // ========================================================
        // NAME VALIDATION
        // ========================================================

        name =
                cleanValue(name);

        if (name == null ||
                name.length() < 2) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "Please enter a valid name."
            );

            return "redirect:/about-me";
        }


        // ========================================================
        // ATTEMPT YEAR VALIDATION
        // ========================================================

        if (attemptYear == null
                || attemptYear < 2026
                || attemptYear > 2050) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "Please enter a valid UPSC attempt year between 2026 and 2050."
            );

            return "redirect:/about-me";
        }


        // ========================================================
        // CLEAN UPSC PROFILE DETAILS
        // ========================================================

        whyUpsc =
                cleanValue(whyUpsc);

        dreamPost =
                cleanValue(dreamPost);


        // ========================================================
        // UPDATE PROFILE DETAILS
        // ========================================================

        user.setName(name);

        user.setAttemptYear(
                attemptYear
        );

        user.setWhyUpsc(
                whyUpsc
        );

        user.setDreamPost(
                dreamPost
        );

        user.setEducation(
                cleanValue(education)
        );

        user.setProfession(
                cleanValue(profession)
        );

        user.setLocation(
                cleanValue(location)
        );

        user.setBio(
                cleanValue(bio)
        );


        // ========================================================
        // PROFILE PICTURE
        // ========================================================

        if (profilePicture != null
                && !profilePicture.isEmpty()) {


            // ----------------------------------------------------
            // IMAGE TYPE VALIDATION
            // ----------------------------------------------------

            if (!isAllowedImage(profilePicture)) {

                redirectAttributes.addFlashAttribute(
                        "error",
                        "Only JPG, JPEG, PNG and WEBP images are allowed."
                );

                return "redirect:/about-me";
            }


            try {


                // ------------------------------------------------
                // CREATE DIRECTORY
                // ------------------------------------------------

                Files.createDirectories(
                        uploadDirectory
                );


                // ------------------------------------------------
                // GET EXTENSION
                // ------------------------------------------------

                String originalFileName =
                        profilePicture.getOriginalFilename();

                String extension =
                        getFileExtension(
                                originalFileName
                        );


                // ------------------------------------------------
                // GENERATE UNIQUE FILE NAME
                // ------------------------------------------------

                String newFileName =
                        UUID.randomUUID()
                                + extension;


                Path targetPath =
                        uploadDirectory.resolve(
                                newFileName
                        );


                // ------------------------------------------------
                // SAVE NEW IMAGE
                // ------------------------------------------------

                Files.copy(
                        profilePicture.getInputStream(),
                        targetPath
                );


                // ------------------------------------------------
                // DELETE OLD IMAGE
                // ------------------------------------------------

                if (user.getProfilePicture() != null
                        && !user.getProfilePicture().isBlank()) {

                    try {

                        Path oldPath =
                                uploadDirectory.resolve(
                                        user.getProfilePicture()
                                );

                        Files.deleteIfExists(
                                oldPath
                        );

                    } catch (Exception ignored) {

                        // Old image deletion failure
                        // should not stop profile update.
                    }
                }


                // ------------------------------------------------
                // SAVE NEW FILE NAME
                // ------------------------------------------------

                user.setProfilePicture(
                        newFileName
                );


            } catch (IOException e) {

                redirectAttributes.addFlashAttribute(
                        "error",
                        "Unable to upload profile picture."
                );

                return "redirect:/about-me";
            }
        }


        // ========================================================
        // SAVE USER
        // ========================================================

        userRepository.save(user);


        // ========================================================
        // UPDATE SESSION
        // ========================================================

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


        // ========================================================
        // SUCCESS
        // ========================================================

        redirectAttributes.addFlashAttribute(
                "success",
                "Profile updated successfully!"
        );


        return "redirect:/about-me";
    }


    // ============================================================
    // HELPER METHODS
    // ============================================================

    private List<Topic> getTopicsForSubjects(
            List<Topic> allTopics,
            List<Subject> subjects) {

        return allTopics.stream()
                .filter(topic ->
                        topic.getSubject() != null
                                && subjects.stream()
                                .anyMatch(subject ->
                                        subject.getId()
                                                .equals(
                                                        topic.getSubject()
                                                                .getId()
                                                )
                                )
                )
                .toList();
    }


    private long countCompleted(
            List<Topic> topics) {

        return topics.stream()
                .filter(Topic::isCompleted)
                .count();
    }


    private int calculateProgress(
            long completed,
            int total) {

        if (total == 0) {
            return 0;
        }

        return (int)
                ((completed * 100) / total);
    }


    private String cleanValue(
            String value) {

        if (value == null) {
            return null;
        }

        String cleaned =
                value.trim();

        return cleaned.isEmpty()
                ? null
                : cleaned;
    }


    private boolean isAllowedImage(
            MultipartFile file) {

        String contentType =
                file.getContentType();

        if (contentType == null) {
            return false;
        }

        return contentType.equalsIgnoreCase(
                "image/jpeg"
        )
                || contentType.equalsIgnoreCase(
                "image/png"
        )
                || contentType.equalsIgnoreCase(
                "image/webp"
        );
    }


    private String getFileExtension(
            String fileName) {

        if (fileName == null
                || !fileName.contains(".")) {

            return ".jpg";
        }

        return fileName.substring(
                fileName.lastIndexOf(".")
        ).toLowerCase();
    }

}