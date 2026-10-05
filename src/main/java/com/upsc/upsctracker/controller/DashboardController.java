package com.upsc.upsctracker.controller;

import com.upsc.upsctracker.entity.ExamType;
import com.upsc.upsctracker.entity.Subject;
import com.upsc.upsctracker.entity.Topic;
import com.upsc.upsctracker.repository.SubjectRepository;
import com.upsc.upsctracker.repository.TopicRepository;

import jakarta.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class DashboardController {

    private final SubjectRepository subjectRepository;
    private final TopicRepository topicRepository;

    public DashboardController(
            SubjectRepository subjectRepository,
            TopicRepository topicRepository) {

        this.subjectRepository = subjectRepository;
        this.topicRepository = topicRepository;
    }

    @GetMapping("/dashboard")
    public String dashboard(
            HttpSession session,
            Model model) {

        // Check login
        Object userId =
                session.getAttribute("loggedInUserId");

        if (userId == null) {
            return "redirect:/login";
        }

        // User details
        String userName =
                (String) session.getAttribute(
                        "loggedInUserName"
                );

        Integer attemptYear =
                (Integer) session.getAttribute(
                        "attemptYear"
                );

        // If attempt year is not available
        if (attemptYear == null) {
            attemptYear = 2027;
        }

        // All subjects and topics
        List<Subject> allSubjects =
                subjectRepository.findAll();

        List<Topic> allTopics =
                topicRepository.findAll();

        // =========================
        // PRELIMS
        // =========================

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
                allTopics.stream()
                        .filter(topic ->
                                topic.getSubject() != null
                                        && prelimsSubjects.stream()
                                        .anyMatch(subject ->
                                                subject.getId()
                                                        .equals(
                                                                topic.getSubject()
                                                                        .getId()
                                                        )
                                        )
                        )
                        .toList();

        int prelimsTotal =
                prelimsTopics.size();

        long prelimsCompleted =
                prelimsTopics.stream()
                        .filter(Topic::isCompleted)
                        .count();

        int prelimsProgress =
                calculateProgress(
                        prelimsCompleted,
                        prelimsTotal
                );


        // =========================
        // MAINS
        // =========================

        List<Subject> mainsSubjects =
                allSubjects.stream()
                        .filter(subject ->
                                subject.getExamType()
                                        == ExamType.MAINS)
                        .toList();

        List<Topic> mainsTopics =
                allTopics.stream()
                        .filter(topic ->
                                topic.getSubject() != null
                                        && mainsSubjects.stream()
                                        .anyMatch(subject ->
                                                subject.getId()
                                                        .equals(
                                                                topic.getSubject()
                                                                        .getId()
                                                        )
                                        )
                        )
                        .toList();

        int mainsTotal =
                mainsTopics.size();

        long mainsCompleted =
                mainsTopics.stream()
                        .filter(Topic::isCompleted)
                        .count();

        int mainsProgress =
                calculateProgress(
                        mainsCompleted,
                        mainsTotal
                );


        // =========================
        // CSAT
        // =========================

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
                csatTopics.stream()
                        .filter(Topic::isCompleted)
                        .count();

        int csatProgress =
                calculateProgress(
                        csatCompleted,
                        csatTotal
                );


        // =========================
        // CURRENT AFFAIRS
        // =========================

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
                currentAffairsTopics.stream()
                        .filter(Topic::isCompleted)
                        .count();

        int currentAffairsProgress =
                calculateProgress(
                        currentAffairsCompleted,
                        currentAffairsTotal
                );


        // =========================
        // OVERALL
        // =========================

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


        // =========================
        // SEND TO HTML
        // =========================

        model.addAttribute(
                "userName",
                userName
        );

        model.addAttribute(
                "attemptYear",
                attemptYear
        );

        model.addAttribute(
                "prelimsProgress",
                prelimsProgress
        );

        model.addAttribute(
                "prelimsCompleted",
                prelimsCompleted
        );

        model.addAttribute(
                "prelimsTotal",
                prelimsTotal
        );

        model.addAttribute(
                "mainsProgress",
                mainsProgress
        );

        model.addAttribute(
                "mainsCompleted",
                mainsCompleted
        );

        model.addAttribute(
                "mainsTotal",
                mainsTotal
        );

        model.addAttribute(
                "csatProgress",
                csatProgress
        );

        model.addAttribute(
                "csatCompleted",
                csatCompleted
        );

        model.addAttribute(
                "csatTotal",
                csatTotal
        );

        model.addAttribute(
                "currentAffairsProgress",
                currentAffairsProgress
        );

        model.addAttribute(
                "currentAffairsCompleted",
                currentAffairsCompleted
        );

        model.addAttribute(
                "currentAffairsTotal",
                currentAffairsTotal
        );

        model.addAttribute(
                "overallProgress",
                overallProgress
        );

        model.addAttribute(
                "totalCompleted",
                totalCompleted
        );

        model.addAttribute(
                "totalTopics",
                totalTopics
        );

        return "dashboard";
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
}