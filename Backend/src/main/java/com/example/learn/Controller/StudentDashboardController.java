package com.example.learn.Controller;

import com.example.learn.Entity.*;
import com.example.learn.Service.LessonService;
import com.example.learn.Service.StudentDashboardService;
import com.example.learn.lessons.LessonDTO;
import com.example.learn.lessons.LessonMapper;
import com.example.learn.lessons.ProgressDTO;
import com.razorpay.RazorpayException;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/student")
@CrossOrigin(origins = "http://127.0.0.1:5501", allowCredentials = "true")
public class StudentDashboardController {
	
	private final LessonService lessonService;
	private final LessonMapper lessonMapper; // assuming you have a mapper bean
	private final StudentDashboardService studentService;

	public StudentDashboardController(StudentDashboardService studentService,
	                                  LessonService lessonService,
	                                  LessonMapper lessonMapper) {
	    this.studentService = studentService;
	    this.lessonService = lessonService;
	    this.lessonMapper = lessonMapper;
	}


    // Course catalog
    @GetMapping("/courses")
    public List<Course> listCourses() {
        return studentService.listAllCourses();
    }

    @GetMapping("/courses/{courseId}/lessons")
    public List<Lesson> listLessons(@PathVariable String courseId) {
        return studentService.listLessons(courseId);
    }

    @PostMapping("/purchase/initiate")
    public ResponseEntity<PaymentOrder> initiatePurchase(HttpServletRequest request, @RequestParam String courseId) throws RazorpayException {
        String email = (String) request.getAttribute("userEmail");
        if (email == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(studentService.initiatePurchaseByEmail(email, courseId));
    }

    @GetMapping("/lessons/{lessonId}")
    public ResponseEntity<LessonDTO> getLessonById(@PathVariable String lessonId) {
        Lesson lesson = lessonService.getLessonByLessonId(lessonId);
        if (lesson == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
        LessonDTO dto = lessonMapper.toDTO(lesson);
        return ResponseEntity.ok(dto);
    }

    @PostMapping("/purchase/complete")
    public ResponseEntity<String> completePurchase(@RequestBody Map<String, String> payload) {
        String razorpayOrderId = payload.get("razorpayOrderId");
        String razorpayPaymentId = payload.get("razorpayPaymentId");
        String razorpaySignature = payload.get("razorpaySignature");

        studentService.handlePurchaseCallback(razorpayOrderId, razorpayPaymentId, razorpaySignature);
        return ResponseEntity.ok("Enrolled successfully");
    }

    @DeleteMapping("/courses/{courseId}")
    public ResponseEntity<String> deleteCourse(@PathVariable String courseId, HttpServletRequest request) {
        String email = (String) request.getAttribute("userEmail");
        if (email == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        try {
            boolean deleted = studentService.deleteCourseForUser(email, courseId);
            if (deleted) {
                return ResponseEntity.ok("Course deleted successfully");
            } else {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Not authorized to delete this course");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to delete course");
        }
    }

    // Enrollments & progress
    @GetMapping("/enrollments")
    public List<Enrollment> userEnrollments(HttpServletRequest request) {
        String email = (String) request.getAttribute("userEmail");
        if (email == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }
        return studentService.userEnrollmentsByEmail(email);
    }
    @PostMapping("/purchase/simulate/{orderId}")
    public ResponseEntity<PaymentOrder> simulatePurchase(@PathVariable Long orderId) {
        PaymentOrder order = studentService.getPaymentOrderById(orderId); // delegate
        PaymentOrder simulated = studentService.simulateTestPayment(order);
        return ResponseEntity.ok(simulated);
    }

    @GetMapping("/enrollments/{userId}")
    public List<Enrollment> userEnrollments(@PathVariable Long userId) {
        return studentService.userEnrollments(userId);
    }
    @GetMapping("/profile")
    public ResponseEntity<Map<String, String>> getStudentProfile(HttpServletRequest request) {
        String email = (String) request.getAttribute("userEmail");
        if (email == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        User student = studentService.getUserByEmail(email);
        if (student == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        Map<String, String> profile = new HashMap<>();
        profile.put("name", student.getName());
        profile.put("email", student.getEmail());
        return ResponseEntity.ok(profile);
    }
    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpServletRequest request) {
        // Invalidate session or clear auth cookies/tokens depending on your security setup

        // Example: if using HttpSession
        try {
            request.getSession().invalidate();
        } catch (Exception e) {
            // log if needed
        }
        
        // Or clear authentication token (if using JWT, cookie clearing must be done in frontend)

        return ResponseEntity.ok("Logged out successfully");
    }
    


    @GetMapping("/progress/{enrollmentId}")
    public List<ProgressDTO> getProgress(@PathVariable Long enrollmentId) {
        return studentService.enrollmentProgress(enrollmentId);
    }



    @PostMapping("/progress/complete")
    public Progress markLessonCompleted(
            @RequestParam Long enrollmentId,
            @RequestParam Long lessonId) {
        return studentService.markCompleted(enrollmentId, lessonId);
    }
}
