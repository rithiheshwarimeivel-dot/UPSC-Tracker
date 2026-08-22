
        package com.upsc.upsctracker.repository;

import com.upsc.upsctracker.entity.Section;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SectionRepository extends JpaRepository<Section, Long> {

    // Find all sections belonging to a subject
    List<Section> findBySubjectId(Long subjectId);

    // Find sections belonging to a subject for a specific year
    List<Section> findBySubjectIdAndYear(Long subjectId, Integer year);

}

