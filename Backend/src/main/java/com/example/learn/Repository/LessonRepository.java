package com.example.learn.Repository;

import com.example.learn.Entity.Course;
import com.example.learn.Entity.Lesson;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface LessonRepository extends JpaRepository<Lesson, Long> {
    List<Lesson> findByCourse(Course course);
    Optional<Lesson> findByLessonId(String lessonId);
    
}