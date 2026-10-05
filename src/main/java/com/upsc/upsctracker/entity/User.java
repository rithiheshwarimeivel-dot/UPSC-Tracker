package com.upsc.upsctracker.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "users")
@Getter
@Setter
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "email", unique = true, length = 150)
    private String email;

    @Column(name = "password", length = 255)
    private String password;

    @Column(name = "created_at")
    private java.sql.Timestamp createdAt;

    @Column(name = "attempt_year")
    private Integer attemptYear;

    @Column(name = "why_upsc", columnDefinition = "TEXT")
    private String whyUpsc;

    @Column(name = "dream_post", length = 100)
    private String dreamPost;

    @Column(name = "education", length = 150)
    private String education;

    @Column(name = "profession", length = 100)
    private String profession;

    @Column(name = "location", length = 150)
    private String location;

    @Column(name = "bio", columnDefinition = "TEXT")
    private String bio;

    @Column(name = "profile_picture", length = 255)
    private String profilePicture;
}