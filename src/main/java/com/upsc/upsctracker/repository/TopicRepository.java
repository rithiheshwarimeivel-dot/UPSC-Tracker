
        package com.upsc.upsctracker.repository;

import com.upsc.upsctracker.entity.Topic;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TopicRepository extends JpaRepository<Topic, Long> {

    List<Topic> findBySectionId(Long sectionId);

}
