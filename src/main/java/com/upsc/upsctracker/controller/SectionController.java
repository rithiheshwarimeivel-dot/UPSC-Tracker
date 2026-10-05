package com.upsc.upsctracker.controller;

import com.upsc.upsctracker.entity.ExamType;
import com.upsc.upsctracker.entity.Section;
import com.upsc.upsctracker.entity.Subject;
import com.upsc.upsctracker.entity.Topic;
import com.upsc.upsctracker.repository.SectionRepository;
import com.upsc.upsctracker.repository.SubjectRepository;
import com.upsc.upsctracker.repository.TopicRepository;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class SectionController {

    private final SectionRepository sectionRepository;
    private final SubjectRepository subjectRepository;
    private final TopicRepository topicRepository;

    public SectionController(
            SectionRepository sectionRepository,
            SubjectRepository subjectRepository,
            TopicRepository topicRepository) {

        this.sectionRepository = sectionRepository;
        this.subjectRepository = subjectRepository;
        this.topicRepository = topicRepository;
    }


    // =========================================================
    // NORMAL SUBJECT → SECTIONS
    // =========================================================

    @GetMapping("/sections/{subjectId}")
    public String showSections(
            @PathVariable Long subjectId,
            Model model) {

        Subject subject = subjectRepository
                .findById(subjectId)
                .orElseThrow();

        List<Section> sections =
                sectionRepository.findBySubjectId(subjectId);

        List<Topic> allTopics =
                topicRepository.findAll()
                        .stream()
                        .filter(topic ->
                                topic.getSubject() != null &&
                                        topic.getSubject()
                                                .getId()
                                                .equals(subjectId))
                        .toList();

        Map<Long, Integer> sectionProgress =
                new HashMap<>();

        Map<Long, Boolean> sectionCompleted =
                new HashMap<>();

        for (Section section : sections) {

            List<Topic> sectionTopics =
                    allTopics.stream()
                            .filter(topic ->
                                    topic.getSection() != null &&
                                            topic.getSection()
                                                    .getId()
                                                    .equals(section.getId()))
                            .toList();

            long completed =
                    sectionTopics.stream()
                            .filter(Topic::isCompleted)
                            .count();

            int total = sectionTopics.size();

            int progress =
                    total == 0
                            ? 0
                            : (int) ((completed * 100) / total);

            sectionProgress.put(
                    section.getId(),
                    progress
            );

            sectionCompleted.put(
                    section.getId(),
                    total > 0 && completed == total
            );
        }

        long completedTopics =
                allTopics.stream()
                        .filter(Topic::isCompleted)
                        .count();

        int totalTopics = allTopics.size();

        int subjectProgress =
                totalTopics == 0
                        ? 0
                        : (int)
                          ((completedTopics * 100) / totalTopics);

        model.addAttribute(
                "subject",
                subject
        );

        model.addAttribute(
                "sections",
                sections
        );

        model.addAttribute(
                "sectionProgress",
                sectionProgress
        );

        model.addAttribute(
                "sectionCompleted",
                sectionCompleted
        );

        model.addAttribute(
                "subjectProgress",
                subjectProgress
        );

        // =====================================================
        // IMPORTANT:
        // TRUE  → Mains subject
        // FALSE → Prelims subject
        // =====================================================

        model.addAttribute(
                "isMains",
                subject.getExamType() == ExamType.MAINS
        );

        model.addAttribute(
                "csat",
                false
        );

        model.addAttribute(
                "categoryPage",
                false
        );

        return "sections";
    }


    // =========================================================
    // CSAT CATEGORY → SECTIONS
    //
    // Example:
    // /sections/15/category/Aptitude
    // =========================================================

    @GetMapping("/sections/{subjectId}/category/{category}")
    public String showCategorySections(
            @PathVariable Long subjectId,
            @PathVariable String category,
            Model model) {

        Subject subject = subjectRepository
                .findById(subjectId)
                .orElseThrow();

        List<Section> sections =
                sectionRepository
                        .findBySubjectId(subjectId)
                        .stream()
                        .filter(section ->
                                category.equals(
                                        section.getCategory()))
                        .toList();

        List<Topic> allTopics =
                topicRepository.findAll()
                        .stream()
                        .filter(topic ->
                                topic.getSubject() != null &&
                                        topic.getSubject()
                                                .getId()
                                                .equals(subjectId))
                        .toList();

        Map<Long, Integer> sectionProgress =
                new HashMap<>();

        Map<Long, Boolean> sectionCompleted =
                new HashMap<>();

        for (Section section : sections) {

            List<Topic> sectionTopics =
                    allTopics.stream()
                            .filter(topic ->
                                    topic.getSection() != null &&
                                            topic.getSection()
                                                    .getId()
                                                    .equals(section.getId()))
                            .toList();

            long completed =
                    sectionTopics.stream()
                            .filter(Topic::isCompleted)
                            .count();

            int total = sectionTopics.size();

            int progress =
                    total == 0
                            ? 0
                            : (int) ((completed * 100) / total);

            sectionProgress.put(
                    section.getId(),
                    progress
            );

            sectionCompleted.put(
                    section.getId(),
                    total > 0 && completed == total
            );
        }

        model.addAttribute(
                "subject",
                subject
        );

        model.addAttribute(
                "sections",
                sections
        );

        model.addAttribute(
                "category",
                category
        );

        model.addAttribute(
                "sectionProgress",
                sectionProgress
        );

        model.addAttribute(
                "sectionCompleted",
                sectionCompleted
        );

        model.addAttribute(
                "csat",
                false
        );

        model.addAttribute(
                "categoryPage",
                true
        );

        // CSAT category page-ku isMains false
        model.addAttribute(
                "isMains",
                false
        );

        return "sections";
    }
}