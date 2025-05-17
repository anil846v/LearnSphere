package com.example.learn.Entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonManagedReference;

@Entity
@Table(name = "enrollments")
public class Enrollment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "student_id")
    private User student;

    @ManyToOne(optional = false)
    @JoinColumn(name = "course_id")
    private Course course;

    @Column(nullable = false)
    private boolean active;

    private LocalDateTime enrolledAt;
    @OneToMany(mappedBy = "enrollment", cascade = CascadeType.REMOVE, orphanRemoval = true)
    @JsonManagedReference
    private List<Progress> progresses;

    @OneToOne(mappedBy = "enrollment", cascade = CascadeType.REMOVE, orphanRemoval = true)
    @JsonManagedReference
    private PaymentOrder paymentOrder;

 // Getters and Setters
    // Constructors

	public Enrollment() {
	}

	public Enrollment(Long id, User student, Course course, boolean active, LocalDateTime enrolledAt,PaymentOrder paymentOrder, List<Progress> progresses) {
		this.id = id;
		this.student = student;
		this.course = course;
		this.active = active;
		this.enrolledAt = enrolledAt;
	    this.paymentOrder = paymentOrder;
	    this.progresses = progresses;


	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public User getStudent() {
		return student;
	}

	public void setStudent(User student) {
		this.student = student;
	}

	public Course getCourse() {
		return course;
	}

	public void setCourse(Course course) {
		this.course = course;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public LocalDateTime getEnrolledAt() {
		return enrolledAt;
	}

	public void setEnrolledAt(LocalDateTime enrolledAt) {
		this.enrolledAt = enrolledAt;
	}
	public PaymentOrder getPaymentOrder() {
	    return paymentOrder;
	}

	public void setPaymentOrder(PaymentOrder paymentOrder) {
	    this.paymentOrder = paymentOrder;
	}
	public List<Progress> getProgresses() {
	    return progresses;
	}

	public void setProgresses(List<Progress> progresses) {
	    this.progresses = progresses;
	}

    
}
