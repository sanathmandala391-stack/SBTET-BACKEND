package com.sbtetAttendance.sbtet.service;



import com.sbtetAttendance.sbtet.model.*;
import com.sbtetAttendance.sbtet.Repository.*;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;

@Service
public class FacultyService {

    private final UserRepository userRepo;
    private final AttendanceDayRepository attendanceDayRepo;
    private final AttendanceSummaryRepository summaryRepo;

    public FacultyService(UserRepository userRepo, AttendanceDayRepository attendanceDayRepo,
                          AttendanceSummaryRepository summaryRepo) {
        this.userRepo = userRepo;
        this.attendanceDayRepo = attendanceDayRepo;
        this.summaryRepo = summaryRepo;
    }

    public List<User> getStudents(User faculty, String branch, String semester) {
        return userRepo.findByCollegeIdAndRoleAndIsApprovedAndIsActive(
                        faculty.getCollege().getId(), Role.STUDENT, true, true).stream()
                .filter(s -> branch == null || branch.equalsIgnoreCase(s.getBranch()))
                .filter(s -> semester == null || semester.equalsIgnoreCase(s.getSemester()))
                .toList();
    }

    public Map<String, Object> getStudentAttendance(Long studentId, Integer month, Integer year) {
        Map<String, Object> result = new LinkedHashMap<>();
        var filter = attendanceDayRepo.findByStudentIdOrderByDateAsc(studentId).stream()
                .filter(r -> month == null || r.getMonth().equals(month))
                .filter(r -> year == null || r.getYear().equals(year))
                .toList();
        result.put("records", filter);
        if (month != null && year != null) {
            summaryRepo.findByStudentIdAndMonthAndYear(studentId, month, year)
                    .ifPresent(s -> result.put("summary", s));
        }
        return result;
    }

    public List<Map<String, Object>> getTodayAttendance(User faculty, String branch, String semester) {
        LocalDate today = LocalDate.now();
        List<User> students = userRepo.findByCollegeIdAndRoleAndIsApprovedAndIsActive(
                        faculty.getCollege().getId(), Role.STUDENT, true, true).stream()
                .filter(s -> branch == null || branch.equalsIgnoreCase(s.getBranch()))
                .filter(s -> semester == null || semester.equalsIgnoreCase(s.getSemester()))
                .toList();

        List<Long> ids = students.stream().map(User::getId).toList();
        List<AttendanceDay> records = attendanceDayRepo.findByStudentIdInAndDate(ids, today);
        Map<Long, String> statusMap = new HashMap<>();
        records.forEach(r -> statusMap.put(r.getStudent().getId(), r.getStatus().name()));

        return students.stream().map(s -> {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("id", s.getId()); map.put("name", s.getName());
            map.put("pinNumber", s.getPinNumber()); map.put("branch", s.getBranch());
            map.put("semester", s.getSemester());
            map.put("todayStatus", statusMap.getOrDefault(s.getId(), "-"));
            return map;
        }).toList();
    }

    public List<Map<String, Object>> generateReport(User faculty, int month, int year, String branch, String semester) {
        List<User> students = userRepo.findByCollegeIdAndRoleAndIsApprovedAndIsActive(
                        faculty.getCollege().getId(), Role.STUDENT, true, true).stream()
                .filter(s -> branch == null || branch.equalsIgnoreCase(s.getBranch()))
                .filter(s -> semester == null || semester.equalsIgnoreCase(s.getSemester()))
                .toList();
        List<Long> ids = students.stream().map(User::getId).toList();

        List<AttendanceDay> records = attendanceDayRepo.findByStudentIdInAndMonthAndYear(ids, month, year);
        Map<Long, Map<Integer, String>> byStudent = new HashMap<>();
        records.forEach(r -> byStudent
                .computeIfAbsent(r.getStudent().getId(), k -> new TreeMap<>())
                .put(r.getDayOfMonth(), r.getStatus().name()));

        List<AttendanceSummary> summaries = summaryRepo.findByCollegeIdAndMonthAndYear(
                faculty.getCollege().getId(), month, year);
        Map<Long, AttendanceSummary> summaryMap = new HashMap<>();
        summaries.forEach(s -> summaryMap.put(s.getStudent().getId(), s));

        return students.stream().map(s -> {
            Map<String, Object> report = new LinkedHashMap<>();
            report.put("student", Map.of("id", s.getId(), "name", s.getName(),
                    "pinNumber", Objects.toString(s.getPinNumber(), ""),
                    "branch", Objects.toString(s.getBranch(), ""),
                    "semester", Objects.toString(s.getSemester(), "")));
            report.put("days", byStudent.getOrDefault(s.getId(), new TreeMap<>()));
            report.put("summary", summaryMap.get(s.getId()));
            return report;
        }).toList();
    }
}