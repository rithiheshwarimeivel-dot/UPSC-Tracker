package com.upsc.upsctracker.controller;

import com.upsc.upsctracker.entity.Subtopic;
import com.upsc.upsctracker.entity.Topic;
import com.upsc.upsctracker.repository.SubtopicRepository;
import com.upsc.upsctracker.repository.TopicRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
public class SubtopicController {

    private final SubtopicRepository subtopicRepository;
    private final TopicRepository topicRepository;

    public SubtopicController(SubtopicRepository subtopicRepository,
                              TopicRepository topicRepository) {

        this.subtopicRepository = subtopicRepository;
        this.topicRepository = topicRepository;
    }

    @GetMapping("/subtopics")
    public String subtopics(Model model) {

        model.addAttribute("subtopics",
                subtopicRepository.findAll());

        return "subtopics";
    }

    @GetMapping("/add-subtopic")
    public String addSubtopicPage(Model model) {

        model.addAttribute("subtopic", new Subtopic());
        model.addAttribute("topics", topicRepository.findAll());

        return "add-subtopic";
    }

    @GetMapping("/subtopics/by-topic/{topicId}")
    @ResponseBody
    public List<Subtopic> getSubtopicsByTopic(
            @PathVariable Long topicId) {

        return subtopicRepository.findByTopicId(topicId);
    }

    @PostMapping("/save-subtopic")
    public String saveSubtopic(@ModelAttribute Subtopic subtopic) {

        if (subtopic.getTopic() != null
                && subtopic.getTopic().getId() != null) {

            Topic topic = topicRepository
                    .findById(subtopic.getTopic().getId())
                    .orElseThrow();

            subtopic.setTopic(topic);
        }

        subtopicRepository.save(subtopic);

        return "redirect:/subtopics";
    }

    @GetMapping("/edit-subtopic/{id}")
    public String editSubtopic(@PathVariable Long id, Model model) {

        Subtopic subtopic = subtopicRepository
                .findById(id)
                .orElseThrow();

        model.addAttribute("subtopic", subtopic);
        model.addAttribute("topics", topicRepository.findAll());

        return "edit-subtopic";
    }

    @PostMapping("/update-subtopic")
    public String updateSubtopic(@ModelAttribute Subtopic subtopic) {

        if (subtopic.getTopic() != null
                && subtopic.getTopic().getId() != null) {

            Topic topic = topicRepository
                    .findById(subtopic.getTopic().getId())
                    .orElseThrow();

            subtopic.setTopic(topic);
        }

        subtopicRepository.save(subtopic);

        return "redirect:/subtopics";
    }

    @PostMapping("/delete-subtopic/{id}")
    public String deleteSubtopic(@PathVariable Long id) {

        subtopicRepository.deleteById(id);

        return "redirect:/subtopics";
    }
}