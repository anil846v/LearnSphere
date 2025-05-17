package com.example.learn.Entity;

import com.fasterxml.jackson.annotation.JsonBackReference;

import jakarta.persistence.*;

@Entity
@Table(name = "progress")
public class Progress {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(optional = false)
	@JoinColumn(name = "enrollment_id")
	@JsonBackReference
	private Enrollment enrollment;

	@ManyToOne(optional = false)
	@JoinColumn(name = "lesson_id")
	private Lesson lesson;

	@Column(nullable = false)
	private boolean completed;

	// Getters and Setters
	// Constructors
	public Progress() {
	}

	public Progress(Long id, Enrollment enrollment, Lesson lesson, boolean completed) {
		this.id = id;
		this.enrollment = enrollment;
		this.lesson = lesson;
		this.completed = completed;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Enrollment getEnrollment() {
		return enrollment;
	}

	public void setEnrollment(Enrollment enrollment) {
		this.enrollment = enrollment;
	}

	public Lesson getLesson() {
		return lesson;
	}

	public void setLesson(Lesson lesson) {
		this.lesson = lesson;
	}

	public boolean isCompleted() {
		return completed;
	}

	public void setCompleted(boolean completed) {
		this.completed = completed;
	}

}
