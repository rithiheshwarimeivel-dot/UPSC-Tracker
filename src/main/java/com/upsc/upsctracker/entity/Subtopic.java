package com.upsc.upsctracker.entity;

import jakarta.persistence.*;

@Entity
public class Subtopic {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private boolean completed;

    @ManyToOne
    private Topic topic;

    public Subtopic() {
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public boolean isCompleted() {
        return completed;
    }

    public Topic getTopic() {
        return topic;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public void setTopic(Topic topic) {
        this.topic = topic;
    }
}