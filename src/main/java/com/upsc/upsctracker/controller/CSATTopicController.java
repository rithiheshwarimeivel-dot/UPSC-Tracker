package com.upsc.upsctracker.controller;

import com.upsc.upsctracker.entity.CSATTopic;
import com.upsc.upsctracker.service.CSATTopicService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/csat")
@CrossOrigin
public class CSATTopicController {

    private final CSATTopicService csatTopicService;

    public CSATTopicController(CSATTopicService csatTopicService) {
        this.csatTopicService = csatTopicService;
    }

    // Show CSAT page
    @GetMapping
    public String showCSATPage() {
        return "csat";
    }

    // Get all CSAT topics
    @GetMapping("/topics")
    public List<CSATTopic> getAllTopics() {
        return csatTopicService.getAllTopics();
    }

    // Get topics by section
    @GetMapping("/topics/{section}")
    public List<CSATTopic> getTopicsBySection(
            @PathVariable String section) {

        return csatTopicService.getTopicsBySection(section);
    }
}