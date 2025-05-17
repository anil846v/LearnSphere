package com.example.learn.Entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "lessons")
public class Lesson {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @Column(name = "lesson_id", nullable = false, unique = true)
    private String lessonId;

    @Column(name = "lesson_name", nullable = false)
    private String lessonName;

    @Column(name = "lesson_topics")
    private String lessonTopics;

    @Column(name = "lesson_video_link")
    private String lessonVideoLink;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    // Default constructor
    public Lesson() {
    }

    // Parameterized constructor
    public Lesson(Long id, Course course, String lessonId, String lessonName, String lessonTopics,
                  String lessonVideoLink, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.course = course;
        this.lessonId = lessonId;
        this.lessonName = lessonName;
        this.lessonTopics = lessonTopics;
        this.lessonVideoLink = lessonVideoLink;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Course getCourse() {
        return course;
    }

    public void setCourse(Course course) {
        this.course = course;
    }

    public String getLessonId() {
        return lessonId;
    }

    public void setLessonId(String lessonId) {
        this.lessonId = lessonId;
    }

    public String getLessonName() {
        return lessonName;
    }

    public void setLessonName(String lessonName) {
        this.lessonName = lessonName;
    }

    public String getLessonTopics() {
        return lessonTopics;
    }

    public void setLessonTopics(String lessonTopics) {
        this.lessonTopics = lessonTopics;
    }

    public String getLessonVideoLink() {
        return lessonVideoLink;
    }

    public void setLessonVideoLink(String lessonVideoLink) {
        this.lessonVideoLink = lessonVideoLink;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}