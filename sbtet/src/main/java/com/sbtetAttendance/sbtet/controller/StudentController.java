package com.sbtetAttendance.sbtet.controller;


import com.sbtetAttendance.sbtet.model.User;
import com.sbtetAttendance.sbtet.service.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/student")
public class StudentController {

    private final AttendanceService attendanceService;



    public StudentController(AttendanceService attendanceService) {
        this.attendanceService = attendanceService;
    }

    @GetMapping("/attendance")
    public ResponseEntity<?> getMyAttendance(@AuthenticationPrincipal User student) {
        return ResponseEntity.ok(attendanceService.getStudentSummary(student.getId()));
    }
}