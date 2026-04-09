//package com.sbtetAttendance.sbtet.service;
//
//
//import com.sbtetAttendance.sbtet.model.*;
//import com.sbtetAttendance.sbtet.Repository.*;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.time.LocalDate;
//import java.time.LocalDateTime;
//import java.util.*;
//
//@Service
//public class HodService {
//
//    private final UserRepository userRepo;
//    private final AttendanceDayRepository attendanceDayRepo;
//    private final AttendanceSummaryRepository summaryRepo;
//    private final AttendanceService attendanceService;
//
//    public HodService(UserRepository userRepo, AttendanceDayRepository attendanceDayRepo,
//                      AttendanceSummaryRepository summaryRepo, AttendanceService attendanceService) {
//        this.userRepo = userRepo;
//        this.attendanceDayRepo = attendanceDayRepo;
//        this.summaryRepo = summaryRepo;
//        this.attendanceService = attendanceService;
//    }
//
//    public Map<String, Object> getTodayAttendance(User hod, String branch, String semester) {
//        Long collegeId = hod.getCollege().getId();
//        LocalDate today = LocalDate.now();
//
//        List<User> students = userRepo.findByCollegeIdAndRoleAndIsApprovedAndIsActive(
//                        collegeId, Role.STUDENT, true, true).stream()
//                .filter(s -> branch == null || branch.equalsIgnoreCase(s.getBranch()))
//                .filter(s -> semester == null || semester.equalsIgnoreCase(s.getSemester()))
//                .toList();
//
//        List<Long> ids = students.stream().map(User::getId).toList();
//        List<AttendanceDay> records = attendanceDayRepo.findByStudentIdInAndDate(ids, today);
//        Map<Long, AttendanceStatus> statusMap = new HashMap<>();
//        records.forEach(r -> statusMap.put(r.getStudent().getId(), r.getStatus()));
//
//        List<Object> present = new ArrayList<>(), absent = new ArrayList<>(),
//                error = new ArrayList<>(), halfDay = new ArrayList<>();
//
//        for (User s : students) {
//            AttendanceStatus st = statusMap.getOrDefault(s.getId(), AttendanceStatus.NONE);
//            Map<String, Object> data = toStudentMap(s);
//            data.put("todayStatus", st.name());
//            if (st == AttendanceStatus.P) present.add(data);
//            else if (st == AttendanceStatus.HD) halfDay.add(data);
//            else if (st == AttendanceStatus.E) error.add(data);
//            else absent.add(data);
//        }
//
//        return Map.of(
//                "date", today,
//                "summary", Map.of("total", students.size(), "present", present.size(),
//                        "absent", absent.size(), "halfDay", halfDay.size(), "error", error.size()),
//                "present", present, "absent", absent, "error", error, "halfDay", halfDay
//        );
//    }
//
//    public List<Map<String, Object>> getCollegeStudents(User hod, String branch, String semester, Boolean isApproved) {
//        return userRepo.findByCollegeIdAndRole(hod.getCollege().getId(), Role.STUDENT).stream()
//                .filter(s -> branch == null || branch.equalsIgnoreCase(s.getBranch()))
//                .filter(s -> semester == null || semester.equalsIgnoreCase(s.getSemester()))
//                .filter(s -> isApproved == null || isApproved.equals(s.getApproved()))
//                .map(s -> {
//                    Map<String, Object> map = toStudentMap(s);
//                    LocalDate now = LocalDate.now();
//                    summaryRepo.findByStudentIdAndMonthAndYear(s.getId(), now.getMonthValue(), now.getYear())
//                            .ifPresent(sum -> map.put("summary", sum));
//                    return map;
//                }).toList();
//    }
//
//    @Transactional
//    public void approveUser(User hod, Long userId, boolean isApproved) {
//        User user = userRepo.findById(userId).orElseThrow(() -> new NoSuchElementException("User not found."));
//        if (!user.getCollege().getId().equals(hod.getCollege().getId()))
//            throw new SecurityException("Can only manage users in your college.");
//        user.setApproved(isApproved);
//        userRepo.save(user);
//    }
//
//    @Transactional
//    public AttendanceDay overrideAttendance(User hod, Long studentId, String date,
//                                            AttendanceStatus newStatus, String reason) {
//        LocalDate d = LocalDate.parse(date);
//        AttendanceDay record = attendanceDayRepo.findByStudentIdAndDate(studentId, d)
//                .orElseThrow(() -> new NoSuchElementException("Attendance record not found."));
//        record.setOriginalStatus(record.getStatus());
//        record.setOverriddenBy(hod);
//        record.setOverrideReason(reason);
//        record.setOverriddenAt(LocalDateTime.now());
//        record.setStatus(newStatus);
//        attendanceDayRepo.save(record);
//        attendanceService.updateMonthlySummary(studentId, record.getCollege().getId(), record.getMonth(), record.getYear());
//        return record;
//    }
//
//    public List<User> getPendingApprovals(User hod) {
//        return userRepo.findByCollegeIdAndIsApprovedFalseAndRoleIn(
//                hod.getCollege().getId(), List.of(Role.STUDENT, Role.FACULTY));
//    }
//
//    public List<AttendanceSummary> getMonthlyReport(User hod, int month, int year, String branch) {
//        return summaryRepo.findByCollegeIdAndMonthAndYear(hod.getCollege().getId(), month, year).stream()
//                .filter(s -> branch == null || branch.equalsIgnoreCase(s.getStudent().getBranch()))
//                .toList();
//    }
//
//    private Map<String, Object> toStudentMap(User s) {
//        Map<String, Object> map = new LinkedHashMap<>();
//        map.put("id", s.getId()); map.put("name", s.getName());
//        map.put("email", s.getEmail()); map.put("pinNumber", s.getPinNumber());
//        map.put("branch", s.getBranch()); map.put("semester", s.getSemester());
//        map.put("isApproved", s.getApproved()); map.put("faceImage", s.getFaceImage());
//        return map;
//    }
//}





//new Second//

package com.sbtetAttendance.sbtet.service;


import com.sbtetAttendance.sbtet.model.*;
import com.sbtetAttendance.sbtet.Repository.*;
import com.sbtetAttendance.sbtet.Service.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class HodService {

    private final UserRepository userRepo;
    private final AttendanceDayRepository attendanceDayRepo;
    private final AttendanceSummaryRepository summaryRepo;
    private final AttendanceService attendanceService;
    @Autowired
    private AttendanceService attendanceService;

    public HodService(UserRepository userRepo,
                      AttendanceDayRepository attendanceDayRepo,
                      AttendanceSummaryRepository summaryRepo,
                      AttendanceService attendanceService) {
        this.userRepo          = userRepo;
        this.attendanceDayRepo = attendanceDayRepo;
        this.summaryRepo       = summaryRepo;
        this.attendanceService = attendanceService;
    }

    // ================================================================== //
    //  FIX 1: HOD department → student branch mapping
    //
    //  A HOD's "department" field (e.g. "Computer Science", "CS", "CSE")
    //  must map to the student's "branch" field (e.g. "CS", "ME", "CE").
    //  We normalise both sides to uppercase branch code before comparing.
    //
    //  Examples:
    //    "Computer Science"            → "CS"
    //    "CSE"                         → "CS"
    //    "Mechanical Engineering"      → "ME"
    //    "Electronics and Communication" → "EC"
    // ================================================================== //
    private static final Map<String, String> DEPT_TO_BRANCH = new HashMap<>();
    static {
        // Computer Science variants
        DEPT_TO_BRANCH.put("computer science",              "CS");
        DEPT_TO_BRANCH.put("computer science engineering",  "CS");
        DEPT_TO_BRANCH.put("cse",                           "CS");
        DEPT_TO_BRANCH.put("cs",                            "CS");
        // Mechanical
        DEPT_TO_BRANCH.put("mechanical",                    "ME");
        DEPT_TO_BRANCH.put("mechanical engineering",        "ME");
        DEPT_TO_BRANCH.put("me",                            "ME");
        // Civil
        DEPT_TO_BRANCH.put("civil",                         "CE");
        DEPT_TO_BRANCH.put("civil engineering",             "CE");
        DEPT_TO_BRANCH.put("ce",                            "CE");
        // Electrical
        DEPT_TO_BRANCH.put("electrical",                    "EE");
        DEPT_TO_BRANCH.put("electrical engineering",        "EE");
        DEPT_TO_BRANCH.put("ee",                            "EE");
        // Electronics
        DEPT_TO_BRANCH.put("electronics",                   "EC");
        DEPT_TO_BRANCH.put("electronics and communication", "EC");
        DEPT_TO_BRANCH.put("ece",                           "EC");
        DEPT_TO_BRANCH.put("ec",                            "EC");
        // Information Technology
        DEPT_TO_BRANCH.put("information technology",        "IT");
        DEPT_TO_BRANCH.put("it",                            "IT");
        // Chemical
        DEPT_TO_BRANCH.put("chemical",                      "CH");
        DEPT_TO_BRANCH.put("chemical technology",           "CH");
        DEPT_TO_BRANCH.put("ch",                            "CH");
        // Textile
        DEPT_TO_BRANCH.put("textile",                       "TT");
        DEPT_TO_BRANCH.put("textile technology",            "TT");
        DEPT_TO_BRANCH.put("tt",                            "TT");
        // Others
        DEPT_TO_BRANCH.put("metallurgy",                    "MET");
        DEPT_TO_BRANCH.put("met",                           "MET");
        DEPT_TO_BRANCH.put("packaging",                     "PKG");
        DEPT_TO_BRANCH.put("pkg",                           "PKG");
        DEPT_TO_BRANCH.put("hotel management",              "HMCT");
        DEPT_TO_BRANCH.put("hmct",                          "HMCT");
        DEPT_TO_BRANCH.put("pharmacy",                      "Pharmacy");
    }

    /**
     * Convert HOD's department string to the branch code used by students.
     * e.g. "Computer Science" → "CS",  "ME" → "ME"
     * Returns null if department is not set (no restriction applied).
     */
    private String hodDepartmentToBranch(String department) {
        if (department == null || department.isBlank()) return null;
        String key = department.trim().toLowerCase();
        // Check map first; fallback to uppercase as-is (e.g. "CS" → "CS")
        return DEPT_TO_BRANCH.getOrDefault(key, department.trim().toUpperCase());
    }

    // ------------------------------------------------------------------ //
    //  Today's attendance — ONLY HOD's own department/branch students
    // ------------------------------------------------------------------ //
    public Map<String, Object> getTodayAttendance(User hod, String branch, String semester) {
        Long      collegeId = hod.getCollege().getId();
        String    hodBranch = hodDepartmentToBranch(hod.getDepartment());
        LocalDate today     = LocalDate.now();

        List<User> students = userRepo
                .findByCollegeIdAndRoleAndIsApprovedAndIsActive(collegeId, Role.STUDENT, true, true)
                .stream()
                // ENFORCE HOD's department branch — ignore query branch param if it differs
                .filter(s -> hodBranch == null || hodBranch.equalsIgnoreCase(s.getBranch()))
                .filter(s -> semester == null   || semester.equalsIgnoreCase(s.getSemester()))
                .toList();

        List<Long> ids = students.stream().map(User::getId).toList();
        List<AttendanceDay> records = attendanceDayRepo.findByStudentIdInAndDate(ids, today);
        Map<Long, AttendanceStatus> statusMap = new HashMap<>();
        records.forEach(r -> statusMap.put(r.getStudent().getId(), r.getStatus()));

        List<Object> present = new ArrayList<>(), absent  = new ArrayList<>(),
                error   = new ArrayList<>(), halfDay = new ArrayList<>();

        for (User s : students) {
            AttendanceStatus st = statusMap.getOrDefault(s.getId(), AttendanceStatus.NONE);
            Map<String, Object> data = toStudentMap(s);
            data.put("todayStatus", st.name());
            switch (st) {
                case P  -> present.add(data);
                case HD -> halfDay.add(data);
                case E  -> error.add(data);
                default -> absent.add(data);  // A, NONE → absent
            }
        }

        return Map.of(
                "date",          today,
                "hodDepartment", Objects.toString(hod.getDepartment(), ""),
                "hodBranch",     Objects.toString(hodBranch, "ALL"),
                "summary", Map.of(
                        "total",   students.size(),
                        "present", present.size(),
                        "absent",  absent.size(),
                        "halfDay", halfDay.size(),
                        "error",   error.size()
                ),
                "present", present,
                "absent",  absent,
                "error",   error,
                "halfDay", halfDay
        );
    }

    // ------------------------------------------------------------------ //
    //  All students — ONLY HOD's own department/branch
    // ------------------------------------------------------------------ //
    public List<Map<String, Object>> getCollegeStudents(User hod, String branch,
                                                        String semester, Boolean isApproved) {
        String    hodBranch = hodDepartmentToBranch(hod.getDepartment());
        LocalDate now       = LocalDate.now();

        return userRepo.findByCollegeIdAndRole(hod.getCollege().getId(), Role.STUDENT)
                .stream()
                // ENFORCE HOD's department branch
                .filter(s -> hodBranch == null  || hodBranch.equalsIgnoreCase(s.getBranch()))
                .filter(s -> semester == null   || semester.equalsIgnoreCase(s.getSemester()))
                .filter(s -> isApproved == null || isApproved.equals(s.getApproved()))
                .map(s -> {
                    Map<String, Object> map = toStudentMap(s);
                    summaryRepo.findByStudentIdAndMonthAndYear(
                                    s.getId(), now.getMonthValue(), now.getYear())
                            .ifPresent(sum -> map.put("summary", sum));
                    return map;
                }).toList();
    }

    // ------------------------------------------------------------------ //
    //  Approve user — enforce department check for students
    // ------------------------------------------------------------------ //
    //@Transactional
    //public void approveUser(User hod, Long userId, boolean isApproved) {
        //User user = userRepo.findById(userId)
        //   .orElseThrow(() -> new NoSuchElementException("User not found."));

        // Must be in same college
     //   if (!user.getCollege().getId().equals(hod.getCollege().getId()))
       //     throw new SecurityException("Can only manage users in your college.");

        // For students: enforce same branch as HOD's department
       // if (user.getRole() == Role.STUDENT) {
        //    String hodBranch = hodDepartmentToBranch(hod.getDepartment());
         //   if (hodBranch != null && !hodBranch.equalsIgnoreCase(user.getBranch())) {
              //  throw new SecurityException(
               //   "You can only approve students from your department (" + hodBranch + ").");
        //    }
    //    }

    //    user.setApproved(isApproved);
    //    userRepo.save(user);
//    }


    //new Second method//
    public void approveUser(User hod, Long userId, Boolean isApproved) {
    User user = userRepo.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found."));
    user.setApproved(isApproved);
    userRepo.save(user);

    // NEW: If student just got approved, backfill their past attendance
    if (isApproved && user.getRole() == Role.STUDENT) {
        attendanceService.backfillStudentAttendance(user);
    }
    }

    // ------------------------------------------------------------------ //
    //  Manual attendance override — enforce department check
    // ------------------------------------------------------------------ //
    @Transactional
    public AttendanceDay overrideAttendance(User hod, Long studentId, String date,
                                            AttendanceStatus newStatus, String reason) {
        // Verify student is in HOD's own department
        User student = userRepo.findById(studentId)
                .orElseThrow(() -> new NoSuchElementException("Student not found."));
        String hodBranch = hodDepartmentToBranch(hod.getDepartment());
        if (hodBranch != null && !hodBranch.equalsIgnoreCase(student.getBranch())) {
            throw new SecurityException(
                    "You can only modify attendance for students in your department (" + hodBranch + ").");
        }

        LocalDate d = LocalDate.parse(date);
        AttendanceDay record = attendanceDayRepo.findByStudentIdAndDate(studentId, d)
                .orElseThrow(() -> new NoSuchElementException("Attendance record not found."));

        record.setOriginalStatus(record.getStatus());
        record.setOverriddenBy(hod);
        record.setOverrideReason(reason);
        record.setOverriddenAt(LocalDateTime.now());
        record.setStatus(newStatus);
        attendanceDayRepo.save(record);

        attendanceService.updateMonthlySummary(
                studentId, record.getCollege().getId(), record.getMonth(), record.getYear());
        return record;
    }

    // ------------------------------------------------------------------ //
    //  Pending approvals — only HOD's branch students + all faculty
    // ------------------------------------------------------------------ //
    public List<User> getPendingApprovals(User hod) {
        String hodBranch = hodDepartmentToBranch(hod.getDepartment());
        Long   collegeId = hod.getCollege().getId();

        // Pending students filtered to HOD's branch only
        List<User> pendingStudents = userRepo
                .findByCollegeIdAndIsApprovedFalseAndRoleIn(collegeId, List.of(Role.STUDENT))
                .stream()
                .filter(s -> hodBranch == null || hodBranch.equalsIgnoreCase(s.getBranch()))
                .toList();

        // Pending faculty — HOD approves all faculty in the college (not branch-specific)
        List<User> pendingFaculty = userRepo
                .findByCollegeIdAndIsApprovedFalseAndRoleIn(collegeId, List.of(Role.FACULTY));

        List<User> combined = new ArrayList<>();
        combined.addAll(pendingStudents);
        combined.addAll(pendingFaculty);
        return combined;
    }

    // ------------------------------------------------------------------ //
    //  Monthly report — only HOD's branch
    // ------------------------------------------------------------------ //
    public List<AttendanceSummary> getMonthlyReport(User hod, int month, int year, String branch) {
        String hodBranch = hodDepartmentToBranch(hod.getDepartment());

        return summaryRepo.findByCollegeIdAndMonthAndYear(hod.getCollege().getId(), month, year)
                .stream()
                // ENFORCE HOD's department branch
                .filter(s -> s.getStudent() != null)
                .filter(s -> hodBranch == null || hodBranch.equalsIgnoreCase(s.getStudent().getBranch()))
                .toList();
    }

    // ------------------------------------------------------------------ //
    //  Helper — build student response map
    // ------------------------------------------------------------------ //
    private Map<String, Object> toStudentMap(User s) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id",         s.getId());
        map.put("name",       s.getName());
        map.put("email",      s.getEmail());
        map.put("pinNumber",  s.getPinNumber());
        map.put("branch",     s.getBranch());
        map.put("semester",   s.getSemester());
        map.put("isApproved", s.getApproved());
        map.put("faceImage",  s.getFaceImage());
        map.put("createdAt",  s.getCreatedAt());
        return map;
    }
}
