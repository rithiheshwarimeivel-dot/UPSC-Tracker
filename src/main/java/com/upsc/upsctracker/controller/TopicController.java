
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
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@Controller
public class TopicController {

    private final TopicRepository topicRepository;
    private final SubjectRepository subjectRepository;
    private final SectionRepository sectionRepository;

    public TopicController(
            TopicRepository topicRepository,
            SubjectRepository subjectRepository,
            SectionRepository sectionRepository) {

        this.topicRepository = topicRepository;
        this.subjectRepository = subjectRepository;
        this.sectionRepository = sectionRepository;
    }

    // --------------------------------------------------
    // SHOW ALL TOPICS
    // --------------------------------------------------

    @GetMapping("/topics")
    public String showTopics(Model model) {

        List<Topic> topics = topicRepository.findAll();

        List<Subject> subjects = new ArrayList<>();

        subjects.addAll(
                subjectRepository.findByExamType(ExamType.PRELIMS)
        );

        subjects.addAll(
                subjectRepository.findByExamType(ExamType.MAINS)
        );

        model.addAttribute("topics", topics);
        model.addAttribute("subjects", subjects);
        model.addAttribute("sections", sectionRepository.findAll());

        return "topics";
    }


    // --------------------------------------------------
    // SHOW TOPICS OF A PARTICULAR SECTION
    // --------------------------------------------------

    @GetMapping("/topics/{sectionId}")
    public String sectionTopics(
            @PathVariable Long sectionId,
            Model model) {

        Section section = sectionRepository
                .findById(sectionId)
                .orElseThrow();

        List<Topic> topics = topicRepository.findAll()
                .stream()
                .filter(topic ->
                        topic.getSection() != null
                                && topic.getSection().getId() != null
                                && topic.getSection().getId().equals(sectionId)
                )
                .toList();

        List<Subject> subjects = new ArrayList<>();

        subjects.addAll(
                subjectRepository.findByExamType(ExamType.PRELIMS)
        );

        subjects.addAll(
                subjectRepository.findByExamType(ExamType.MAINS)
        );

        model.addAttribute("section", section);
        model.addAttribute("topics", topics);
        model.addAttribute("subjects", subjects);
        model.addAttribute("sections", sectionRepository.findAll());

        return "topics";
    }


    // --------------------------------------------------
    // ADD TOPIC
    // --------------------------------------------------

    @PostMapping("/add-topic")
    public String addTopic(@ModelAttribute Topic topic) {

        if (topic.getSection() != null
                && topic.getSection().getId() != null) {

            Section section = sectionRepository
                    .findById(topic.getSection().getId())
                    .orElseThrow();

            topic.setSection(section);

            // Automatically assign subject from section
            topic.setSubject(section.getSubject());
        }

        topicRepository.save(topic);

        return "redirect:/topics";
    }


    // --------------------------------------------------
    // DELETE TOPIC
    // --------------------------------------------------

    @PostMapping("/delete-topic/{id}")
    public String deleteTopic(@PathVariable Long id) {

        topicRepository.deleteById(id);

        return "redirect:/topics";
    }


    // --------------------------------------------------
    // SHOW EDIT TOPIC PAGE
    // --------------------------------------------------

    @GetMapping("/edit-topic/{id}")
    public String editTopic(
            @PathVariable Long id,
            Model model) {

        Topic topic = topicRepository
                .findById(id)
                .orElseThrow();

        List<Subject> subjects = new ArrayList<>();

        subjects.addAll(
                subjectRepository.findByExamType(ExamType.PRELIMS)
        );

        subjects.addAll(
                subjectRepository.findByExamType(ExamType.MAINS)
        );

        model.addAttribute("topic", topic);
        model.addAttribute("subjects", subjects);
        model.addAttribute("sections", sectionRepository.findAll());

        return "edit-topic";
    }


    // --------------------------------------------------
    // UPDATE TOPIC
    // --------------------------------------------------

    @PostMapping("/update-topic")
    public String updateTopic(@ModelAttribute Topic topic) {

        if (topic.getSection() != null
                && topic.getSection().getId() != null) {

            Section section = sectionRepository
                    .findById(topic.getSection().getId())
                    .orElseThrow();

            topic.setSection(section);

            // Automatically assign subject from section
            topic.setSubject(section.getSubject());
        }

        topicRepository.save(topic);

        return "redirect:/topics";
    }
}
