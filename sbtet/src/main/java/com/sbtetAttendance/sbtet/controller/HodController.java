package com.sbtetAttendance.sbtet.controller;

import com.sbtetAttendance.sbtet.model.AttendanceStatus;
import com.sbtetAttendance.sbtet.model.User;
import com.sbtetAttendance.sbtet.service.HodService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/hod")
public class HodController {

    private final HodService hodService;

    public HodController(HodService hodService) {
        this.hodService = hodService;
    }

    @GetMapping("/today")
    public ResponseEntity<?> getTodayAttendance(@AuthenticationPrincipal User hod,
                                                @RequestParam(required = false) String branch,
                                                @RequestParam(required = false) String semester) {
        return ResponseEntity.ok(hodService.getTodayAttendance(hod, branch, semester));
    }

    @GetMapping("/students")
    public ResponseEntity<?> getStudents(@AuthenticationPrincipal User hod,
                                         @RequestParam(required = false) String branch,
                                         @RequestParam(required = false) String semester,
                                         @RequestParam(required = false) Boolean isApproved) {
        return ResponseEntity.ok(hodService.getCollegeStudents(hod, branch, semester, isApproved));
    }

    @PutMapping("/users/{userId}/approval")
    public ResponseEntity<?> approveUser(@AuthenticationPrincipal User hod,
                                         @PathVariable Long userId,
                                         @RequestBody Map<String, Boolean> body) {
        try {
            hodService.approveUser(hod, userId, body.get("isApproved"));
            return ResponseEntity.ok(Map.of("message", "User " + (body.get("isApproved") ? "approved" : "rejected") + "."));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

//    @PostMapping("/attendance/override")
//    public ResponseEntity<?> overrideAttendance(@AuthenticationPrincipal User hod,
//                                                @RequestBody Map<String, Object> body) {
//        try {
//            Long studentId = Long.parseLong(body.get("studentId").toString());
//            String date = (String) body.get("date");
//            AttendanceStatus newStatus = AttendanceStatus.valueOf((String) body.get("newStatus"));
//            String reason = (String) body.get("reason");
//            return ResponseEntity.ok(Map.of(
//                    "message", "Attendance updated successfully.",
//                    "record", hodService.overrideAttendance(hod, studentId, date, newStatus, reason)));
//        } catch (Exception e) {
//            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
//        }
//    }

    @PostMapping("/attendance/override")
    public ResponseEntity<?> overrideAttendance(@AuthenticationPrincipal User hod,
                                                @RequestBody Map<String, Object> body) {
        try {
            Long studentId = Long.parseLong(body.get("studentId").toString());
            String date = (String) body.get("date");
            AttendanceStatus newStatus = AttendanceStatus.valueOf((String) body.get("newStatus"));
            String reason = (String) body.get("reason");
            hodService.overrideAttendance(hod, studentId, date, newStatus, reason);
            return ResponseEntity.ok(Map.of("message", "Attendance updated successfully."));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/approvals")
    public ResponseEntity<?> getPendingApprovals(@AuthenticationPrincipal User hod) {
        return ResponseEntity.ok(hodService.getPendingApprovals(hod));
    }

    @GetMapping("/report")
    public ResponseEntity<?> getMonthlyReport(@AuthenticationPrincipal User hod,
                                              @RequestParam int month,
                                              @RequestParam int year,
                                              @RequestParam(required = false) String branch) {
        return ResponseEntity.ok(hodService.getMonthlyReport(hod, month, year, branch));
    }
}