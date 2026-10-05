package com.upsc.upsctracker.controller;

import com.upsc.upsctracker.entity.ExamType;
import com.upsc.upsctracker.entity.Subject;
import com.upsc.upsctracker.entity.Topic;
import com.upsc.upsctracker.repository.SubjectRepository;
import com.upsc.upsctracker.repository.TopicRepository;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class SubjectController {

    private final SubjectRepository subjectRepository;
    private final TopicRepository topicRepository;

    public SubjectController(
            SubjectRepository subjectRepository,
            TopicRepository topicRepository) {

        this.subjectRepository = subjectRepository;
        this.topicRepository = topicRepository;
    }


    // ==============================
    // SUBJECTS HOME
    // ==============================

    @GetMapping("/subjects")
    public String subjects(Model model) {

        model.addAttribute(
                "subjects",
                subjectRepository.findAll()
        );

        return "subjects";
    }


    // ==============================
    // PRELIMS
    // ==============================

    @GetMapping("/prelims")
    public String prelims() {

        return "prelims";
    }


    // ==============================
    // PRELIMS GENERAL STUDIES
    // ==============================

    @GetMapping("/prelims-gs")
    public String prelimsGS(Model model) {

        // ==========================================
        // GET ONLY PRELIMS SUBJECTS
        // ==========================================

        List<Subject> subjects =
                subjectRepository.findByExamType(
                        ExamType.PRELIMS
                );


        // ==========================================
        // EXCLUDE:
        // 1. Current Affairs - ID 8
        // 2. CSAT - ID 15
        // ==========================================

        subjects = subjects.stream()
                .filter(subject ->
                        subject.getId() != null
                                && !subject.getId().equals(8L)
                                && !subject.getId().equals(15L)
                )
                .toList();


        // ==========================================
        // MAPS FOR SUBJECT PROGRESS
        // ==========================================

        Map<Long, Integer> subjectProgress =
                new HashMap<>();

        Map<Long, Boolean> subjectCompleted =
                new HashMap<>();


        // ==========================================
        // OVERALL PRELIMS GS VALUES
        // ==========================================

        int totalPrelimsTopics = 0;

        long completedPrelimsTopics = 0;


        // ==========================================
        // GET ALL TOPICS ONCE
        // ==========================================

        List<Topic> allTopics =
                topicRepository.findAll();


        // ==========================================
        // CALCULATE EACH GS SUBJECT
        // ==========================================

        for (Subject subject : subjects) {

            List<Topic> topics =
                    allTopics.stream()
                            .filter(topic ->
                                    topic.getSubject() != null
                                            && topic.getSubject()
                                            .getId()
                                            .equals(
                                                    subject.getId()
                                            )
                            )
                            .toList();


            // ======================================
            // SUBJECT TOPIC COUNTS
            // ======================================

            int totalTopics =
                    topics.size();

            long completedTopics =
                    topics.stream()
                            .filter(Topic::isCompleted)
                            .count();


            // ======================================
            // ADD TO OVERALL GS COUNT
            // ======================================

            totalPrelimsTopics +=
                    totalTopics;

            completedPrelimsTopics +=
                    completedTopics;


            // ======================================
            // SUBJECT PROGRESS
            // ======================================

            int progress =
                    totalTopics == 0
                            ? 0
                            : (int) (
                            (completedTopics * 100)
                            / totalTopics
                    );


            // ======================================
            // SUBJECT COMPLETION
            // ======================================

            boolean completed =
                    totalTopics > 0
                            && completedTopics
                            == totalTopics;


            subjectProgress.put(
                    subject.getId(),
                    progress
            );

            subjectCompleted.put(
                    subject.getId(),
                    completed
            );
        }


        // ==========================================
        // OVERALL PRELIMS GS PROGRESS
        // ==========================================

        int overallProgress =
                totalPrelimsTopics == 0
                        ? 0
                        : (int) (
                        (completedPrelimsTopics * 100)
                        / totalPrelimsTopics
                );


        // ==========================================
        // OVERALL GS COMPLETION
        // ==========================================

        boolean prelimsCompleted =
                totalPrelimsTopics > 0
                        && completedPrelimsTopics
                        == totalPrelimsTopics;


        // ==========================================
        // SEND DATA TO HTML
        // ==========================================

        model.addAttribute(
                "subjects",
                subjects
        );

        model.addAttribute(
                "subjectProgress",
                subjectProgress
        );

        model.addAttribute(
                "subjectCompleted",
                subjectCompleted
        );

        model.addAttribute(
                "overallProgress",
                overallProgress
        );

        model.addAttribute(
                "totalPrelimsTopics",
                totalPrelimsTopics
        );

        model.addAttribute(
                "completedPrelimsTopics",
                completedPrelimsTopics
        );

        model.addAttribute(
                "prelimsCompleted",
                prelimsCompleted
        );


        return "prelims-gs";
    }


    // ==============================
    // MAINS
    // ==============================

    @GetMapping("/mains")
    public String mains(Model model) {

        model.addAttribute(
                "mainsSubjects",
                subjectRepository.findByExamType(
                        ExamType.MAINS
                )
        );

        return "mains";
    }


    // ==============================
    // ADD SUBJECT
    // ==============================

    @GetMapping("/add-subject")
    public String addSubjectPage(Model model) {

        model.addAttribute(
                "subject",
                new Subject()
        );

        return "add-subject";
    }


    // ==============================
    // SAVE SUBJECT
    // ==============================

    @PostMapping("/save-subject")
    public String saveSubject(
            @ModelAttribute Subject subject) {

        subjectRepository.save(subject);

        return "redirect:/subjects";
    }
}