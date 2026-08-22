
        package com.upsc.upsctracker.controller;

import com.upsc.upsctracker.entity.Section;
import com.upsc.upsctracker.entity.Subject;
import com.upsc.upsctracker.entity.Topic;
import com.upsc.upsctracker.repository.SectionRepository;
import com.upsc.upsctracker.repository.SubjectRepository;
import com.upsc.upsctracker.repository.TopicRepository;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@Controller
public class CurrentAffairsController {

    private final SectionRepository sectionRepository;
    private final SubjectRepository subjectRepository;
    private final TopicRepository topicRepository;

    public CurrentAffairsController(
            SectionRepository sectionRepository,
            SubjectRepository subjectRepository,
            TopicRepository topicRepository) {

        this.sectionRepository = sectionRepository;
        this.subjectRepository = subjectRepository;
        this.topicRepository = topicRepository;
    }


    // =========================================================
    // CURRENT AFFAIRS MAIN PAGE
    // =========================================================

    @GetMapping("/current-affairs")
    public String currentAffairs() {

        return "current-affairs";
    }


    // =========================================================
    // SELECT ATTEMPT YEAR
    // ONE YEAR FOR PRELIMS + MAINS + 365
    // =========================================================

    @PostMapping("/current-affairs")
    public String selectAttemptYear(
            @RequestParam("attemptYear") Integer attemptYear,
            Model model) {

        model.addAttribute(
                "attemptYear",
                attemptYear
        );

        return "current-affairs";
    }


    // =========================================================
    // PRELIMS PAGE
    // =========================================================

    @GetMapping("/current-affairs/prelims")
    public String showPrelims(
            @RequestParam("attemptYear") Integer attemptYear,
            Model model) {

        Subject subject = subjectRepository
                .findById(8L)
                .orElseThrow();

        int previousYear = attemptYear - 1;


        String[] months = {
                "January",
                "February",
                "March",
                "April",
                "May",
                "June",
                "July",
                "August",
                "September",
                "October",
                "November",
                "December"
        };


        List<Section> sections =
                new ArrayList<>();


        // =====================================================
        // PREVIOUS YEAR
        // JANUARY - DECEMBER
        // =====================================================

        for (String month : months) {

            Section section =
                    getOrCreateSection(
                            month,
                            previousYear,
                            subject
                    );

            sections.add(section);
        }


        // =====================================================
        // ATTEMPT YEAR
        // JANUARY - MAY
        // =====================================================

        for (int i = 0; i < 5; i++) {

            Section section =
                    getOrCreateSection(
                            months[i],
                            attemptYear,
                            subject
                    );

            sections.add(section);
        }


        // =====================================================
        // CREATE 14 PRELIMS SECTIONS
        // =====================================================

        List<Integer> completedCounts =
                new ArrayList<>();


        for (Section section : sections) {

            createPrelimsSections(section);


            List<Topic> topics =
                    getTopicsForSection(
                            section.getId()
                    );


            int completed = 0;


            for (Topic topic : topics) {

                if (topic.isCompleted()) {

                    completed++;
                }
            }


            completedCounts.add(
                    completed
            );
        }


        model.addAttribute(
                "attemptYear",
                attemptYear
        );

        model.addAttribute(
                "sections",
                sections
        );

        model.addAttribute(
                "completedCounts",
                completedCounts
        );


        return "prelims-current-affairs";
    }


    // =========================================================
    // OPEN PRELIMS MONTH
    // =========================================================

    @GetMapping(
            "/current-affairs/prelims/month/{sectionId}"
    )
    public String openPrelimsMonth(
            @PathVariable("sectionId") Long sectionId,
            Model model) {

        Section section =
                sectionRepository
                        .findById(sectionId)
                        .orElseThrow();


        createPrelimsSections(section);


        List<Topic> topics =
                getTopicsForSection(
                        sectionId
                );


        model.addAttribute(
                "section",
                section
        );


        model.addAttribute(
                "topics",
                topics
        );


        return "prelims-current-affairs-topics";
    }


    // =========================================================
    // TOGGLE COMPLETION
    // =========================================================

    @PostMapping(
            "/current-affairs/toggle/{topicId}"
    )
    public String toggleCompletion(
            @PathVariable("topicId") Long topicId,
            @RequestParam(
                    value = "type",
                    defaultValue = "prelims"
            ) String type) {

        Topic topic =
                topicRepository
                        .findById(topicId)
                        .orElseThrow();


        // Toggle
        topic.setCompleted(
                !topic.isCompleted()
        );


        topicRepository.save(topic);


        Section section =
                topic.getSection();


        // =====================================================
        // MAINS
        // =====================================================

        if ("mains".equalsIgnoreCase(type)) {

            return "redirect:/current-affairs/mains?attemptYear="
                    + section.getYear();
        }


        // =====================================================
        // 365
        // =====================================================

        if ("365".equalsIgnoreCase(type)) {

            return "redirect:/current-affairs/365?attemptYear="
                    + section.getYear();
        }


        // =====================================================
        // PRELIMS
        // =====================================================

        return "redirect:/current-affairs/prelims/month/"
                + section.getId();
    }


    // =========================================================
    // MAINS PAGE
    // =========================================================

    @GetMapping("/current-affairs/mains")
    public String showMains(
            @RequestParam("attemptYear") Integer attemptYear,
            Model model) {

        Subject subject =
                subjectRepository
                        .findById(8L)
                        .orElseThrow();


        List<Section> mainsSections =
                new ArrayList<>();


        // =====================================================
        // JANUARY - MAY REVISION
        // =====================================================

        Section revision =
                getOrCreateSection(
                        "January - May Revision",
                        attemptYear,
                        subject
                );


        mainsSections.add(
                revision
        );


        // =====================================================
        // MAY - SEPTEMBER
        // =====================================================

        String[] months = {
                "May",
                "June",
                "July",
                "August",
                "September"
        };


        for (String month : months) {

            Section section =
                    getOrCreateSection(
                            month,
                            attemptYear,
                            subject
                    );


            mainsSections.add(
                    section
            );
        }


        // =====================================================
        // STATUS TOPICS
        // =====================================================

        List<Topic> mainsStatus =
                new ArrayList<>();


        for (Section section : mainsSections) {

            Topic topic =
                    getOrCreateMainsStatus(
                            section
                    );


            mainsStatus.add(
                    topic
            );
        }


        model.addAttribute(
                "attemptYear",
                attemptYear
        );


        model.addAttribute(
                "mainsSections",
                mainsSections
        );


        model.addAttribute(
                "mainsStatus",
                mainsStatus
        );


        return "mains-current-affairs";
    }


    // =========================================================
    // 365 DAYS PAGE
    // =========================================================

    @GetMapping("/current-affairs/365")
    public String show365(
            @RequestParam("attemptYear") Integer attemptYear,
            Model model) {

        Subject subject =
                subjectRepository
                        .findById(8L)
                        .orElseThrow();


        Section section =
                getOrCreateSection(
                        "365 Days Revision",
                        attemptYear,
                        subject
                );


        Topic topic =
                getOrCreate365Status(
                        section
                );


        model.addAttribute(
                "attemptYear",
                attemptYear
        );


        model.addAttribute(
                "section",
                section
        );


        model.addAttribute(
                "topic",
                topic
        );


        return "365-current-affairs";
    }


    // =========================================================
    // CREATE PRELIMS SECTIONS
    // =========================================================

    private void createPrelimsSections(
            Section section) {


        String[] sectionNames = {

                "Polity",

                "Governance",

                "Economy",

                "Environment",

                "International Relations",

                "Science and Technology",

                "Security",

                "Social Issues",

                "Art and Culture",

                "Geography",

                "Agriculture",

                "Reports and Indices",

                "Government Schemes",

                "Miscellaneous"
        };


        List<Topic> existingTopics =
                getTopicsForSection(
                        section.getId()
                );


        for (String sectionName : sectionNames) {


            boolean exists = false;


            for (Topic topic : existingTopics) {


                if (topic.getTopicName() != null
                        && topic.getTopicName()
                        .equalsIgnoreCase(
                                sectionName
                        )) {

                    exists = true;

                    break;
                }
            }


            if (!exists) {


                Topic topic =
                        new Topic();


                topic.setTopicName(
                        sectionName
                );


                topic.setCompleted(
                        false
                );


                topic.setSubject(
                        section.getSubject()
                );


                topic.setSection(
                        section
                );


                topicRepository.save(
                        topic
                );
            }
        }
    }


    // =========================================================
    // CREATE MAINS STATUS
    // =========================================================

    private Topic getOrCreateMainsStatus(
            Section section) {


        List<Topic> topics =
                getTopicsForSection(
                        section.getId()
                );


        for (Topic topic : topics) {


            if (topic.getTopicName() != null
                    && topic.getTopicName()
                    .equalsIgnoreCase(
                            "Mains Status"
                    )) {

                return topic;
            }
        }


        Topic topic =
                new Topic();


        topic.setTopicName(
                "Mains Status"
        );


        topic.setCompleted(
                false
        );


        topic.setSubject(
                section.getSubject()
        );


        topic.setSection(
                section
        );


        return topicRepository.save(
                topic
        );
    }


    // =========================================================
    // CREATE 365 STATUS
    // =========================================================

    private Topic getOrCreate365Status(
            Section section) {


        List<Topic> topics =
                getTopicsForSection(
                        section.getId()
                );


        for (Topic topic : topics) {


            if (topic.getTopicName() != null
                    && topic.getTopicName()
                    .equalsIgnoreCase(
                            "365 Days Status"
                    )) {

                return topic;
            }
        }


        Topic topic =
                new Topic();


        topic.setTopicName(
                "365 Days Status"
        );


        topic.setCompleted(
                false
        );


        topic.setSubject(
                section.getSubject()
        );


        topic.setSection(
                section
        );


        return topicRepository.save(
                topic
        );
    }


    // =========================================================
    // GET TOPICS FOR SECTION
    // =========================================================

    private List<Topic> getTopicsForSection(
            Long sectionId) {


        return topicRepository
                .findAll()
                .stream()
                .filter(topic ->

                        topic.getSection() != null

                                && topic.getSection()
                                .getId() != null

                                && topic.getSection()
                                .getId()
                                .equals(sectionId)

                )
                .toList();
    }


    // =========================================================
    // FIND OR CREATE SECTION
    // =========================================================

    private Section getOrCreateSection(
            String name,
            int year,
            Subject subject) {


        List<Section> sections =
                sectionRepository
                        .findBySubjectIdAndYear(
                                subject.getId(),
                                year
                        );


        for (Section section : sections) {


            if (section.getName() != null
                    && section.getName()
                    .equalsIgnoreCase(
                            name
                    )) {

                return section;
            }
        }


        Section section =
                new Section();


        section.setName(
                name
        );


        section.setYear(
                year
        );


        section.setSubject(
                subject
        );


        return sectionRepository.save(
                section
        );
    }
}
