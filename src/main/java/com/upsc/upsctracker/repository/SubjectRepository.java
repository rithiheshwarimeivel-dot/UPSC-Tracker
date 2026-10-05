package com.upsc.upsctracker.repository;

import com.upsc.upsctracker.entity.ExamType;
import com.upsc.upsctracker.entity.Subject;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SubjectRepository extends JpaRepository<Subject, Long> {

    List<Subject> findByExamType(ExamType examType);
}