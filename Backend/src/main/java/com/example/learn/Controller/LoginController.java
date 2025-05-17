package com.example.learn.Controller;

import com.example.learn.Entity.User;
import com.example.learn.Service.LoginService;
import com.example.learn.jwtUtil.JwtUtil;

//import com.example.learn.Util.JwtUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseCookie;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://127.0.0.1:5501", allowCredentials = "true")
public class LoginController {

    @Autowired
    private LoginService loginService;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/login")
    public Map<String, String> loginUser(@RequestBody User loginRequest, HttpServletResponse response) {
        Map<String, String> result = loginService.loginAndGenerateToken(loginRequest.getEmail(), loginRequest.getPassword());

        if ("success".equals(result.get("status"))) {
            String token = result.get("token");

            // Manually set Set-Cookie header with SameSite=None and Secure
            String cookieValue = "jwt=" + token +
                    "; Path=/; HttpOnly; Max-Age=3600; SameSite=None; Secure";

            response.setHeader("Set-Cookie", cookieValue);

            System.out.println("Setting cookie with token: " + token);

            result.remove("token");
        } else {
            System.out.println("Login failed: " + result);
        }

        return result;
    }




    @GetMapping("/validate")
    public Map<String, String> validateTokenFromCookie(HttpServletRequest request) {
        String token = getTokenFromCookie(request);
        if (token == null) {
            Map<String, String> error = new HashMap<>();
            error.put("status", "error");
            error.put("message", "JWT cookie not found");
            return error;
        }

        return loginService.validateTokenFromCookie(token);
    }

    @PostMapping("/logout")
    public Map<String, String> logout(HttpServletResponse response) {
        Cookie cookie = new Cookie("jwt", null);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(0); // Deletes cookie
        response.addCookie(cookie);

        Map<String, String> result = new HashMap<>();
        result.put("status", "success");
        result.put("message", "Logged out successfully");
        return result;
    }

    private String getTokenFromCookie(HttpServletRequest request) {
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                System.out.println("Cookie: " + cookie.getName() + "=" + cookie.getValue());
                if ("jwt".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

    }