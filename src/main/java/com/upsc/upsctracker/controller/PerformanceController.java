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

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Controller
public class PerformanceController {

    private final SubjectRepository subjectRepository;
    private final TopicRepository topicRepository;

    public PerformanceController(
            SubjectRepository subjectRepository,
            TopicRepository topicRepository) {

        this.subjectRepository = subjectRepository;
        this.topicRepository = topicRepository;
    }

    @GetMapping("/performance")
    public String performance(
            HttpSession session,
            Model model) {

        // =========================
        // LOGIN CHECK
        // =========================

        Object userId =
                session.getAttribute("loggedInUserId");

        if (userId == null) {
            return "redirect:/login";
        }

        String userName =
                (String) session.getAttribute(
                        "loggedInUserName"
                );

        Integer attemptYear =
                (Integer) session.getAttribute(
                        "attemptYear"
                );

        if (attemptYear == null) {
            attemptYear = 2027;
        }


        // =========================
        // GET DATA
        // =========================

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
                getTopicsForSubjects(
                        allTopics,
                        prelimsSubjects
                );

        int prelimsTotal =
                prelimsTopics.size();

        long prelimsCompleted =
                countCompleted(prelimsTopics);

        int prelimsPending =
                prelimsTotal
                        - (int) prelimsCompleted;

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
                getTopicsForSubjects(
                        allTopics,
                        mainsSubjects
                );

        int mainsTotal =
                mainsTopics.size();

        long mainsCompleted =
                countCompleted(mainsTopics);

        int mainsPending =
                mainsTotal
                        - (int) mainsCompleted;

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
                countCompleted(csatTopics);

        int csatPending =
                csatTotal
                        - (int) csatCompleted;

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
                countCompleted(
                        currentAffairsTopics
                );

        int currentAffairsPending =
                currentAffairsTotal
                        - (int) currentAffairsCompleted;

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

        int totalPending =
                totalTopics
                        - (int) totalCompleted;

        int overallProgress =
                calculateProgress(
                        totalCompleted,
                        totalTopics
                );


        // =========================
        // SUBJECT PERFORMANCE
        // =========================

        List<SubjectPerformance> prelimsPerformance =
                buildSubjectPerformance(
                        prelimsSubjects,
                        allTopics
                );

        List<SubjectPerformance> mainsPerformance =
                buildSubjectPerformance(
                        mainsSubjects,
                        allTopics
                );


        // =========================
        // MODEL
        // =========================

        model.addAttribute(
                "userName",
                userName
        );

        model.addAttribute(
                "attemptYear",
                attemptYear
        );


        // Overall

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
                "totalPending",
                totalPending
        );


        // Prelims

        model.addAttribute(
                "prelimsProgress",
                prelimsProgress
        );

        model.addAttribute(
                "prelimsTotal",
                prelimsTotal
        );

        model.addAttribute(
                "prelimsCompleted",
                prelimsCompleted
        );

        model.addAttribute(
                "prelimsPending",
                prelimsPending
        );


        // Mains

        model.addAttribute(
                "mainsProgress",
                mainsProgress
        );

        model.addAttribute(
                "mainsTotal",
                mainsTotal
        );

        model.addAttribute(
                "mainsCompleted",
                mainsCompleted
        );

        model.addAttribute(
                "mainsPending",
                mainsPending
        );


        // CSAT

        model.addAttribute(
                "csatProgress",
                csatProgress
        );

        model.addAttribute(
                "csatTotal",
                csatTotal
        );

        model.addAttribute(
                "csatCompleted",
                csatCompleted
        );

        model.addAttribute(
                "csatPending",
                csatPending
        );


        // Current Affairs

        model.addAttribute(
                "currentAffairsProgress",
                currentAffairsProgress
        );

        model.addAttribute(
                "currentAffairsTotal",
                currentAffairsTotal
        );

        model.addAttribute(
                "currentAffairsCompleted",
                currentAffairsCompleted
        );

        model.addAttribute(
                "currentAffairsPending",
                currentAffairsPending
        );


        // Subject performance

        model.addAttribute(
                "prelimsPerformance",
                prelimsPerformance
        );

        model.addAttribute(
                "mainsPerformance",
                mainsPerformance
        );


        return "performance";
    }


    // =====================================================
    // GET TOPICS FOR SUBJECTS
    // =====================================================

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


    // =====================================================
    // COUNT COMPLETED
    // =====================================================

    private long countCompleted(
            List<Topic> topics) {

        return topics.stream()
                .filter(Topic::isCompleted)
                .count();
    }


    // =====================================================
    // CALCULATE PROGRESS
    // =====================================================

    private int calculateProgress(
            long completed,
            int total) {

        if (total == 0) {
            return 0;
        }

        return (int)
                ((completed * 100) / total);
    }


    // =====================================================
    // SUBJECT PERFORMANCE
    // =====================================================

    private List<SubjectPerformance> buildSubjectPerformance(
            List<Subject> subjects,
            List<Topic> allTopics) {

        List<SubjectPerformance> result =
                new ArrayList<>();

        for (Subject subject : subjects) {

            List<Topic> subjectTopics =
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

            int total =
                    subjectTopics.size();

            long completed =
                    countCompleted(subjectTopics);

            int pending =
                    total
                            - (int) completed;

            int progress =
                    calculateProgress(
                            completed,
                            total
                    );

            result.add(
                    new SubjectPerformance(
                            subject.getName(),
                            total,
                            completed,
                            pending,
                            progress
                    )
            );
        }

        result.sort(
                Comparator.comparing(
                        SubjectPerformance::getProgress
                ).reversed()
        );

        return result;
    }


    // =====================================================
    // INNER CLASS
    // =====================================================

    public static class SubjectPerformance {

        private final String subjectName;
        private final int total;
        private final long completed;
        private final int pending;
        private final int progress;


        public SubjectPerformance(
                String subjectName,
                int total,
                long completed,
                int pending,
                int progress) {

            this.subjectName = subjectName;
            this.total = total;
            this.completed = completed;
            this.pending = pending;
            this.progress = progress;
        }


        public String getSubjectName() {
            return subjectName;
        }

        public int getTotal() {
            return total;
        }

        public long getCompleted() {
            return completed;
        }

        public int getPending() {
            return pending;
        }

        public int getProgress() {
            return progress;
        }
    }
}