package com.sbtetAttendance.sbtet.controller;

import com.sbtetAttendance.sbtet.model.User;
import com.sbtetAttendance.sbtet.service.AdminService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/dashboard")
    public ResponseEntity<?> getDashboard() {
        return ResponseEntity.ok(adminService.getDashboardStats());
    }

    @PostMapping("/college")
    public ResponseEntity<?> registerCollege(@RequestBody Map<String, Object> body,
                                             @AuthenticationPrincipal User admin) {
        try {
            return ResponseEntity.status(201).body(Map.of(
                    "message", "College registered successfully.",
                    "college", adminService.registerCollege(body, admin)));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/colleges")
    public ResponseEntity<?> getAllColleges() {
        return ResponseEntity.ok(adminService.getAllColleges());
    }

    @PutMapping("/colleges/{collegeId}")
    public ResponseEntity<?> updateCollege(@PathVariable Long collegeId,
                                           @RequestBody Map<String, Object> body) {
        try {
            return ResponseEntity.ok(Map.of("message", "College updated.",
                    "college", adminService.updateCollege(collegeId, body)));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/colleges/{collegeId}/holidays")
    public ResponseEntity<?> addHoliday(@PathVariable Long collegeId,
                                        @RequestBody Map<String, String> body) {
        try {
            adminService.addHoliday(collegeId, body.get("date"), body.get("reason"));
            return ResponseEntity.ok(Map.of("message", "Holiday added successfully."));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/approvals")
    public ResponseEntity<?> getPendingApprovals() {
        return ResponseEntity.ok(adminService.getPendingApprovals());
    }

    @PutMapping("/users/{userId}/approval")
    public ResponseEntity<?> updateApproval(@PathVariable Long userId,
                                            @RequestBody Map<String, Boolean> body) {
        try {
            adminService.updateUserApproval(userId, body.get("isApproved"));
            return ResponseEntity.ok(Map.of("message", "User " + (body.get("isApproved") ? "approved" : "rejected") + " successfully."));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/users")
    public ResponseEntity<?> getAllUsers(@RequestParam(required = false) String role,
                                         @RequestParam(required = false) String collegeCode,
                                         @RequestParam(required = false) Boolean isApproved) {
        return ResponseEntity.ok(adminService.getAllUsers(role, collegeCode, isApproved));
    }
}