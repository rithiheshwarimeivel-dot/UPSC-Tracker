package com.upsc.upsctracker.controller;

import com.upsc.upsctracker.entity.Section;
import com.upsc.upsctracker.entity.Topic;
import com.upsc.upsctracker.repository.SectionRepository;
import com.upsc.upsctracker.repository.TopicRepository;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class CSATPageController {

    private final SectionRepository sectionRepository;
    private final TopicRepository topicRepository;

    public CSATPageController(
            SectionRepository sectionRepository,
            TopicRepository topicRepository) {

        this.sectionRepository = sectionRepository;
        this.topicRepository = topicRepository;
    }

    /*
     * ============================================================
     * MAIN CSAT PAGE
     * ============================================================
     *
     * URL:
     * /csat
     *
     * Shows only the three CSAT categories:
     * Aptitude
     * Reasoning
     * English
     */
    @GetMapping("/csat")
    public String showCSATPage(Model model) {

        model.addAttribute("selectedCategory", null);

        return loadCSATData(
                model,
                null
        );
    }


    /*
     * ============================================================
     * CATEGORY-SPECIFIC CSAT PAGE
     * ============================================================
     *
     * URLs:
     *
     * /csat/aptitude
     * /csat/reasoning
     * /csat/english
     *
     * Only the selected category's sections will be displayed.
     */
    @GetMapping("/csat/{category}")
    public String showCSATCategory(
            @PathVariable String category,
            Model model) {

        String normalizedCategory =
                category.trim().toLowerCase();

        if (!normalizedCategory.equals("aptitude")
                && !normalizedCategory.equals("reasoning")
                && !normalizedCategory.equals("english")) {

            return "redirect:/csat";
        }

        model.addAttribute(
                "selectedCategory",
                normalizedCategory
        );

        return loadCSATData(
                model,
                normalizedCategory
        );
    }


    /*
     * ============================================================
     * COMMON CSAT DATA LOADING
     * ============================================================
     */
    private String loadCSATData(
            Model model,
            String selectedCategory) {

        // CSAT subject ID = 15
        List<Section> allSections =
                sectionRepository.findBySubjectId(15L);


        /*
         * --------------------------------------------------------
         * CATEGORY FILTERING
         * --------------------------------------------------------
         */

        List<Section> aptitudeSections =
                allSections.stream()
                        .filter(section ->
                                "Aptitude".equalsIgnoreCase(
                                        section.getCategory()))
                        .toList();


        List<Section> reasoningSections =
                allSections.stream()
                        .filter(section ->
                                "Reasoning".equalsIgnoreCase(
                                        section.getCategory()))
                        .toList();


        List<Section> englishSections =
                allSections.stream()
                        .filter(section ->
                                "English".equalsIgnoreCase(
                                        section.getCategory()))
                        .toList();


        /*
         * --------------------------------------------------------
         * ALL CSAT TOPICS
         * --------------------------------------------------------
         */

        List<Topic> allTopics =
                topicRepository.findAll()
                        .stream()
                        .filter(topic ->
                                topic.getSubject() != null
                                        &&
                                        topic.getSubject()
                                                .getId()
                                                .equals(15L))
                        .toList();


        /*
         * --------------------------------------------------------
         * SECTION PROGRESS
         * --------------------------------------------------------
         */

        Map<Long, Integer> sectionProgress =
                new HashMap<>();

        Map<Long, Boolean> sectionCompleted =
                new HashMap<>();


        for (Section section : allSections) {

            List<Topic> sectionTopics =
                    allTopics.stream()
                            .filter(topic ->
                                    topic.getSection() != null
                                            &&
                                            topic.getSection()
                                                    .getId()
                                                    .equals(
                                                            section.getId()
                                                    ))
                            .toList();


            long completedTopics =
                    sectionTopics.stream()
                            .filter(Topic::isCompleted)
                            .count();


            int totalTopics =
                    sectionTopics.size();


            int progress =
                    totalTopics == 0
                            ? 0
                            : (int) (
                            (completedTopics * 100)
                            / totalTopics
                    );


            boolean completed =
                    totalTopics > 0
                            &&
                            completedTopics == totalTopics;


            sectionProgress.put(
                    section.getId(),
                    progress
            );


            sectionCompleted.put(
                    section.getId(),
                    completed
            );
        }


        /*
         * --------------------------------------------------------
         * SEND DATA TO THYMELEAF
         * --------------------------------------------------------
         */

        model.addAttribute(
                "aptitudeSections",
                aptitudeSections
        );

        model.addAttribute(
                "reasoningSections",
                reasoningSections
        );

        model.addAttribute(
                "englishSections",
                englishSections
        );

        model.addAttribute(
                "sectionProgress",
                sectionProgress
        );

        model.addAttribute(
                "sectionCompleted",
                sectionCompleted
        );


        return "csat";
    }
}