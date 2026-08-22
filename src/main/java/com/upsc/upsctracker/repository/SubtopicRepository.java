package com.upsc.upsctracker.repository;

import com.upsc.upsctracker.entity.Subtopic;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SubtopicRepository extends JpaRepository<Subtopic, Long> {

    List<Subtopic> findByTopicId(Long topicId);
}