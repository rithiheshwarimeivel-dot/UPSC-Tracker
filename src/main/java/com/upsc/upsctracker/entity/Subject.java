package com.upsc.upsctracker.entity;

import jakarta.persistence.*;

@Entity
public class Subject {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Enumerated(EnumType.STRING)
    private ExamType examType;

    public Subject() {
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public ExamType getExamType() {
        return examType;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setExamType(ExamType examType) {
        this.examType = examType;
    }
}