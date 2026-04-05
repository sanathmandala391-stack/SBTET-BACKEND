package com.sbtetAttendance.sbtet.controller;


import com.sbtetAttendance.sbtet.model.User;
import com.sbtetAttendance.sbtet.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }


    @GetMapping("/admin/reset-temp")
    public String resetAdmin() {
        return authService.resetAdminPasswordTemporary();
    }


    @PostMapping("/register/student")
    public ResponseEntity<?> registerStudent(@RequestBody Map<String, Object> body) {
        try {
            return ResponseEntity.status(201).body(authService.registerStudent(body));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/register/faculty")
    public ResponseEntity<?> registerFaculty(@RequestBody Map<String, Object> body) {
        try {
            return ResponseEntity.status(201).body(authService.registerFaculty(body));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> body) {
        try {
            return ResponseEntity.ok(authService.login(body.get("email"), body.get("password")));
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.status(401).body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/me")
    public ResponseEntity<?> getMe(@AuthenticationPrincipal User user) {
        Map<String, Object> userMap = new HashMap<>();
        userMap.put("id", user.getId());
        userMap.put("name", user.getName());
        userMap.put("email", user.getEmail());
        userMap.put("role", user.getRole());
        userMap.put("isApproved", user.getApproved());
        userMap.put("pinNumber", user.getPinNumber() != null ? user.getPinNumber() : "");
        userMap.put("branch", user.getBranch() != null ? user.getBranch() : "");
        userMap.put("semester", user.getSemester() != null ? user.getSemester() : "");
        userMap.put("collegeCode", user.getCollegeCode() != null ? user.getCollegeCode() : "");
        userMap.put("faceImage", user.getFaceImage() != null ? user.getFaceImage() : "");
        userMap.put("department", user.getDepartment() != null ? user.getDepartment() : "");
        return ResponseEntity.ok(userMap);
    }

    @PutMapping("/face")
    public ResponseEntity<?> updateFace(@AuthenticationPrincipal User user,
                                        @RequestBody Map<String, String> body) {
        try {
            authService.updateFaceDescriptor(user, body.get("faceDescriptor"), body.get("faceImageBase64"));
            return ResponseEntity.ok(Map.of("message", "Face data updated successfully."));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("message", e.getMessage()));
        }
    }
}
