package com.example.learn.Service;

import com.example.learn.Entity.Enrollment;
import com.example.learn.Entity.PaymentOrder;
import com.example.learn.Entity.PaymentStatus;
import com.example.learn.Repository.EnrollmentRepository;
import com.example.learn.Repository.PaymentRepository;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import com.razorpay.Utils;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

@Service
public class PaymentService {

	private final PaymentRepository paymentRepo;
	private final EnrollmentRepository enrollmentRepo;
	private final RazorpayClient razorpayClient;
	private final String razorpaySecret;

	public PaymentService(PaymentRepository paymentRepo, EnrollmentRepository enrollmentRepo,
			@Value("${razorpay.key_id}") String key, @Value("${razorpay.key_secret}") String secret)
			throws RazorpayException {
		this.paymentRepo = paymentRepo;
		this.enrollmentRepo = enrollmentRepo;
		this.razorpayClient = new RazorpayClient(key, secret);
		this.razorpaySecret = secret;
	}

	/**
	 * Fetch enrollment by id.
	 */
	public Enrollment getEnrollment(Long enrollmentId) {
		return enrollmentRepo.findById(enrollmentId)
				.orElseThrow(() -> new IllegalArgumentException("Enrollment not found with id: " + enrollmentId));
	}

	/**
	 * Create a Razorpay order and a corresponding PaymentOrder record.
	 */
	public PaymentOrder createRazorpayOrder(Enrollment enrollment) throws RazorpayException {
		BigDecimal amountPaise = enrollment.getCourse().getPrice().multiply(BigDecimal.valueOf(100));

		JSONObject req = new JSONObject();
		req.put("amount", amountPaise.intValue()); // Amount in paise (smallest currency unit)
		req.put("currency", "INR");
		req.put("receipt", "rcpt_" + enrollment.getId());

		Order rzOrder = razorpayClient.orders.create(req); // lowercase 'orders' per Razorpay Java SDK

		PaymentOrder po = new PaymentOrder();
		po.setEnrollment(enrollment);
		po.setAmount(enrollment.getCourse().getPrice());
		po.setRazorpayOrderId(rzOrder.get("id"));
		po.setStatus(PaymentStatus.CREATED);

		return paymentRepo.save(po);
	}
	public void markPaymentSuccess(String razorpayOrderId, String razorpayPaymentId, String razorpaySignature) {
	    PaymentOrder order = paymentRepo.findByRazorpayOrderId(razorpayOrderId)
	        .orElseThrow(() -> new IllegalArgumentException("PaymentOrder not found"));

	    order.setRazorpayPaymentId(razorpayPaymentId);
	    order.setRazorpaySignature(razorpaySignature);
	    order.setStatus(PaymentStatus.SUCCESSFUL); // or any appropriate status
	    paymentRepo.save(order);

	    // Enroll user if not already enrolled
	    Enrollment enrollment = order.getEnrollment();
	    enrollment.setActive(true); // assuming this field enables access
	    enrollmentRepo.save(enrollment);
	}

	public boolean verifySignature(String orderId, String paymentId, String signature, String secret) {
	    try {
	        String payload = orderId + "|" + paymentId;

	        Mac sha256Hmac = Mac.getInstance("HmacSHA256");
	        SecretKeySpec secretKey = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
	        sha256Hmac.init(secretKey);

	        byte[] hash = sha256Hmac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
	        StringBuilder hexString = new StringBuilder(2 * hash.length);
	        for (byte b : hash) {
	            String hex = Integer.toHexString(0xff & b);
	            if (hex.length() == 1) hexString.append('0');
	            hexString.append(hex);
	        }

	        return hexString.toString().equals(signature);
	    } catch (Exception e) {
	        return false;
	    }
	}


	/**
	 * Verify the Razorpay signature and update status.
	 */
	public PaymentOrder simulateTestPayment(PaymentOrder order) {
	    // 1. Generate a fake payment ID (for test only)
	    String fakePaymentId = "pay_test_" + System.currentTimeMillis();

	    // 2. Generate the Razorpay signature as if it was real
	    String payload = order.getRazorpayOrderId() + "|" + fakePaymentId;
	    String signature = calculateRazorpaySignature(payload);

	    // 3. Set values
	    order.setRazorpayPaymentId(fakePaymentId);
	    order.setRazorpaySignature(signature);
	    order.setStatus(PaymentStatus.SUCCESSFUL);

	    return paymentRepo.save(order);
	}
	public PaymentOrder getPaymentOrderById(Long id) {
	    return paymentRepo.findById(id)
	            .orElseThrow(() -> new IllegalArgumentException("PaymentOrder not found: " + id));
	}


	private String calculateRazorpaySignature(String payload) {
	    try {
	        Mac sha256Hmac = Mac.getInstance("HmacSHA256");
	        SecretKeySpec keySpec = new SecretKeySpec(razorpaySecret.getBytes(), "HmacSHA256");
	        sha256Hmac.init(keySpec);
	        byte[] hash = sha256Hmac.doFinal(payload.getBytes());
	        return Base64.getEncoder().encodeToString(hash);
	    } catch (Exception e) {
	        throw new RuntimeException("Error generating signature", e);
	    }
	}


	private static final Logger log = LoggerFactory.getLogger(PaymentService.class);

	public PaymentOrder handlePaymentCallback(String razorpayOrderId, String razorpayPaymentId,
			String razorpaySignature) {

		PaymentOrder po = paymentRepo.findByRazorpayOrderId(razorpayOrderId)
				.orElseThrow(() -> new IllegalArgumentException("Order not found with id: " + razorpayOrderId));

		String payload = razorpayOrderId + "|" + razorpayPaymentId;
		boolean valid = false;
		try {
			valid = Utils.verifySignature(payload, razorpaySignature, razorpaySecret);
		} catch (RazorpayException e) {
			e.printStackTrace();
		}

		po.setRazorpayPaymentId(razorpayPaymentId);
		po.setRazorpaySignature(razorpaySignature);
		po.setStatus(valid ? PaymentStatus.SUCCESSFUL : PaymentStatus.FAILED);

		return paymentRepo.save(po);
	}
}
