package com.sbtetAttendance.sbtet.controller;

import com.sbtetAttendance.sbtet.model.User;
import com.sbtetAttendance.sbtet.service.AttendanceService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/attendance")
public class AttendanceController {

    private final AttendanceService attendanceService;

    public AttendanceController(AttendanceService attendanceService) {
        this.attendanceService = attendanceService;
    }

    @PostMapping("/checkin")
    public ResponseEntity<?> checkIn(@AuthenticationPrincipal User user,
                                     @RequestBody Map<String, Object> body) {
        try {
            double lat = Double.parseDouble(body.get("latitude").toString());
            double lon = Double.parseDouble(body.get("longitude").toString());
            boolean faceVerified = Boolean.parseBoolean(body.get("faceVerified").toString());
            return ResponseEntity.ok(attendanceService.checkIn(user, lat, lon, faceVerified));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/checkout")
    public ResponseEntity<?> checkOut(@AuthenticationPrincipal User user,
                                      @RequestBody Map<String, Object> body) {
        try {
            double lat = Double.parseDouble(body.get("latitude").toString());
            double lon = Double.parseDouble(body.get("longitude").toString());
            boolean faceVerified = Boolean.parseBoolean(body.get("faceVerified").toString());
            return ResponseEntity.ok(attendanceService.checkOut(user, lat, lon, faceVerified));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/summary/{studentId}")
    public ResponseEntity<?> getStudentSummary(@PathVariable Long studentId,
                                               @AuthenticationPrincipal User user) {
        try {
            if (user.getRole().name().equals("STUDENT") && !user.getId().equals(studentId))
                return ResponseEntity.status(403).body(Map.of("message", "Access denied."));
            return ResponseEntity.ok(attendanceService.getStudentSummary(studentId));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }
    @GetMapping("/today")
    public ResponseEntity<?> getTodayStatus(@AuthenticationPrincipal User user) {

        Map<String, Object> response = new java.util.HashMap<>();

        Object record = attendanceService.getTodayStatus(user.getId()).orElse(null);

        response.put("record", record);
        response.put("today", java.time.LocalDate.now());

        return ResponseEntity.ok(response);
    }
}