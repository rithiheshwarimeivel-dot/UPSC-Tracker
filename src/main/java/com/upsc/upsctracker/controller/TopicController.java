package com.upsc.upsctracker.controller;

import com.upsc.upsctracker.entity.Section;
import com.upsc.upsctracker.entity.Subtopic;
import com.upsc.upsctracker.entity.Topic;
import com.upsc.upsctracker.repository.SectionRepository;
import com.upsc.upsctracker.repository.SubtopicRepository;
import com.upsc.upsctracker.repository.TopicRepository;
import com.upsc.upsctracker.service.RevisionService;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
public class TopicController {

    private final TopicRepository topicRepository;
    private final SubtopicRepository subtopicRepository;
    private final SectionRepository sectionRepository;
    private final RevisionService revisionService;

    public TopicController(
            TopicRepository topicRepository,
            SubtopicRepository subtopicRepository,
            SectionRepository sectionRepository,
            RevisionService revisionService) {

        this.topicRepository = topicRepository;
        this.subtopicRepository = subtopicRepository;
        this.sectionRepository = sectionRepository;
        this.revisionService = revisionService;
    }


    // ==================================================
    // SHOW TOPICS FOR A SECTION
    // URL: /topics/{sectionId}
    // ==================================================

    @GetMapping("/topics/{sectionId}")
    public String showTopics(
            @PathVariable Long sectionId,
            Model model) {

        Section section =
                sectionRepository
                        .findById(sectionId)
                        .orElseThrow();

        List<Topic> topics =
                topicRepository
                        .findBySectionId(sectionId);

        long completedCount =
                topics.stream()
                        .filter(Topic::isCompleted)
                        .count();

        int totalTopics =
                topics.size();

        int progress =
                totalTopics == 0
                        ? 0
                        : (int) (
                        (completedCount * 100)
                        / totalTopics
                );

        model.addAttribute(
                "section",
                section
        );

        model.addAttribute(
                "topics",
                topics
        );

        model.addAttribute(
                "completedCount",
                completedCount
        );

        model.addAttribute(
                "totalTopics",
                totalTopics
        );

        model.addAttribute(
                "progress",
                progress
        );

        return "topics";
    }


    // ==================================================
    // OPEN PARTICULAR TOPIC
    // SHOW ITS SUBTOPICS
    // URL: /topic/{topicId}
    // ==================================================

    @GetMapping("/topic/{topicId}")
    public String openTopic(
            @PathVariable Long topicId,
            Model model) {

        Topic topic =
                topicRepository
                        .findById(topicId)
                        .orElseThrow();

        List<Subtopic> subtopics =
                subtopicRepository
                        .findByTopicId(topicId);

        // ==================================================
        // GET PARENT SECTION
        // ==================================================

        Section section =
                topic.getSection();

        // ==================================================
        // SEND DATA TO HTML
        // ==================================================

        model.addAttribute(
                "topic",
                topic
        );

        model.addAttribute(
                "subtopics",
                subtopics
        );

        model.addAttribute(
                "section",
                section
        );

        return "topic-subtopics";
    }


    // ==================================================
    // TOGGLE SUBTOPIC COMPLETION
    // ==================================================

    @PostMapping("/toggle-subtopic/{id}")
    public String toggleSubtopic(
            @PathVariable Long id) {

        Subtopic subtopic =
                subtopicRepository
                        .findById(id)
                        .orElseThrow();

        // ==================================================
        // TOGGLE SUBTOPIC
        // ==================================================

        subtopic.setCompleted(
                !subtopic.isCompleted()
        );

        subtopicRepository.save(
                subtopic
        );

        // ==================================================
        // GET PARENT TOPIC
        // ==================================================

        Topic topic =
                subtopic.getTopic();

        // ==================================================
        // GET ALL SUBTOPICS
        // ==================================================

        List<Subtopic> subtopics =
                subtopicRepository
                        .findByTopicId(
                                topic.getId()
                        );

        // ==================================================
        // AUTOMATIC TOPIC COMPLETION
        // ==================================================

        boolean allCompleted =
                !subtopics.isEmpty()
                        &&
                        subtopics.stream()
                                .allMatch(
                                        Subtopic::isCompleted
                                );

        boolean wasCompleted =
                topic.isCompleted();

        topic.setCompleted(
                allCompleted
        );

        topicRepository.save(
                topic
        );


        // ==================================================
        // AUTOMATIC REVISION CREATION
        //
        // Topic becomes completed:
        // Create Revision 1 automatically
        // ==================================================

        if (!wasCompleted && allCompleted) {

            revisionService.createRevisionForTopic(
                    topic
            );
        }


        // ==================================================
        // TOPIC BECOMES INCOMPLETE
        //
        // Remove revision because topic is no longer completed
        // ==================================================

        if (wasCompleted && !allCompleted) {

            revisionService.removeRevisionForTopic(
                    topic.getId()
            );
        }


        return "redirect:/topic/"
                + topic.getId();
    }


    // ==================================================
    // DIRECTLY COMPLETE TOPIC
    // ONLY FOR TOPICS WITH NO SUBTOPICS
    // ==================================================

    @PostMapping("/complete-topic/{id}")
    public String completeTopic(
            @PathVariable Long id) {

        Topic topic =
                topicRepository
                        .findById(id)
                        .orElseThrow();

        List<Subtopic> subtopics =
                subtopicRepository
                        .findByTopicId(id);

        if (subtopics.isEmpty()) {

            boolean wasCompleted =
                    topic.isCompleted();

            topic.setCompleted(true);

            topicRepository.save(
                    topic
            );


            // ==================================================
            // AUTOMATIC REVISION CREATION
            // ==================================================

            if (!wasCompleted) {

                revisionService.createRevisionForTopic(
                        topic
                );
            }
        }

        return "redirect:/topic/" + id;
    }


    // ==================================================
    // MARK TOPIC INCOMPLETE
    // ONLY FOR TOPICS WITH NO SUBTOPICS
    // ==================================================

    @PostMapping("/incomplete-topic/{id}")
    public String incompleteTopic(
            @PathVariable Long id) {

        Topic topic =
                topicRepository
                        .findById(id)
                        .orElseThrow();

        List<Subtopic> subtopics =
                subtopicRepository
                        .findByTopicId(id);

        if (subtopics.isEmpty()) {

            boolean wasCompleted =
                    topic.isCompleted();

            topic.setCompleted(false);

            topicRepository.save(
                    topic
            );


            // ==================================================
            // REMOVE REVISION
            // TOPIC IS NO LONGER COMPLETED
            // ==================================================

            if (wasCompleted) {

                revisionService.removeRevisionForTopic(
                        topic.getId()
                );
            }
        }

        return "redirect:/topic/" + id;
    }

}