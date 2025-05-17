package com.example.learn.Repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.learn.Entity.Course;
import com.example.learn.Entity.Enrollment;
import com.example.learn.Entity.User;

import jakarta.transaction.Transactional;
//enrollment
public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {
 List<Enrollment> findByStudent(User student);
 Optional<Enrollment> findByStudentAndCourse(User student, Course course);
 @Modifying
 @Transactional
 @Query("DELETE FROM Enrollment e WHERE e.student.id = :userId AND e.course.courseId = :courseId")
 void deleteEnrollmentByUserAndCourse(@Param("userId") Long userId, @Param("courseId") String courseId);
}