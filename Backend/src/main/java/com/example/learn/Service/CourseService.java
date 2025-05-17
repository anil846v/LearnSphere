package com.example.learn.Service;

import com.example.learn.Entity.Course;
import com.example.learn.Entity.User;
import com.example.learn.Repository.CourseRepository;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class CourseService {

    private final CourseRepository courseRepository;

    public CourseService(CourseRepository courseRepository) {
        this.courseRepository = courseRepository;
    }

    public Course addCourse(Course course, User trainer) {
        course.setTrainer(trainer);
        LocalDateTime now = LocalDateTime.now();
        course.setCreatedAt(now);
        course.setUpdatedAt(now);
        return courseRepository.save(course);
    }

    public List<Course> listCourses(User trainer) {
        return courseRepository.findByTrainer(trainer);
    }

    public Course getCourseById(Long id) {
        return courseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Course not found"));
    }

    public Course getCourseByCourseId(String courseId) {
        return courseRepository.findByCourseId(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found"));
    }
    public void deleteCourse(Long courseId) {
        courseRepository.deleteById(courseId);
    }


    public Course updateCourse(Long id, Course updatedCourse) {
        Course course = getCourseById(id);
        course.setCourseId(updatedCourse.getCourseId());
        course.setCourseName(updatedCourse.getCourseName());
        course.setPrice(updatedCourse.getPrice());
        course.setDescription(updatedCourse.getDescription());
        course.setDuration(updatedCourse.getDuration());
        course.setUpdatedAt(LocalDateTime.now());
        return courseRepository.save(course);
    }

    public void publishCourse(Long id) {
        Course course = getCourseById(id);
        course.setUpdatedAt(LocalDateTime.now());
        courseRepository.save(course);
    }
}