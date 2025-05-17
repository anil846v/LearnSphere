package com.example.learn.Repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.learn.Entity.Enrollment;
import com.example.learn.Entity.PaymentOrder;

//payment
public interface PaymentRepository extends JpaRepository<PaymentOrder, Long> {
 Optional<PaymentOrder> findByRazorpayOrderId(String razorpayOrderId);
 void deleteByEnrollment(Enrollment enrollment);

}