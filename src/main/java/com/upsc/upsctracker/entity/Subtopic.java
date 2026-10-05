
        package com.upsc.upsctracker.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "subtopic")
public class Subtopic {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name")
    private String name;

    @ManyToOne
    @JoinColumn(name = "topic_id")
    private Topic topic;

    @Column(name = "completed")
    private boolean completed = false;

    public Subtopic() {
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Topic getTopic() {
        return topic;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setTopic(Topic topic) {
        this.topic = topic;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }
}

