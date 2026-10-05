package com.upsc.upsctracker.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "revision_history")
@Getter
@Setter
public class RevisionHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(
            name = "revision_id",
            nullable = false
    )
    private Revision revision;

    @Column(name = "revision_number", nullable = false)
    private Integer revisionNumber;

    @Column(name = "revised_at")
    private LocalDateTime revisedAt;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @PrePersist
    protected void onCreate() {

        if (revisedAt == null) {
            revisedAt = LocalDateTime.now();
        }
    }
}