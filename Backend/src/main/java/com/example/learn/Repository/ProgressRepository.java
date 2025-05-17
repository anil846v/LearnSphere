package com.example.learn.Repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.learn.Entity.Enrollment;
import com.example.learn.Entity.Lesson;
import com.example.learn.Entity.Progress;
import com.example.learn.Entity.User;
// progress
public interface ProgressRepository extends JpaRepository<Progress, Long> {
    List<Progress> findByEnrollment(Enrollment enrollment);
    void deleteByEnrollment(Enrollment enrollment);
    Optional<Progress> findByEnrollmentAndLesson(Enrollment enrollment, Lesson lesson);
    List<Progress> findByEnrollmentId(Long enrollmentId);


}