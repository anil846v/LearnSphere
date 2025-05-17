package com.example.learn.Controller;

import com.example.learn.Entity.Course;
import com.example.learn.Entity.Lesson;
import com.example.learn.Entity.User;
import com.example.learn.Repository.UserRepository;
import com.example.learn.Service.CourseService;
import com.example.learn.Service.LessonService;
import com.example.learn.jwtUtil.JwtUtil;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("api/trainer")
@CrossOrigin(origins = "http://127.0.0.1:5501", allowCredentials = "true")
public class TrainerController {

    private final CourseService courseService;
    private final LessonService lessonService;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    public TrainerController(CourseService courseService, LessonService lessonService, UserRepository userRepository, JwtUtil jwtUtil) {
        this.courseService = courseService;
        this.lessonService = lessonService;
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
    }

    // Add a new course
    @PostMapping("/coursess")
    public ResponseEntity<Course> addCourse(@RequestBody Course course, HttpServletRequest request) {
        User trainer = getTrainerFromRequest(request);
        Course newCourse = courseService.addCourse(course, trainer);
        return ResponseEntity.ok(newCourse);
    }

    // View all courses
    @GetMapping("/courses")
    public ResponseEntity<List<Map<String, Object>>> viewCourses(HttpServletRequest request) {
        User trainer = getTrainerFromRequest(request);
        List<Course> courses = courseService.listCourses(trainer);

        List<Map<String, Object>> courseList = new ArrayList<>();
        for (Course c : courses) {
            Map<String, Object> courseMap = new HashMap<>();
            courseMap.put("courseId", c.getCourseId());
            courseMap.put("courseName", c.getCourseName());
            courseMap.put("price", c.getPrice());

            List<Lesson> lessons = lessonService.listLessons(c);
            String lessonTopics = lessons.stream()
                    .map(Lesson::getLessonTopics)
                    .collect(Collectors.joining(", "));
            courseMap.put("lessons", lessonTopics);

            courseList.add(courseMap);
        }

        return ResponseEntity.ok(courseList);
    }

    // Get a specific course
    @GetMapping("/courses/{courseId}")
    public ResponseEntity<Course> getCourse(@PathVariable String courseId, HttpServletRequest request) {
        User trainer = getTrainerFromRequest(request);
        Course course = courseService.getCourseByCourseId(courseId);
        verifyOwnership(course, trainer);
        return ResponseEntity.ok(course);
    }

    // Update a course
    @PutMapping("/courses/{courseId}")
    public ResponseEntity<Course> updateCourse(@PathVariable String courseId, @RequestBody Course updatedCourse, HttpServletRequest request) {
        User trainer = getTrainerFromRequest(request);
        Course course = courseService.getCourseByCourseId(courseId);
        verifyOwnership(course, trainer);
        Course updated = courseService.updateCourse(course.getId(), updatedCourse);
        return ResponseEntity.ok(updated);
    }

    // Publish a course
    @PostMapping("/courses/{courseId}/publish")
    public ResponseEntity<Map<String, String>> publishCourse(@PathVariable String courseId, HttpServletRequest request) {
        User trainer = getTrainerFromRequest(request);
        Course course = courseService.getCourseByCourseId(courseId);
        verifyOwnership(course, trainer);
        courseService.publishCourse(course.getId());
        return ResponseEntity.ok(Map.of("message", "Course published successfully"));
    }
    @GetMapping("/profile")
    public ResponseEntity<Map<String, String>> getTrainerProfile(HttpServletRequest request) {
        User trainer = getTrainerFromRequest(request);
        Map<String, String> profile = new HashMap<>();
        profile.put("name", trainer.getName()); // or trainer.getEmail()
        return ResponseEntity.ok(profile);
    }


    // Add a new lesson
    @PostMapping("/courses/{courseId}/lessons")
    public ResponseEntity<Lesson> addLesson(@PathVariable String courseId, @RequestBody Lesson lesson, HttpServletRequest request) {
        User trainer = getTrainerFromRequest(request);
        Course course = courseService.getCourseByCourseId(courseId);
        verifyOwnership(course, trainer);
        Lesson newLesson = lessonService.addLesson(lesson, course);
        return ResponseEntity.ok(newLesson);
    }

    // View lessons in a course
    @GetMapping("/courses/{courseId}/lessons")
    public ResponseEntity<List<Lesson>> viewLessons(@PathVariable String courseId, HttpServletRequest request) {
        User trainer = getTrainerFromRequest(request);
        Course course = courseService.getCourseByCourseId(courseId);
        verifyOwnership(course, trainer);
        return ResponseEntity.ok(lessonService.listLessons(course));
    }

    // Update a lesson
    @PutMapping("/lessons/{lessonId}")
    public ResponseEntity<Lesson> updateLesson(@PathVariable String lessonId, @RequestBody Lesson updatedLesson, HttpServletRequest request) {
        User trainer = getTrainerFromRequest(request);
        Lesson lesson = lessonService.getLessonByLessonId(lessonId);
        verifyOwnership(lesson.getCourse(), trainer);
        Lesson updated = lessonService.updateLesson(lesson.getId(), updatedLesson);
        return ResponseEntity.ok(updated);
    }
 // Delete a course
    @DeleteMapping("/courses/{courseId}")
    public ResponseEntity<Map<String, String>> deleteCourse(@PathVariable String courseId, HttpServletRequest request) {
        User trainer = getTrainerFromRequest(request);
        Course course = courseService.getCourseByCourseId(courseId);
        verifyOwnership(course, trainer);
        courseService.deleteCourse(course.getId());
        return ResponseEntity.ok(Map.of("message", "Course deleted successfully"));
    }


    // Helpers
    
    private User getTrainerFromRequest(HttpServletRequest request) {
        String token = getTokenFromCookie(request);
        if (token == null) {
            throw new RuntimeException("JWT cookie not found from the method of getTrainerFromRequest");
        }
        String email = jwtUtil.extractUsername(token);
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Trainer not found"));
    }
    
    
    private String getTokenFromCookie(HttpServletRequest request) {
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("jwt".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        throw new RuntimeException("JWT cookie not found from the method of getTokenFromCookie");
    }

    private void verifyOwnership(Course course, User trainer) {
        if (course == null || course.getTrainer() == null) {
            throw new RuntimeException("Course or trainer not found");
        }

        if (!Objects.equals(course.getTrainer().getId(), trainer.getId())) {
            throw new RuntimeException("Unauthorized: You do not own this course");
        }
    }

}
