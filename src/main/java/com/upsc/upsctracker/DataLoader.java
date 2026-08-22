package com.upsc.upsctracker;

import com.upsc.upsctracker.entity.Subject;
import com.upsc.upsctracker.entity.Topic;
import com.upsc.upsctracker.repository.SubjectRepository;
import com.upsc.upsctracker.repository.TopicRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

//@Component
public class DataLoader implements CommandLineRunner {

    private final TopicRepository topicRepository;
    private final SubjectRepository subjectRepository;

    public DataLoader(TopicRepository topicRepository,
                      SubjectRepository subjectRepository) {
        this.topicRepository = topicRepository;
        this.subjectRepository = subjectRepository;
    }

    @Override
    public void run(String... args) {
        topicRepository.deleteAll();
        subjectRepository.deleteAll();

        Subject polity = new Subject();
        polity.setName("Polity");
        subjectRepository.save(polity);


        Topic topic1 = new Topic();
        topic1.setSubject(polity);
        topic1.setTopicName("Fundamental Rights");
        topic1.setCompleted(false);

        topicRepository.save(topic1);
    }
}