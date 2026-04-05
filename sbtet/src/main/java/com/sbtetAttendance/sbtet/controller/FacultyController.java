package com.sbtetAttendance.sbtet.controller;

import com.sbtetAttendance.sbtet.model.User;
import com.sbtetAttendance.sbtet.service.FacultyService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/faculty")
public class FacultyController {

    private final FacultyService facultyService;

    public FacultyController(FacultyService facultyService) {
        this.facultyService = facultyService;
    }

    @GetMapping("/students")
    public ResponseEntity<?> getStudents(@AuthenticationPrincipal User faculty,
                                         @RequestParam(required = false) String branch,
                                         @RequestParam(required = false) String semester) {
        return ResponseEntity.ok(facultyService.getStudents(faculty, branch, semester));
    }

    @GetMapping("/students/{studentId}/attendance")
    public ResponseEntity<?> getStudentAttendance(@PathVariable Long studentId,
                                                  @RequestParam(required = false) Integer month,
                                                  @RequestParam(required = false) Integer year) {
        return ResponseEntity.ok(facultyService.getStudentAttendance(studentId, month, year));
    }

    @GetMapping("/today")
    public ResponseEntity<?> getTodayAttendance(@AuthenticationPrincipal User faculty,
                                                @RequestParam(required = false) String branch,
                                                @RequestParam(required = false) String semester) {
        return ResponseEntity.ok(facultyService.getTodayAttendance(faculty, branch, semester));
    }

    @GetMapping("/report")
    public ResponseEntity<?> generateReport(@AuthenticationPrincipal User faculty,
                                            @RequestParam int month,
                                            @RequestParam int year,
                                            @RequestParam(required = false) String branch,
                                            @RequestParam(required = false) String semester) {
        return ResponseEntity.ok(facultyService.generateReport(faculty, month, year, branch, semester));
    }
}
