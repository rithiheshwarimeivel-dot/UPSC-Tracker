package com.upsc.upsctracker.repository;

import com.upsc.upsctracker.entity.Section;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SectionRepository extends JpaRepository<Section, Long> {

    List<Section> findBySubjectId(Long subjectId);

    List<Section> findBySubjectIdAndYear(Long subjectId, Integer year);
}