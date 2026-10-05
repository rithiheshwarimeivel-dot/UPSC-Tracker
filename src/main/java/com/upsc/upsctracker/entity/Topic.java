package com.upsc.upsctracker.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "topic")
public class Topic {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "topic_name")
    private String topicName;

    @Column(name = "completed")
    private boolean completed = false;

    @ManyToOne
    @JoinColumn(name = "subject_id")
    private Subject subject;

    @ManyToOne
    @JoinColumn(name = "section_id")
    private Section section;

    public Topic() {
    }

    // =========================
    // GETTERS
    // =========================

    public Long getId() {
        return id;
    }

    public String getTopicName() {
        return topicName;
    }

    public boolean isCompleted() {
        return completed;
    }

    public Subject getSubject() {
        return subject;
    }

    public Section getSection() {
        return section;
    }

    // =========================
    // SETTERS
    // =========================

    public void setId(Long id) {
        this.id = id;
    }

    public void setTopicName(String topicName) {
        this.topicName = topicName;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public void setSubject(Subject subject) {
        this.subject = subject;
    }

    public void setSection(Section section) {
        this.section = section;
    }
}