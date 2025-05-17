package com.example.learn.lessons;

public class LessonDTO {
	private String lessonId;
	private String lessonName;
	private String lessonTopics;
	private String lessonVideoLink;
    // Getters and Setters

	public LessonDTO() {
	}

	public LessonDTO(String lessonId, String lessonName, String lessonTopics, String lessonVideoLink) {
		this.lessonId = lessonId;
		this.lessonName = lessonName;
		this.lessonTopics = lessonTopics;
		this.lessonVideoLink = lessonVideoLink;
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
}
