package com.example.learn.Repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.learn.Entity.Course;
import com.example.learn.Entity.User;
import java.util.List;
import java.util.Optional;

public interface CourseRepository extends JpaRepository<Course, Long> {
    List<Course> findByTrainer(User trainer);
    Optional<Course> findByCourseId(String courseId);
}