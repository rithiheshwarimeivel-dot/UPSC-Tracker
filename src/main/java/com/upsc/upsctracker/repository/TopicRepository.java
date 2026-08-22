package com.upsc.upsctracker.repository;

import com.upsc.upsctracker.entity.Topic;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TopicRepository extends JpaRepository<Topic, Long> {
}