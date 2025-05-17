package com.example.learn.Service;

import com.example.learn.Entity.*;
import com.example.learn.Repository.*;
import com.example.learn.lessons.ProgressDTO;
import com.razorpay.RazorpayException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

@Service
public class StudentDashboardService {

    private final UserRepository userRepo;
    private final CourseRepository courseRepo;
    private final LessonRepository lessonRepo;
    private final EnrollmentRepository enrollmentRepo;
    private final ProgressRepository progressRepo;
    private final PaymentService paymentService;
    

    public StudentDashboardService(UserRepository userRepo,
                                   CourseRepository courseRepo,
                                   LessonRepository lessonRepo,
                                   EnrollmentRepository enrollmentRepo,
                                   ProgressRepository progressRepo,
                                   PaymentService paymentService) {
        this.userRepo = userRepo;
        this.courseRepo = courseRepo;
        this.lessonRepo = lessonRepo;
        this.enrollmentRepo = enrollmentRepo;
        this.progressRepo = progressRepo;
        this.paymentService = paymentService;
    }

    // ------------------------ COURSE CATALOG ------------------------

    public List<Course> listAllCourses() {
        return courseRepo.findAll();
    }

    public Course getCourseOrThrow(String courseId) {
        return courseRepo.findByCourseId(courseId)
                .orElseThrow(() -> new IllegalArgumentException("Course not found: " + courseId));
    }

    public List<Lesson> listLessons(String courseId) {
        Course course = getCourseOrThrow(courseId);
        return lessonRepo.findByCourse(course);
    }

    // ---------------------- ENROLLMENT & PURCHASE ----------------------

    @Transactional
    public PaymentOrder initiatePurchase(Long userId, String courseId) throws RazorpayException {
    	User student = userRepo.findById(userId)
    	        .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));


        Course course = getCourseOrThrow(courseId);

        Enrollment enrollment = new Enrollment();
        enrollment.setStudent(student);
        enrollment.setCourse(course);
        enrollment.setActive(false);  // initially inactive
        enrollmentRepo.save(enrollment);

        return paymentService.createRazorpayOrder(enrollment);
    }

    @Transactional
    public void finalizeEnrollment(PaymentOrder paidOrder) {
        if (paidOrder.getStatus() == PaymentStatus.SUCCESSFUL) {
        	Enrollment enrollment = paidOrder.getEnrollment();
            enrollment.setActive(true);
            enrollmentRepo.save(enrollment);

            // Initialize progress for all lessons in the course
            List<Lesson> lessons = lessonRepo.findByCourse(enrollment.getCourse());
            for (Lesson lesson : lessons) {
                Progress progress = new Progress();
                progress.setEnrollment(enrollment);
                progress.setLesson(lesson);
                progress.setCompleted(false);
                progressRepo.save(progress);
            }
            
        } else {
            throw new IllegalStateException("Cannot finalize enrollment: Payment not successful");
        }
    }
    // --------------------- PROGRESS TRACKING ---------------------

    public List<Enrollment> userEnrollments(Long userId) {
        User student = userRepo.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
        return enrollmentRepo.findByStudent(student);
    }

    @Value("${razorpay.key_secret}") // inject from application.properties
    private String razorpaySecret;

    public void handlePurchaseCallback(String razorpayOrderId, String razorpayPaymentId, String razorpaySignature) {
        boolean valid = paymentService.verifySignature(razorpayOrderId, razorpayPaymentId, razorpaySignature, razorpaySecret);

        if (!valid) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid Razorpay signature. Payment verification failed.");
        }

        // If valid, update the PaymentOrder and enroll user
        paymentService.markPaymentSuccess(razorpayOrderId, razorpayPaymentId, razorpaySignature);
    }
    public List<Enrollment> userEnrollmentsByEmail(String email) {
        User user = userRepo.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("User not found"));
        return enrollmentRepo.findByStudent(user);
    }
    

    public User getUserByEmail(String email) {
        Optional<User> userOpt = userRepo.findByEmail(email);
        return userOpt.orElse(null);
    }
    public boolean deleteCourseForUser(String userEmail, String courseId) {
        // 1. Verify user owns/enrolled in the course (authorization)
        User user = getUserByEmail(userEmail);
        if (user == null) {
            return false;
        }
        
        // Check if user is enrolled in the course or owns the course
        List<Enrollment> enrollments = userEnrollmentsByEmail(userEmail);
        boolean isEnrolled = enrollments.stream()
                              .anyMatch(enrollment -> enrollment.getCourse().getCourseId().equals(courseId));
        
        if (!isEnrolled) {
            return false;  // user cannot delete courses they are not enrolled in
        }
        
        // 2. Perform the deletion
        // Implement actual deletion logic, e.g.:
        // - Remove enrollment record
        // - Optionally remove course if you want
        
        // Example: remove enrollment for this user-course pair
        enrollmentRepo.deleteEnrollmentByUserAndCourse(user.getId(), courseId);
        
        return true;
    }
    public List<ProgressDTO> enrollmentProgress(Long enrollmentId) {
        List<Progress> progressList = progressRepo.findByEnrollmentId(enrollmentId);
        return progressList.stream().map(p -> {
            ProgressDTO dto = new ProgressDTO();
            dto.setEnrollmentId(p.getEnrollment().getId());
            dto.setLessonName(p.getLesson().getLessonName());
            dto.setLessonId(p.getLesson().getId());
            dto.setCompleted(p.isCompleted());
            return dto;
        }).collect(Collectors.toList());
    }

    public boolean verifySignature(String orderId, String paymentId, String signature, String secret) {
        try {
            String payload = orderId + "|" + paymentId;
            String generatedSignature = hmacSha256Hex(payload, secret);
            return generatedSignature.equals(signature);
        } catch (Exception e) {
            return false;
        }
    }
    private String hmacSha256Hex(String payload, String secret) {
        try {
            Mac sha256Hmac = Mac.getInstance("HmacSHA256");
            SecretKeySpec keySpec = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            sha256Hmac.init(keySpec);
            byte[] hash = sha256Hmac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder(2 * hash.length);
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            throw new RuntimeException("Error generating HMAC SHA256 signature", e);
        }
    }


    public PaymentOrder getPaymentOrderById(Long orderId) {
        return paymentService.getPaymentOrderById(orderId);
    }

    public PaymentOrder simulateTestPayment(PaymentOrder order) {
        return paymentService.simulateTestPayment(order);
    }

    @Transactional
    public Progress markCompleted(Long enrollmentId, Long lessonId) {
        Enrollment enrollment = enrollmentRepo.findById(enrollmentId)
                .orElseThrow(() -> new IllegalArgumentException("Enrollment not found: " + enrollmentId));

        Lesson lesson = lessonRepo.findById(lessonId)
                .orElseThrow(() -> new IllegalArgumentException("Lesson not found: " + lessonId));

        Optional<Progress> existingProgress = progressRepo.findByEnrollmentAndLesson(enrollment, lesson);

        Progress progress = existingProgress.orElseGet(() -> {
            Progress p = new Progress();
            p.setEnrollment(enrollment);
            p.setLesson(lesson);
            return p;
        });
        System.out.println("Marking lesson " + lessonId + " completed for enrollment " + enrollmentId);

        progress.setCompleted(true);

        return progressRepo.save(progress);
    }

  

   

    @Transactional
    public PaymentOrder initiatePurchaseByEmail(String email, String courseId) throws RazorpayException {
        User student = userRepo.findByEmail(email)
            .orElseThrow(() -> new IllegalArgumentException("User not found: " + email));

        Course course = getCourseOrThrow(courseId);

        Enrollment enrollment = new Enrollment();
        enrollment.setStudent(student);
        enrollment.setCourse(course);
        enrollment.setActive(false);  // initially inactive
        enrollmentRepo.save(enrollment);

        return paymentService.createRazorpayOrder(enrollment);
    }
}
