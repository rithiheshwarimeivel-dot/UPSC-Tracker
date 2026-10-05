package com.upsc.upsctracker.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "csat_topics")
public class CSATTopic {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String section;

    @Column(name = "topic_name", nullable = false)
    private String topicName;

    @Column(name = "topic_order")
    private Integer topicOrder;

    @Column(name = "questions_count")
    private Integer questionsCount;

    private Integer difficulty;

    private boolean completed = false;

    public CSATTopic() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getSection() {
        return section;
    }

    public void setSection(String section) {
        this.section = section;
    }

    public String getTopicName() {
        return topicName;
    }

    public void setTopicName(String topicName) {
        this.topicName = topicName;
    }

    public Integer getTopicOrder() {
        return topicOrder;
    }

    public void setTopicOrder(Integer topicOrder) {
        this.topicOrder = topicOrder;
    }

    public Integer getQuestionsCount() {
        return questionsCount;
    }

    public void setQuestionsCount(Integer questionsCount) {
        this.questionsCount = questionsCount;
    }

    public Integer getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(Integer difficulty) {
        this.difficulty = difficulty;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }
}