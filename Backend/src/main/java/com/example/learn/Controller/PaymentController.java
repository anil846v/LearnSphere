package com.example.learn.Controller;

import com.example.learn.Entity.PaymentOrder;
import com.example.learn.Service.PaymentService;
import com.razorpay.RazorpayException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/payment")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    /** 
     * Called by frontend or student-service to create Razorpay order.
     */
    @PostMapping("/order/{enrollmentId}")
    public ResponseEntity<PaymentOrder> createOrder(@PathVariable Long enrollmentId) 
            throws RazorpayException {
        // enrollment fetched inside service
        return ResponseEntity.ok(
            paymentService.createRazorpayOrder(
              paymentService.getEnrollment(enrollmentId)
            )
        );
    }
    @PostMapping("/simulate/{orderId}")
    public ResponseEntity<PaymentOrder> simulatePayment(@PathVariable Long orderId) {
        PaymentOrder order = paymentService.getPaymentOrderById(orderId);
        PaymentOrder simulated = paymentService.simulateTestPayment(order);
        return ResponseEntity.ok(simulated);
    }

    /**
     * Razorpay callback endpoint (or your frontend relay).
     */
    @PostMapping("/callback")
    public ResponseEntity<PaymentOrder> handleCallback(
            @RequestParam String razorpay_order_id,
            @RequestParam String razorpay_payment_id,
            @RequestParam String razorpay_signature) {

        PaymentOrder updated = paymentService.handlePaymentCallback(
            razorpay_order_id, razorpay_payment_id, razorpay_signature
        );
        return ResponseEntity.ok(updated);
    }
}
