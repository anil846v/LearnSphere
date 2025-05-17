package com.example.learn.lessons;
public class ProgressDTO {
    private Long enrollmentId;
    private String lessonName;
    private Long lessonId;
    private boolean completed;
	public ProgressDTO() {
	}
	public ProgressDTO(Long enrollmentId, String lessonName, Long lessonId, boolean completed) {
		this.enrollmentId = enrollmentId;
		this.lessonName = lessonName;
		this.lessonId = lessonId;
		this.completed = completed;
	}
	public Long getEnrollmentId() {
		return enrollmentId;
	}
	public void setEnrollmentId(Long enrollmentId) {
		this.enrollmentId = enrollmentId;
	}
	public String getLessonName() {
		return lessonName;
	}
	public void setLessonName(String lessonName) {
		this.lessonName = lessonName;
	}
	public Long getLessonId() {
		return lessonId;
	}
	public void setLessonId(Long lessonId) {
		this.lessonId = lessonId;
	}
	public boolean isCompleted() {
		return completed;
	}
	public void setCompleted(boolean completed) {
		this.completed = completed;
	}
    
}
