package com.upsc.upsctracker.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "revisions")
@Getter
@Setter
public class Revision {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @OneToOne
    @JoinColumn(
            name = "topic_id",
            nullable = false,
            unique = true
    )
    private Topic topic;

    @Column(name = "next_revision_date")
    private LocalDate nextRevisionDate;

    @Column(name = "revision_count", nullable = false)
    private Integer revisionCount = 0;

    @Column(name = "current_status", nullable = false)
    private String currentStatus = "NOT_REVISED";

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {

        LocalDateTime now = LocalDateTime.now();

        createdAt = now;
        updatedAt = now;

        if (revisionCount == null) {
            revisionCount = 0;
        }

        if (currentStatus == null) {
            currentStatus = "NOT_REVISED";
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}