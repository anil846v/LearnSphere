package com.example.learn.Service;

import com.example.learn.Entity.Course;
import com.example.learn.Entity.Lesson;
import com.example.learn.Repository.LessonRepository;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class LessonService {

    private final LessonRepository lessonRepository;

    public LessonService(LessonRepository lessonRepository) {
        this.lessonRepository = lessonRepository;
    }

    public Lesson addLesson(Lesson lesson, Course course) {
        lesson.setCourse(course);
        LocalDateTime now = LocalDateTime.now();
        lesson.setCreatedAt(now);
        lesson.setUpdatedAt(now);
        return lessonRepository.save(lesson);
    }

    public List<Lesson> listLessons(Course course) {
        return lessonRepository.findByCourse(course);
    }

    public Lesson getLessonById(Long id) {
        return lessonRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Lesson not found"));
    }

    public Lesson getLessonByLessonId(String lessonId) {
        return lessonRepository.findByLessonId(lessonId)
                .orElseThrow(() -> new RuntimeException("Lesson not found"));
    }

    public Lesson updateLesson(Long id, Lesson updatedLesson) {
        Lesson lesson = getLessonById(id);
        lesson.setLessonId(updatedLesson.getLessonId());
        lesson.setLessonName(updatedLesson.getLessonName());
        lesson.setLessonTopics(updatedLesson.getLessonTopics());
        lesson.setLessonVideoLink(updatedLesson.getLessonVideoLink());
        lesson.setUpdatedAt(LocalDateTime.now());
        return lessonRepository.save(lesson);
    }
}