package com.example.learn.lessons;

import org.springframework.stereotype.Component;

import com.example.learn.Entity.Lesson;

@Component
public class LessonMapper {
	public LessonDTO toDTO(Lesson lesson) {
		LessonDTO dto = new LessonDTO();
		dto.setLessonId(lesson.getLessonId());
		dto.setLessonName(lesson.getLessonName());
		dto.setLessonTopics(lesson.getLessonTopics());
		dto.setLessonVideoLink(lesson.getLessonVideoLink());
		String link = lesson.getLessonVideoLink();
		if (link != null) {
			if (link.contains("watch?v=")) {
				String videoId = link.substring(link.indexOf("watch?v=") + 8);
				dto.setLessonVideoLink("https://www.youtube.com/embed/" + videoId);
			} else if (link.contains("shorts/")) {
				String videoId = link.substring(link.lastIndexOf("/") + 1);
				dto.setLessonVideoLink("https://www.youtube.com/embed/" + videoId);
			} else if (link.contains("youtube.com/embed/")) {
				dto.setLessonVideoLink(link); // already embed
			} else {
				dto.setLessonVideoLink(link); // fallback
			}
		}
		return dto;
	}
}
