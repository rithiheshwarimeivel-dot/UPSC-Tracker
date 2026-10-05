package com.upsc.upsctracker.service;

import com.upsc.upsctracker.entity.CSATTopic;
import com.upsc.upsctracker.repository.CSATTopicRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CSATTopicService {

    private final CSATTopicRepository csatTopicRepository;

    public CSATTopicService(CSATTopicRepository csatTopicRepository) {
        this.csatTopicRepository = csatTopicRepository;
    }

    public List<CSATTopic> getAllTopics() {
        return csatTopicRepository.findAll();
    }

    public List<CSATTopic> getTopicsBySection(String section) {
        return csatTopicRepository.findBySection(section);
    }

    public void updateCompletionStatus(Integer id, boolean completed) {

        CSATTopic topic = csatTopicRepository
                .findById(id)
                .orElseThrow();

        topic.setCompleted(completed);

        csatTopicRepository.save(topic);
    }
}