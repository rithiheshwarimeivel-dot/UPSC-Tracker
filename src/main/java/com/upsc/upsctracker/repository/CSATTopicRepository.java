package com.upsc.upsctracker.repository;

import com.upsc.upsctracker.entity.CSATTopic;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CSATTopicRepository extends JpaRepository<CSATTopic, Integer> {

    List<CSATTopic> findBySection(String section);
}