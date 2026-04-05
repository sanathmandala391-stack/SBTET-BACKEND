//package com.sbtetAttendance.sbtet.service;
//
//
//
//import com.sbtetAttendance.sbtet.model.*;
//import com.sbtetAttendance.sbtet.Repository.*;
//import com.sbtetAttendance.sbtet.util.GeofenceUtil;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.time.LocalDate;
//import java.time.LocalDateTime;
//import java.time.temporal.ChronoUnit;
//import java.util.*;
//
//@Service
//public class AttendanceService {
//
//    private final AttendanceDayRepository attendanceDayRepo;
//    private final AttendanceSummaryRepository summaryRepo;
//    private final UserRepository userRepo;
//    private final CollegeRepository collegeRepo;
//    private final CollegeHolidayRepository holidayRepo;
//    private final GeofenceUtil geofenceUtil;
//
//    @Value("${attendance.gap.hours:6}")
//    private int gapHours;
//
//    public AttendanceService(AttendanceDayRepository attendanceDayRepo,
//                             AttendanceSummaryRepository summaryRepo,
//                             UserRepository userRepo,
//                             CollegeRepository collegeRepo,
//                             CollegeHolidayRepository holidayRepo,
//                             GeofenceUtil geofenceUtil) {
//        this.attendanceDayRepo = attendanceDayRepo;
//        this.summaryRepo = summaryRepo;
//        this.userRepo = userRepo;
//        this.collegeRepo = collegeRepo;
//        this.holidayRepo = holidayRepo;
//        this.geofenceUtil = geofenceUtil;
//    }
//
//    @Transactional
//    public Map<String, Object> checkIn(User student, double latitude, double longitude, boolean faceVerified) {
//        if (!faceVerified)
//            throw new IllegalArgumentException("Face verification failed. Please try again.");
//
//        College college = student.getCollege();
//        if (college == null)
//            throw new IllegalArgumentException("College not found.");
//
//        if (!geofenceUtil.isWithinCollege(latitude, longitude, college)) {
//            int dist = geofenceUtil.getDistanceFromCollege(latitude, longitude, college);
//            throw new IllegalArgumentException("You are outside the college premises. You are " + dist + "m away.");
//        }
//
//        LocalDate today = LocalDate.now();
//        List<CollegeHoliday> holidays = holidayRepo.findByCollegeId(college.getId());
//
//        if (geofenceUtil.isWeekend(today))
//            throw new IllegalArgumentException("Today is a weekend. No attendance required.");
//        if (geofenceUtil.isHoliday(today, holidays))
//            throw new IllegalArgumentException("Today is a holiday. No attendance required.");
//
//        Optional<AttendanceDay> existing = attendanceDayRepo.findByStudentIdAndDate(student.getId(), today);
//        if (existing.isPresent()) {
//            AttendanceDay rec = existing.get();
//            if (rec.getCheckInTime() != null) {
//                if (rec.getCheckOutTime() != null)
//                    throw new IllegalArgumentException("Attendance already marked for today.");
//                throw new IllegalArgumentException("Already checked in. Check out after " + gapHours + " hours.");
//            }
//        }
//
//        LocalDateTime now = LocalDateTime.now();
//        AttendanceDay record = existing.orElseGet(AttendanceDay::new);
//        record.setStudent(student);
//        record.setCollege(college);
//        record.setDate(today);
//        record.setMonth(today.getMonthValue());
//        record.setYear(today.getYear());
//        record.setDayOfMonth(today.getDayOfMonth());
//        record.setCheckInTime(now);
//        record.setCheckInLat(latitude);
//        record.setCheckInLon(longitude);
//        record.setCheckInFaceVerified(true);
//        record.setStatus(AttendanceStatus.E);
//        attendanceDayRepo.save(record);
//
//        return Map.of("message", "Check-in successful! Remember to check out after " + gapHours + " hours.", "checkInTime", now);
//    }
//
//    @Transactional
//    public Map<String, Object> checkOut(User student, double latitude, double longitude, boolean faceVerified) {
//        if (!faceVerified)
//            throw new IllegalArgumentException("Face verification failed. Please try again.");
//
//        College college = student.getCollege();
//        if (!geofenceUtil.isWithinCollege(latitude, longitude, college)) {
//            int dist = geofenceUtil.getDistanceFromCollege(latitude, longitude, college);
//            throw new IllegalArgumentException("You are outside the college premises (" + dist + "m away).");
//        }
//
//        LocalDate today = LocalDate.now();
//        AttendanceDay record = attendanceDayRepo.findByStudentIdAndDate(student.getId(), today)
//                .orElseThrow(() -> new IllegalArgumentException("No check-in found for today. Please check in first."));
//
//        if (record.getCheckInTime() == null)
//            throw new IllegalArgumentException("No check-in found for today.");
//        if (record.getCheckOutTime() != null)
//            throw new IllegalArgumentException("Already checked out today.");
//
//        LocalDateTime now = LocalDateTime.now();
//        long minutes = ChronoUnit.MINUTES.between(record.getCheckInTime(), now);
//        double hours = minutes / 60.0;
//        AttendanceStatus status = hours >= gapHours ? AttendanceStatus.P : AttendanceStatus.HD;
//
//        record.setCheckOutTime(now);
//        record.setCheckOutLat(latitude);
//        record.setCheckOutLon(longitude);
//        record.setCheckOutFaceVerified(true);
//        record.setGapMinutes((int) minutes);
//        record.setStatus(status);
//        attendanceDayRepo.save(record);
//
//        updateMonthlySummary(student.getId(), college.getId(), record.getMonth(), record.getYear());
//
//        String msg = status == AttendanceStatus.P
//                ? "Check-out successful! Full attendance marked for today."
//                : "Check-out recorded as Half-Day. Gap was only " + (int)(hours) + "h " + (minutes % 60) + "m (need " + gapHours + "h for full day).";
//
//        return Map.of("message", msg, "status", status, "gapMinutes", minutes, "checkOutTime", now);
//    }
//
//    public Map<String, Object> getStudentSummary(Long studentId) {
//        User student = userRepo.findById(studentId)
//                .orElseThrow(() -> new NoSuchElementException("Student not found."));
//        List<AttendanceDay> records = attendanceDayRepo.findByStudentIdOrderByDateAsc(studentId);
//
//        Map<String, Map<String, Object>> monthlyData = new LinkedHashMap<>();
//        for (AttendanceDay r : records) {
//            String key = r.getYear() + "-" + r.getMonth();
//            monthlyData.computeIfAbsent(key, k -> {
//                Map<String, Object> m = new LinkedHashMap<>();
//                m.put("year", r.getYear()); m.put("month", r.getMonth());
//                m.put("days", new LinkedHashMap<Integer, String>());
//                return m;
//            });
//            @SuppressWarnings("unchecked")
//            Map<Integer, String> days = (Map<Integer, String>) monthlyData.get(key).get("days");
//            days.put(r.getDayOfMonth(), r.getStatus().name());
//        }
//
//        long workingDays = records.stream().filter(r -> !List.of(AttendanceStatus.W, AttendanceStatus.H, AttendanceStatus.NONE).contains(r.getStatus())).count();
//        long present = records.stream().filter(r -> r.getStatus() == AttendanceStatus.P).count();
//        long halfDay = records.stream().filter(r -> r.getStatus() == AttendanceStatus.HD).count();
//        long absent = records.stream().filter(r -> r.getStatus() == AttendanceStatus.A).count();
//        double percentage = workingDays > 0 ? Math.round(((present + halfDay * 0.5) / workingDays * 100) * 100.0) / 100.0 : 0;
//
//        return Map.of(
//                "student", Map.of(
//                        "name", student.getName(),
//                        "pinNumber", Objects.toString(student.getPinNumber(), ""),
//                        "attendeeId", Objects.toString(student.getAttendeeId(), ""),
//                        "branch", Objects.toString(student.getBranch(), ""),
//                        "semester", Objects.toString(student.getSemester(), ""),
//                        "collegeCode", Objects.toString(student.getCollegeCode(), "")
//                ),
//                "stats", Map.of(
//                        "workingDays", workingDays, "presentDays", present,
//                        "halfDays", halfDay, "absentDays", absent, "percentage", percentage
//                ),
//                "monthlyData", new ArrayList<>(monthlyData.values()),
//                "records", records
//        );
//    }
//
//    public Optional<AttendanceDay> getTodayStatus(Long studentId) {
//        return attendanceDayRepo.findByStudentIdAndDate(studentId, LocalDate.now());
//    }
//
//    @Transactional
//    public void updateMonthlySummary(Long studentId, Long collegeId, int month, int year) {
//        List<AttendanceDay> records = attendanceDayRepo.findByStudentIdAndMonthAndYear(studentId, month, year);
//        int workingDays = (int) records.stream().filter(r -> !List.of(AttendanceStatus.W, AttendanceStatus.H, AttendanceStatus.NONE).contains(r.getStatus())).count();
//        int present = (int) records.stream().filter(r -> r.getStatus() == AttendanceStatus.P).count();
//        int halfDay = (int) records.stream().filter(r -> r.getStatus() == AttendanceStatus.HD).count();
//        int absent = (int) records.stream().filter(r -> r.getStatus() == AttendanceStatus.A).count();
//        int error = (int) records.stream().filter(r -> r.getStatus() == AttendanceStatus.E).count();
//        int holiday = (int) records.stream().filter(r -> r.getStatus() == AttendanceStatus.H).count();
//        int weekend = (int) records.stream().filter(r -> r.getStatus() == AttendanceStatus.W).count();
//        double pct = workingDays > 0 ? Math.round(((present + halfDay * 0.5) / workingDays * 100) * 100.0) / 100.0 : 0;
//
//        User student = userRepo.getReferenceById(studentId);
//        College college = collegeRepo.getReferenceById(collegeId);
//
//        AttendanceSummary summary = summaryRepo.findByStudentIdAndMonthAndYear(studentId, month, year)
//                .orElseGet(AttendanceSummary::new);
//        summary.setStudent(student);
//        summary.setCollege(college);
//        summary.setMonth(month);
//        summary.setYear(year);
//        summary.setTotalWorkingDays(workingDays);
//        summary.setDaysPresent(present);
//        summary.setDaysHalfDay(halfDay);
//        summary.setDaysAbsent(absent);
//        summary.setDaysError(error);
//        summary.setDaysHoliday(holiday);
//        summary.setDaysWeekend(weekend);
//        summary.setAttendancePercentage(pct);
//        summaryRepo.save(summary);
//    }
//
//    /** Cron: End-of-day job */
//    @Transactional
//    public void markEndOfDayAttendance() {
//        LocalDate today = LocalDate.now();
//
//        // Mark E for students who only checked-in (no check-out)
//        List<AttendanceDay> incompleteRecords = attendanceDayRepo
//                .findByDateAndStatusAndCheckOutTimeIsNull(today, AttendanceStatus.E);
//        incompleteRecords.forEach(r -> {
//            attendanceDayRepo.save(r); // Status already E
//            updateMonthlySummary(r.getStudent().getId(), r.getCollege().getId(), r.getMonth(), r.getYear());
//        });
//
//        // Mark Absent for students with no record
//        List<College> colleges = collegeRepo.findByIsActiveTrueOrderByCreatedAtDesc();
//        for (College college : colleges) {
//            List<CollegeHoliday> holidays = holidayRepo.findByCollegeId(college.getId());
//            if (geofenceUtil.isWeekend(today) || geofenceUtil.isHoliday(today, holidays)) continue;
//
//            List<User> students = userRepo.findByCollegeIdAndRoleAndIsApprovedAndIsActive(
//                    college.getId(), Role.STUDENT, true, true);
//            for (User student : students) {
//                attendanceDayRepo.findByStudentIdAndDate(student.getId(), today).ifPresentOrElse(
//                        r -> {},
//                        () -> {
//                            AttendanceDay absent = new AttendanceDay();
//                            absent.setStudent(student);
//                            absent.setCollege(college);
//                            absent.setDate(today);
//                            absent.setStatus(AttendanceStatus.A);
//                            absent.setMonth(today.getMonthValue());
//                            absent.setYear(today.getYear());
//                            absent.setDayOfMonth(today.getDayOfMonth());
//                            attendanceDayRepo.save(absent);
//                            updateMonthlySummary(student.getId(), college.getId(),
//                                    today.getMonthValue(), today.getYear());
//                        }
//                );
//            }
//        }
//    }
//}


//second//





//package com.sbtetAttendance.sbtet.service;
//
//import com.sbtetAttendance.sbtet.model.*;
//import com.sbtetAttendance.sbtet.Repository.*;
//import com.sbtetAttendance.sbtet.util.GeofenceUtil;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.time.LocalDate;
//import java.time.LocalDateTime;
//import java.time.temporal.ChronoUnit;
//import java.util.*;
//
//@Service
//public class AttendanceService {
//
//    private final AttendanceDayRepository attendanceDayRepo;
//    private final AttendanceSummaryRepository summaryRepo;
//    private final UserRepository userRepo;
//    private final CollegeRepository collegeRepo;
//    private final CollegeHolidayRepository holidayRepo;
//    private final GeofenceUtil geofenceUtil;
//
//    @Value("${attendance.gap.hours:6}")
//    private int gapHours;
//
//    public AttendanceService(AttendanceDayRepository attendanceDayRepo,
//                             AttendanceSummaryRepository summaryRepo,
//                             UserRepository userRepo,
//                             CollegeRepository collegeRepo,
//                             CollegeHolidayRepository holidayRepo,
//                             GeofenceUtil geofenceUtil) {
//        this.attendanceDayRepo = attendanceDayRepo;
//        this.summaryRepo       = summaryRepo;
//        this.userRepo          = userRepo;
//        this.collegeRepo       = collegeRepo;
//        this.holidayRepo       = holidayRepo;
//        this.geofenceUtil      = geofenceUtil;
//    }
//
//    // ─────────────────────────────────────────────────────────────────────────
//    // CHECK IN
//    // ─────────────────────────────────────────────────────────────────────────
//    @Transactional
//    public Map<String, Object> checkIn(User student, double latitude, double longitude, boolean faceVerified) {
//        if (!faceVerified)
//            throw new IllegalArgumentException("Face verification failed. Please try again.");
//
//        College college = student.getCollege();
//        if (college == null)
//            throw new IllegalArgumentException("College not found.");
//
//        if (!geofenceUtil.isWithinCollege(latitude, longitude, college)) {
//            int dist = geofenceUtil.getDistanceFromCollege(latitude, longitude, college);
//            throw new IllegalArgumentException(
//                    "You are outside the college premises. You are " + dist + "m away. " +
//                            "Please come to college to mark attendance.");
//        }
//
//        LocalDate today = LocalDate.now();
//        List<CollegeHoliday> holidays = holidayRepo.findByCollegeId(college.getId());
//
//        if (geofenceUtil.isWeekend(today))
//            throw new IllegalArgumentException("Today is a weekend. No attendance required.");
//        if (geofenceUtil.isHoliday(today, holidays))
//            throw new IllegalArgumentException("Today is a holiday. No attendance required.");
//
//        Optional<AttendanceDay> existing = attendanceDayRepo.findByStudentIdAndDate(student.getId(), today);
//        if (existing.isPresent()) {
//            AttendanceDay rec = existing.get();
//            if (rec.getCheckInTime() != null) {
//                if (rec.getCheckOutTime() != null)
//                    throw new IllegalArgumentException("Attendance already marked for today.");
//                throw new IllegalArgumentException(
//                        "Already checked in. Check out after " + gapHours + " hours.");
//            }
//        }
//
//        LocalDateTime now = LocalDateTime.now();
//        AttendanceDay record = existing.orElseGet(AttendanceDay::new);
//        record.setStudent(student);
//        record.setCollege(college);
//        record.setDate(today);
//        record.setMonth(today.getMonthValue());
//        record.setYear(today.getYear());
//        record.setDayOfMonth(today.getDayOfMonth());
//        record.setCheckInTime(now);
//        record.setCheckInLat(latitude);
//        record.setCheckInLon(longitude);
//        record.setCheckInFaceVerified(true);
//        record.setStatus(AttendanceStatus.E);  // confirmed on checkout
//        attendanceDayRepo.save(record);
//
//        return Map.of(
//                "message",     "Check-in successful! Remember to check out after " + gapHours + " hours.",
//                "checkInTime", now
//        );
//    }
//
//    // ─────────────────────────────────────────────────────────────────────────
//    // CHECK OUT
//    // ─────────────────────────────────────────────────────────────────────────
//    @Transactional
//    public Map<String, Object> checkOut(User student, double latitude, double longitude, boolean faceVerified) {
//        if (!faceVerified)
//            throw new IllegalArgumentException("Face verification failed. Please try again.");
//
//        College college = student.getCollege();
//        if (!geofenceUtil.isWithinCollege(latitude, longitude, college)) {
//            int dist = geofenceUtil.getDistanceFromCollege(latitude, longitude, college);
//            throw new IllegalArgumentException(
//                    "You are outside the college premises (" + dist + "m away). " +
//                            "Please be in college to check out.");
//        }
//
//        LocalDate today = LocalDate.now();
//        AttendanceDay record = attendanceDayRepo.findByStudentIdAndDate(student.getId(), today)
//                .orElseThrow(() -> new IllegalArgumentException(
//                        "No check-in found for today. Please check in first."));
//
//        if (record.getCheckInTime() == null)
//            throw new IllegalArgumentException("No check-in found for today.");
//        if (record.getCheckOutTime() != null)
//            throw new IllegalArgumentException("Already checked out today.");
//
//        LocalDateTime now     = LocalDateTime.now();
//        long minutes          = ChronoUnit.MINUTES.between(record.getCheckInTime(), now);
//        double hours          = minutes / 60.0;
//        AttendanceStatus status = hours >= gapHours ? AttendanceStatus.P : AttendanceStatus.HD;
//
//        record.setCheckOutTime(now);
//        record.setCheckOutLat(latitude);
//        record.setCheckOutLon(longitude);
//        record.setCheckOutFaceVerified(true);
//        record.setGapMinutes((int) minutes);
//        record.setStatus(status);
//        attendanceDayRepo.save(record);
//
//        updateMonthlySummary(student.getId(), college.getId(), record.getMonth(), record.getYear());
//
//        String msg = status == AttendanceStatus.P
//                ? "Check-out successful! Full attendance marked for today."
//                : "Check-out recorded as Half-Day. Gap was only "
//                + (int) hours + "h " + (minutes % 60) + "m (need " + gapHours + "h for full day).";
//
//        return Map.of(
//                "message",      msg,
//                "status",       status.name(),
//                "gapMinutes",   minutes,
//                "checkOutTime", now
//        );
//    }
//
//    // ─────────────────────────────────────────────────────────────────────────
//    // GET STUDENT SUMMARY  — SBTET portal style
//    //
//    // SBTET formula (from official www.sbtet.telangana.gov.in portal):
//    //
//    //   workingDays    = count of days that are P, A, E, or HD  (NOT W or H)
//    //   presentDays    = count of P records only
//    //   halfDays       = count of HD records  (shown as "HP" on portal)
//    //   effectivePresent = presentDays + halfDays
//    //                    (HP counts the same as P in the numerator on SBTET portal)
//    //   percentage     = effectivePresent / workingDays × 100
//    //
//    //   Example: 49 present + 0 halfday, 90 working days
//    //            → 49/90 × 100 = 54.44%
//    //            Each present day = 1/90 = 1.11%
//    //
//    // Detention rules:
//    //   ≥ 75%          → ELIGIBLE for examination
//    //   65% – 74.99%   → CONDONATION  (must pay fee)
//    //   < 65%          → DETAINED
//    // ─────────────────────────────────────────────────────────────────────────
//    public Map<String, Object> getStudentSummary(Long studentId) {
//        User student = userRepo.findById(studentId)
//                .orElseThrow(() -> new NoSuchElementException("Student not found."));
//
//        // All records for this student, chronological
//        List<AttendanceDay> records = attendanceDayRepo.findByStudentIdOrderByDateAsc(studentId);
//
//        // ── Build monthly grid ────────────────────────────────────────────────
//        // HD is displayed as "HP" on the SBTET portal
//        Map<String, Map<String, Object>> monthlyMap = new LinkedHashMap<>();
//        for (AttendanceDay r : records) {
//            String key = r.getYear() + "-" + String.format("%02d", r.getMonth());
//            monthlyMap.computeIfAbsent(key, k -> {
//                Map<String, Object> m = new LinkedHashMap<>();
//                m.put("year",  r.getYear());
//                m.put("month", r.getMonth());
//                m.put("days",  new TreeMap<Integer, String>());
//                return m;
//            });
//            @SuppressWarnings("unchecked")
//            Map<Integer, String> days = (Map<Integer, String>) monthlyMap.get(key).get("days");
//            // Show HD as HP to match SBTET portal label
//            days.put(r.getDayOfMonth(),
//                    r.getStatus() == AttendanceStatus.HD ? "HP" : r.getStatus().name());
//        }
//
//        // Sort months chronologically
//        List<Map<String, Object>> monthlyData = monthlyMap.entrySet().stream()
//                .sorted(Map.Entry.comparingByKey())
//                .map(Map.Entry::getValue)
//                .toList();
//
//        // ── Calculate totals ──────────────────────────────────────────────────
//        // Working day = any day that is NOT W (weekend) or H (holiday) or NONE
//        Set<AttendanceStatus> nonWorking = Set.of(
//                AttendanceStatus.W, AttendanceStatus.H, AttendanceStatus.NONE);
//
//        long workingDays      = records.stream()
//                .filter(r -> !nonWorking.contains(r.getStatus())).count();
//        long presentDays      = records.stream()
//                .filter(r -> r.getStatus() == AttendanceStatus.P).count();
//        long halfDays         = records.stream()
//                .filter(r -> r.getStatus() == AttendanceStatus.HD).count();
//        long absentDays       = records.stream()
//                .filter(r -> r.getStatus() == AttendanceStatus.A).count();
//        long errorDays        = records.stream()
//                .filter(r -> r.getStatus() == AttendanceStatus.E).count();
//
//        // SBTET counts HP (halfDay) same as P in the numerator
//        long effectivePresent = presentDays + halfDays;
//
//        // percentage = effectivePresent / workingDays × 100, rounded to 2 decimal places
//        double percentage = workingDays > 0
//                ? Math.round((effectivePresent * 100.0 / workingDays) * 100.0) / 100.0
//                : 0.0;
//
//        // Detention status
//        String detentionStatus =
//                percentage >= 75.0 ? "ELIGIBLE" :
//                        percentage >= 65.0 ? "CONDONATION" :
//                                "DETAINED";
//
//        // How many more present days needed to reach 75%?
//        // Solve: (effectivePresent + x) / (workingDays + x) = 0.75
//        //        x = (0.75 * workingDays - effectivePresent) / 0.25
//        int daysNeededFor75 = percentage >= 75.0 ? 0
//                : (int) Math.ceil((0.75 * workingDays - effectivePresent) / 0.25);
//
//        // Per-day impact (how much % does one present day add)
//        double perDayImpact = workingDays > 0
//                ? Math.round((100.0 / workingDays) * 100.0) / 100.0
//                : 0.0;
//
//        // ── Build response ────────────────────────────────────────────────────
//        Map<String, Object> studentMap = new LinkedHashMap<>();
//        studentMap.put("name",        student.getName());
//        studentMap.put("pinNumber",   Objects.toString(student.getPinNumber(),   ""));
//        studentMap.put("attendeeId",  Objects.toString(student.getAttendeeId(),  ""));
//        studentMap.put("branch",      Objects.toString(student.getBranch(),      ""));
//        studentMap.put("semester",    Objects.toString(student.getSemester(),    ""));
//        studentMap.put("collegeCode", Objects.toString(student.getCollegeCode(), ""));
//
//        Map<String, Object> statsMap = new LinkedHashMap<>();
//        statsMap.put("workingDays",       workingDays);       // denominator (e.g. 90)
//        statsMap.put("presentDays",       presentDays);       // pure P count
//        statsMap.put("halfDays",          halfDays);          // HP count
//        statsMap.put("effectivePresent",  effectivePresent);  // P + HP  (what SBTET shows as "days present")
//        statsMap.put("absentDays",        absentDays);
//        statsMap.put("errorDays",         errorDays);
//        statsMap.put("percentage",        percentage);        // e.g. 54.44
//        statsMap.put("detentionStatus",   detentionStatus);   // ELIGIBLE | CONDONATION | DETAINED
//        statsMap.put("daysNeededFor75",   daysNeededFor75);   // days needed to reach 75%
//        statsMap.put("perDayImpact",      perDayImpact);      // % gained per 1 present day
//        statsMap.put("lastCalculated",    LocalDateTime.now());
//
//        return Map.of(
//                "student",     studentMap,
//                "stats",       statsMap,
//                "monthlyData", monthlyData,
//                "records",     records
//        );
//    }
//
//    // ─────────────────────────────────────────────────────────────────────────
//    // TODAY STATUS
//    // ─────────────────────────────────────────────────────────────────────────
//    public Optional<AttendanceDay> getTodayStatus(Long studentId) {
//        return attendanceDayRepo.findByStudentIdAndDate(studentId, LocalDate.now());
//    }
//
//    // ─────────────────────────────────────────────────────────────────────────
//    // UPDATE MONTHLY SUMMARY  (called after checkout + end-of-day cron)
//    // Uses same SBTET formula: effectivePresent = present + halfDay
//    // ─────────────────────────────────────────────────────────────────────────
//    @Transactional
//    public void updateMonthlySummary(Long studentId, Long collegeId, int month, int year) {
//        List<AttendanceDay> records = attendanceDayRepo
//                .findByStudentIdAndMonthAndYear(studentId, month, year);
//
//        Set<AttendanceStatus> nonWorking = Set.of(
//                AttendanceStatus.W, AttendanceStatus.H, AttendanceStatus.NONE);
//
//        int workingDays = (int) records.stream()
//                .filter(r -> !nonWorking.contains(r.getStatus())).count();
//        int present  = (int) records.stream().filter(r -> r.getStatus() == AttendanceStatus.P).count();
//        int halfDay  = (int) records.stream().filter(r -> r.getStatus() == AttendanceStatus.HD).count();
//        int absent   = (int) records.stream().filter(r -> r.getStatus() == AttendanceStatus.A).count();
//        int error    = (int) records.stream().filter(r -> r.getStatus() == AttendanceStatus.E).count();
//        int holiday  = (int) records.stream().filter(r -> r.getStatus() == AttendanceStatus.H).count();
//        int weekend  = (int) records.stream().filter(r -> r.getStatus() == AttendanceStatus.W).count();
//
//        // SBTET formula: effectivePresent = present + halfDay (HP counts same as P)
//        int effectivePresent = present + halfDay;
//        double pct = workingDays > 0
//                ? Math.round((effectivePresent * 100.0 / workingDays) * 100.0) / 100.0
//                : 0.0;
//
//        User    studentRef = userRepo.getReferenceById(studentId);
//        College collegeRef = collegeRepo.getReferenceById(collegeId);
//
//        AttendanceSummary summary = summaryRepo
//                .findByStudentIdAndMonthAndYear(studentId, month, year)
//                .orElseGet(AttendanceSummary::new);
//
//        summary.setStudent(studentRef);
//        summary.setCollege(collegeRef);
//        summary.setMonth(month);
//        summary.setYear(year);
//        summary.setTotalWorkingDays(workingDays);
//        summary.setDaysPresent(present);
//        summary.setDaysHalfDay(halfDay);
//        summary.setDaysAbsent(absent);
//        summary.setDaysError(error);
//        summary.setDaysHoliday(holiday);
//        summary.setDaysWeekend(weekend);
//        summary.setAttendancePercentage(pct);
//        summaryRepo.save(summary);
//    }
//
//    // ─────────────────────────────────────────────────────────────────────────
//    // END-OF-DAY CRON  (called from SchedulerService at 11:59 PM IST)
//    // ─────────────────────────────────────────────────────────────────────────
//    @Transactional
//    public void markEndOfDayAttendance() {
//        LocalDate today = LocalDate.now();
//
//        // 1. Confirm E status for students who checked in but never checked out
//        List<AttendanceDay> incomplete = attendanceDayRepo
//                .findByDateAndStatusAndCheckOutTimeIsNull(today, AttendanceStatus.E);
//        for (AttendanceDay rec : incomplete) {
//            attendanceDayRepo.save(rec);   // status stays E, triggers updatedAt
//            updateMonthlySummary(
//                    rec.getStudent().getId(),
//                    rec.getCollege().getId(),
//                    rec.getMonth(), rec.getYear());
//        }
//
//        // 2. Mark Absent for every approved student who has NO record at all today
//        List<College> colleges = collegeRepo.findByIsActiveTrueOrderByCreatedAtDesc();
//        for (College college : colleges) {
//            List<CollegeHoliday> holidays = holidayRepo.findByCollegeId(college.getId());
//            // Skip if today is weekend or holiday for this college
//            if (geofenceUtil.isWeekend(today) || geofenceUtil.isHoliday(today, holidays)) continue;
//
//            List<User> students = userRepo.findByCollegeIdAndRoleAndIsApprovedAndIsActive(
//                    college.getId(), Role.STUDENT, true, true);
//
//            for (User student : students) {
//                attendanceDayRepo.findByStudentIdAndDate(student.getId(), today).ifPresentOrElse(
//                        existing -> { /* already has a record — do nothing */ },
//                        () -> {
//                            AttendanceDay absentRec = new AttendanceDay();
//                            absentRec.setStudent(student);
//                            absentRec.setCollege(college);
//                            absentRec.setDate(today);
//                            absentRec.setStatus(AttendanceStatus.A);
//                            absentRec.setMonth(today.getMonthValue());
//                            absentRec.setYear(today.getYear());
//                            absentRec.setDayOfMonth(today.getDayOfMonth());
//                            attendanceDayRepo.save(absentRec);
//                            updateMonthlySummary(student.getId(), college.getId(),
//                                    today.getMonthValue(), today.getYear());
//                        }
//                );
//            }
//        }
//    }
//}






//third//






//
//package com.sbtetAttendance.sbtet.service;
//
//import com.sbtetAttendance.sbtet.model.*;
//import com.sbtetAttendance.sbtet.Repository.*;
//import com.sbtetAttendance.sbtet.util.GeofenceUtil;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.time.LocalDate;
//import java.time.LocalDateTime;
//import java.time.temporal.ChronoUnit;
//import java.util.*;
//
//@Service
//public class AttendanceService {
//
//    private final AttendanceDayRepository attendanceDayRepo;
//    private final AttendanceSummaryRepository summaryRepo;
//    private final UserRepository userRepo;
//    private final CollegeRepository collegeRepo;
//    private final CollegeHolidayRepository holidayRepo;
//    private final GeofenceUtil geofenceUtil;
//
//    @Value("${attendance.gap.hours:6}")
//    private int gapHours;
//
//    /**
//     * SBTET Diploma: each semester has approximately 90 working days total.
//     * This is the FIXED denominator used to calculate attendance percentage.
//     *
//     * 1 present day = 1/90 = 1.11%
//     * 2 present days = 2/90 = 2.22%
//     * ...
//     * 90 present days = 90/90 = 100%
//     *
//     * Set in application.properties:
//     *   attendance.semester.working.days=90
//     */
//    @Value("${attendance.semester.working.days:90}")
//    private int semesterTotalWorkingDays;
//
//    public AttendanceService(AttendanceDayRepository attendanceDayRepo,
//                             AttendanceSummaryRepository summaryRepo,
//                             UserRepository userRepo,
//                             CollegeRepository collegeRepo,
//                             CollegeHolidayRepository holidayRepo,
//                             GeofenceUtil geofenceUtil) {
//        this.attendanceDayRepo = attendanceDayRepo;
//        this.summaryRepo       = summaryRepo;
//        this.userRepo          = userRepo;
//        this.collegeRepo       = collegeRepo;
//        this.holidayRepo       = holidayRepo;
//        this.geofenceUtil      = geofenceUtil;
//    }
//
//    // ─────────────────────────────────────────────────────────────────────────
//    // CHECK IN
//    // ─────────────────────────────────────────────────────────────────────────
//    @Transactional
//    public Map<String, Object> checkIn(User student, double latitude, double longitude, boolean faceVerified) {
//        if (!faceVerified)
//            throw new IllegalArgumentException("Face verification failed. Please try again.");
//
//        College college = student.getCollege();
//        if (college == null)
//            throw new IllegalArgumentException("College not found.");
//
//        if (!geofenceUtil.isWithinCollege(latitude, longitude, college)) {
//            int dist = geofenceUtil.getDistanceFromCollege(latitude, longitude, college);
//            throw new IllegalArgumentException(
//                    "You are outside the college premises. You are " + dist + "m away.");
//        }
//
//        LocalDate today = LocalDate.now();
//        List<CollegeHoliday> holidays = holidayRepo.findByCollegeId(college.getId());
//
//        if (geofenceUtil.isWeekend(today))
//            throw new IllegalArgumentException("Today is a weekend. No attendance required.");
//        if (geofenceUtil.isHoliday(today, holidays))
//            throw new IllegalArgumentException("Today is a holiday. No attendance required.");
//
//        Optional<AttendanceDay> existing = attendanceDayRepo.findByStudentIdAndDate(student.getId(), today);
//        if (existing.isPresent()) {
//            AttendanceDay rec = existing.get();
//            if (rec.getCheckInTime() != null) {
//                if (rec.getCheckOutTime() != null)
//                    throw new IllegalArgumentException("Attendance already marked for today.");
//                throw new IllegalArgumentException(
//                        "Already checked in. Check out after " + gapHours + " hours.");
//            }
//        }
//
//        LocalDateTime now = LocalDateTime.now();
//        AttendanceDay record = existing.orElseGet(AttendanceDay::new);
//        record.setStudent(student);
//        record.setCollege(college);
//        record.setDate(today);
//        record.setMonth(today.getMonthValue());
//        record.setYear(today.getYear());
//        record.setDayOfMonth(today.getDayOfMonth());
//        record.setCheckInTime(now);
//        record.setCheckInLat(latitude);
//        record.setCheckInLon(longitude);
//        record.setCheckInFaceVerified(true);
//        record.setStatus(AttendanceStatus.E);
//        attendanceDayRepo.save(record);
//
//        return Map.of(
//                "message",     "Check-in successful! Remember to check out after " + gapHours + " hours.",
//                "checkInTime", now
//        );
//    }
//
//    // ─────────────────────────────────────────────────────────────────────────
//    // CHECK OUT
//    // ─────────────────────────────────────────────────────────────────────────
//    @Transactional
//    public Map<String, Object> checkOut(User student, double latitude, double longitude, boolean faceVerified) {
//        if (!faceVerified)
//            throw new IllegalArgumentException("Face verification failed. Please try again.");
//
//        College college = student.getCollege();
//        if (!geofenceUtil.isWithinCollege(latitude, longitude, college)) {
//            int dist = geofenceUtil.getDistanceFromCollege(latitude, longitude, college);
//            throw new IllegalArgumentException(
//                    "You are outside the college premises (" + dist + "m away).");
//        }
//
//        LocalDate today = LocalDate.now();
//        AttendanceDay record = attendanceDayRepo.findByStudentIdAndDate(student.getId(), today)
//                .orElseThrow(() -> new IllegalArgumentException(
//                        "No check-in found for today. Please check in first."));
//
//        if (record.getCheckInTime() == null)
//            throw new IllegalArgumentException("No check-in found for today.");
//        if (record.getCheckOutTime() != null)
//            throw new IllegalArgumentException("Already checked out today.");
//
//        LocalDateTime now   = LocalDateTime.now();
//        long minutes        = ChronoUnit.MINUTES.between(record.getCheckInTime(), now);
//        double hours        = minutes / 60.0;
//        AttendanceStatus status = hours >= gapHours ? AttendanceStatus.P : AttendanceStatus.HD;
//
//        record.setCheckOutTime(now);
//        record.setCheckOutLat(latitude);
//        record.setCheckOutLon(longitude);
//        record.setCheckOutFaceVerified(true);
//        record.setGapMinutes((int) minutes);
//        record.setStatus(status);
//        attendanceDayRepo.save(record);
//
//        updateMonthlySummary(student.getId(), college.getId(), record.getMonth(), record.getYear());
//
//        String msg = status == AttendanceStatus.P
//                ? "Check-out successful! Full attendance marked for today."
//                : "Check-out recorded as Half-Day. Gap was only "
//                + (int) hours + "h " + (minutes % 60) + "m (need " + gapHours + "h for full day).";
//
//        return Map.of(
//                "message",      msg,
//                "status",       status.name(),
//                "gapMinutes",   minutes,
//                "checkOutTime", now
//        );
//    }
//
//    // ─────────────────────────────────────────────────────────────────────────
//    // GET STUDENT SUMMARY — SBTET Portal Style
//    //
//    // THE ROOT FIX:
//    //   Old (wrong): workingDays = count of records in DB  → Day 1 = 1/1 = 100%
//    //   New (correct): workingDays = semesterTotalWorkingDays (90) → Day 1 = 1/90 = 1.11%
//    //
//    // SBTET formula:
//    //   effectivePresent = presentDays + halfDays  (HP counts same as P)
//    //   percentage       = effectivePresent / 90   * 100
//    //
//    // Detention rules:
//    //   >= 75% → ELIGIBLE
//    //   >= 65% → CONDONATION (pay fee)
//    //   <  65% → DETAINED
//    // ─────────────────────────────────────────────────────────────────────────
//    public Map<String, Object> getStudentSummary(Long studentId) {
//        User student = userRepo.findById(studentId)
//                .orElseThrow(() -> new NoSuchElementException("Student not found."));
//
//        List<AttendanceDay> records = attendanceDayRepo.findByStudentIdOrderByDateAsc(studentId);
//
//        // ── Build monthly grid (HD shown as HP to match SBTET portal) ─────────
//        Map<String, Map<String, Object>> monthlyMap = new LinkedHashMap<>();
//        for (AttendanceDay r : records) {
//            String key = r.getYear() + "-" + String.format("%02d", r.getMonth());
//            monthlyMap.computeIfAbsent(key, k -> {
//                Map<String, Object> m = new LinkedHashMap<>();
//                m.put("year",  r.getYear());
//                m.put("month", r.getMonth());
//                m.put("days",  new TreeMap<Integer, String>());
//                return m;
//            });
//            @SuppressWarnings("unchecked")
//            Map<Integer, String> days = (Map<Integer, String>) monthlyMap.get(key).get("days");
//            days.put(r.getDayOfMonth(),
//                    r.getStatus() == AttendanceStatus.HD ? "HP" : r.getStatus().name());
//        }
//
//        List<Map<String, Object>> monthlyData = monthlyMap.entrySet().stream()
//                .sorted(Map.Entry.comparingByKey())
//                .map(Map.Entry::getValue)
//                .toList();
//
//        // ── Count actual present/absent days from records ──────────────────────
//        long presentDays = records.stream()
//                .filter(r -> r.getStatus() == AttendanceStatus.P).count();
//        long halfDays    = records.stream()
//                .filter(r -> r.getStatus() == AttendanceStatus.HD).count();
//        long absentDays  = records.stream()
//                .filter(r -> r.getStatus() == AttendanceStatus.A).count();
//        long errorDays   = records.stream()
//                .filter(r -> r.getStatus() == AttendanceStatus.E).count();
//
//        // HP counts same as P in SBTET numerator
//        long effectivePresent = presentDays + halfDays;
//
//        // ── KEY FIX: denominator is fixed semester total (90), not record count ──
//        //
//        //   Example with semesterTotalWorkingDays = 90:
//        //     Day 1 present:  1  / 90 * 100 = 1.11%   ✅
//        //     Day 45 present: 45 / 90 * 100 = 50.00%  ✅
//        //     Day 90 present: 90 / 90 * 100 = 100.00% ✅
//        //
//        int totalWorkingDays = semesterTotalWorkingDays; // 90 from application.properties
//
//        double percentage = totalWorkingDays > 0
//                ? Math.round((effectivePresent * 100.0 / totalWorkingDays) * 100.0) / 100.0
//                : 0.0;
//
//        // ── Detention status ──────────────────────────────────────────────────
//        String detentionStatus =
//                percentage >= 75.0 ? "ELIGIBLE" :
//                        percentage >= 65.0 ? "CONDONATION" :
//                                "DETAINED";
//
//        // ── Days needed to reach 75% ──────────────────────────────────────────
//        // 75% of 90 = 67.5 → need 68 present days total
//        // already have effectivePresent → need (68 - effectivePresent) more
//        long daysNeededFor75 = percentage >= 75.0 ? 0
//                : Math.max(0, (long) Math.ceil(0.75 * totalWorkingDays) - effectivePresent);
//
//        // ── Per-day impact: 1/90 * 100 = 1.11% ───────────────────────────────
//        double perDayImpact = totalWorkingDays > 0
//                ? Math.round((100.0 / totalWorkingDays) * 100.0) / 100.0
//                : 0.0;
//
//        // ── Build response ────────────────────────────────────────────────────
//        Map<String, Object> studentMap = new LinkedHashMap<>();
//        studentMap.put("name",        student.getName());
//        studentMap.put("pinNumber",   Objects.toString(student.getPinNumber(),   ""));
//        studentMap.put("attendeeId",  Objects.toString(student.getAttendeeId(),  ""));
//        studentMap.put("branch",      Objects.toString(student.getBranch(),      ""));
//        studentMap.put("semester",    Objects.toString(student.getSemester(),     ""));
//        studentMap.put("collegeCode", Objects.toString(student.getCollegeCode(), ""));
//
//        Map<String, Object> statsMap = new LinkedHashMap<>();
//        statsMap.put("workingDays",       totalWorkingDays);   // always 90 (semester total)
//        statsMap.put("presentDays",       presentDays);        // pure P count
//        statsMap.put("halfDays",          halfDays);           // HP count
//        statsMap.put("effectivePresent",  effectivePresent);   // P + HP
//        statsMap.put("absentDays",        absentDays);
//        statsMap.put("errorDays",         errorDays);
//        statsMap.put("percentage",        percentage);         // 1.11 on day 1
//        statsMap.put("detentionStatus",   detentionStatus);
//        statsMap.put("daysNeededFor75",   daysNeededFor75);
//        statsMap.put("perDayImpact",      perDayImpact);       // always 1.11 when total=90
//        statsMap.put("lastCalculated",    LocalDateTime.now());
//
//        return Map.of(
//                "student",     studentMap,
//                "stats",       statsMap,
//                "monthlyData", monthlyData,
//                "records",     records
//        );
//    }
//
//    // ─────────────────────────────────────────────────────────────────────────
//    // TODAY STATUS
//    // ─────────────────────────────────────────────────────────────────────────
//    public Optional<AttendanceDay> getTodayStatus(Long studentId) {
//        return attendanceDayRepo.findByStudentIdAndDate(studentId, LocalDate.now());
//    }
//
//    // ─────────────────────────────────────────────────────────────────────────
//    // UPDATE MONTHLY SUMMARY
//    // (monthly summary tracks actual days per month for HOD/faculty reports)
//    // ─────────────────────────────────────────────────────────────────────────
//    @Transactional
//    public void updateMonthlySummary(Long studentId, Long collegeId, int month, int year) {
//        List<AttendanceDay> records = attendanceDayRepo
//                .findByStudentIdAndMonthAndYear(studentId, month, year);
//
//        Set<AttendanceStatus> nonWorking = Set.of(
//                AttendanceStatus.W, AttendanceStatus.H, AttendanceStatus.NONE);
//
//        int workingDays      = (int) records.stream()
//                .filter(r -> !nonWorking.contains(r.getStatus())).count();
//        int present          = (int) records.stream()
//                .filter(r -> r.getStatus() == AttendanceStatus.P).count();
//        int halfDay          = (int) records.stream()
//                .filter(r -> r.getStatus() == AttendanceStatus.HD).count();
//        int absent           = (int) records.stream()
//                .filter(r -> r.getStatus() == AttendanceStatus.A).count();
//        int error            = (int) records.stream()
//                .filter(r -> r.getStatus() == AttendanceStatus.E).count();
//        int holiday          = (int) records.stream()
//                .filter(r -> r.getStatus() == AttendanceStatus.H).count();
//        int weekend          = (int) records.stream()
//                .filter(r -> r.getStatus() == AttendanceStatus.W).count();
//        int effectivePresent = present + halfDay;
//
//        // Monthly % is still per actual working days of that month (for reports)
//        double pct = workingDays > 0
//                ? Math.round((effectivePresent * 100.0 / workingDays) * 100.0) / 100.0
//                : 0.0;
//
//        User    studentRef = userRepo.getReferenceById(studentId);
//        College collegeRef = collegeRepo.getReferenceById(collegeId);
//
//        AttendanceSummary summary = summaryRepo
//                .findByStudentIdAndMonthAndYear(studentId, month, year)
//                .orElseGet(AttendanceSummary::new);
//
//        summary.setStudent(studentRef);
//        summary.setCollege(collegeRef);
//        summary.setMonth(month);
//        summary.setYear(year);
//        summary.setTotalWorkingDays(workingDays);
//        summary.setDaysPresent(present);
//        summary.setDaysHalfDay(halfDay);
//        summary.setDaysAbsent(absent);
//        summary.setDaysError(error);
//        summary.setDaysHoliday(holiday);
//        summary.setDaysWeekend(weekend);
//        summary.setAttendancePercentage(pct);
//        summaryRepo.save(summary);
//    }
//
//    // ─────────────────────────────────────────────────────────────────────────
//    // END-OF-DAY CRON  (called by SchedulerService at 11:59 PM IST)
//    // ─────────────────────────────────────────────────────────────────────────
//    @Transactional
//    public void markEndOfDayAttendance() {
//        LocalDate today = LocalDate.now();
//
//        // Confirm E for checked-in-only records (no checkout)
//        List<AttendanceDay> incomplete = attendanceDayRepo
//                .findByDateAndStatusAndCheckOutTimeIsNull(today, AttendanceStatus.E);
//        for (AttendanceDay rec : incomplete) {
//            attendanceDayRepo.save(rec);
//            updateMonthlySummary(
//                    rec.getStudent().getId(), rec.getCollege().getId(),
//                    rec.getMonth(), rec.getYear());
//        }
//
//        // Mark Absent for students with no record today
//        List<College> colleges = collegeRepo.findByIsActiveTrueOrderByCreatedAtDesc();
//        for (College college : colleges) {
//            List<CollegeHoliday> holidays = holidayRepo.findByCollegeId(college.getId());
//            if (geofenceUtil.isWeekend(today) || geofenceUtil.isHoliday(today, holidays)) continue;
//
//            List<User> students = userRepo.findByCollegeIdAndRoleAndIsApprovedAndIsActive(
//                    college.getId(), Role.STUDENT, true, true);
//
//            for (User student : students) {
//                attendanceDayRepo.findByStudentIdAndDate(student.getId(), today).ifPresentOrElse(
//                        existing -> { /* already has a record — skip */ },
//                        () -> {
//                            AttendanceDay absentRec = new AttendanceDay();
//                            absentRec.setStudent(student);
//                            absentRec.setCollege(college);
//                            absentRec.setDate(today);
//                            absentRec.setStatus(AttendanceStatus.A);
//                            absentRec.setMonth(today.getMonthValue());
//                            absentRec.setYear(today.getYear());
//                            absentRec.setDayOfMonth(today.getDayOfMonth());
//                            attendanceDayRepo.save(absentRec);
//                            updateMonthlySummary(student.getId(), college.getId(),
//                                    today.getMonthValue(), today.getYear());
//                        }
//                );
//            }
//        }
//    }
//}



//fourth//
//
//package com.sbtetAttendance.sbtet.service;
//
//import com.sbtetAttendance.sbtet.model.*;
//import com.sbtetAttendance.sbtet.Repository.*;
//import com.sbtetAttendance.sbtet.util.GeofenceUtil;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.time.LocalDate;
//import java.time.LocalDateTime;
//import java.time.LocalTime;
//import java.time.temporal.ChronoUnit;
//import java.util.*;
//
//@Service
//public class AttendanceService {
//
//    private final AttendanceDayRepository attendanceDayRepo;
//    private final AttendanceSummaryRepository summaryRepo;
//    private final UserRepository userRepo;
//    private final CollegeRepository collegeRepo;
//    private final CollegeHolidayRepository holidayRepo;
//    private final GeofenceUtil geofenceUtil;
//
//    @Value("${attendance.gap.hours:6}")
//    private int gapHours;
//
//    /**
//     * FIX 1: Fixed semester denominator (default 90).
//     * This makes 1 present day = 1/90 = 1.11%, not 100%.
//     *
//     * Set in application.properties:
//     *   attendance.semester.working.days=90
//     */
//    @Value("${attendance.semester.working.days:90}")
//    private int semesterTotalWorkingDays;
//
//    public AttendanceService(AttendanceDayRepository attendanceDayRepo,
//                             AttendanceSummaryRepository summaryRepo,
//                             UserRepository userRepo,
//                             CollegeRepository collegeRepo,
//                             CollegeHolidayRepository holidayRepo,
//                             GeofenceUtil geofenceUtil) {
//        this.attendanceDayRepo = attendanceDayRepo;
//        this.summaryRepo       = summaryRepo;
//        this.userRepo          = userRepo;
//        this.collegeRepo       = collegeRepo;
//        this.holidayRepo       = holidayRepo;
//        this.geofenceUtil      = geofenceUtil;
//    }
//
//    // ================================================================== //
//    //  FIX 2: "Attendance Calculated" time
//    //
//    //  SBTET calculates attendance at 5:00 AM every day.
//    //  Return today at 05:00:00 AM — NOT the current time when
//    //  the student opens the page (which was the old bug).
//    //
//    //  Old (wrong): LocalDateTime.now()       → "4 Apr 2026, 12:32 PM"
//    //  New (fixed): today at 05:00 AM         → "4 Apr 2026, 05:00 AM"
//    // ================================================================== //
//    private LocalDateTime getAttendanceCalculatedTime() {
//        return LocalDateTime.of(LocalDate.now(), LocalTime.of(5, 0, 0));
//    }
//
//    // ------------------------------------------------------------------ //
//    //  Check-In
//    // ------------------------------------------------------------------ //
//    @Transactional
//    public Map<String, Object> checkIn(User student, double latitude, double longitude, boolean faceVerified) {
//        if (!faceVerified)
//            throw new IllegalArgumentException("Face verification failed. Please try again.");
//
//        College college = student.getCollege();
//        if (college == null)
//            throw new IllegalArgumentException("College not found for this student.");
//
//        if (!geofenceUtil.isWithinCollege(latitude, longitude, college)) {
//            int dist = geofenceUtil.getDistanceFromCollege(latitude, longitude, college);
//            throw new IllegalArgumentException(
//                    "You are outside the college premises. You are " + dist + "m away. "
//                            + "Please come to college to mark attendance.");
//        }
//
//        LocalDate today = LocalDate.now();
//        List<CollegeHoliday> holidays = holidayRepo.findByCollegeId(college.getId());
//
//        if (geofenceUtil.isWeekend(today))
//            throw new IllegalArgumentException("Today is a weekend. No attendance required.");
//        if (geofenceUtil.isHoliday(today, holidays))
//            throw new IllegalArgumentException("Today is a holiday. No attendance required.");
//
//        Optional<AttendanceDay> existing = attendanceDayRepo.findByStudentIdAndDate(student.getId(), today);
//        if (existing.isPresent()) {
//            AttendanceDay rec = existing.get();
//            if (rec.getCheckInTime() != null) {
//                if (rec.getCheckOutTime() != null)
//                    throw new IllegalArgumentException("Attendance already marked for today.");
//                throw new IllegalArgumentException(
//                        "Already checked in. Check out after " + gapHours + " hours.");
//            }
//        }
//
//        LocalDateTime now = LocalDateTime.now();
//        AttendanceDay record = existing.orElseGet(AttendanceDay::new);
//        record.setStudent(student);
//        record.setCollege(college);
//        record.setDate(today);
//        record.setMonth(today.getMonthValue());
//        record.setYear(today.getYear());
//        record.setDayOfMonth(today.getDayOfMonth());
//        record.setCheckInTime(now);
//        record.setCheckInLat(latitude);
//        record.setCheckInLon(longitude);
//        record.setCheckInFaceVerified(true);
//        record.setStatus(AttendanceStatus.E);
//        attendanceDayRepo.save(record);
//
//        return Map.of(
//                "message",     "Check-in successful! Remember to check out after " + gapHours + " hours.",
//                "checkInTime", now
//        );
//    }
//
//    // ------------------------------------------------------------------ //
//    //  Check-Out
//    // ------------------------------------------------------------------ //
//    @Transactional
//    public Map<String, Object> checkOut(User student, double latitude, double longitude, boolean faceVerified) {
//        if (!faceVerified)
//            throw new IllegalArgumentException("Face verification failed. Please try again.");
//
//        College college = student.getCollege();
//        if (!geofenceUtil.isWithinCollege(latitude, longitude, college)) {
//            int dist = geofenceUtil.getDistanceFromCollege(latitude, longitude, college);
//            throw new IllegalArgumentException(
//                    "You are outside the college premises (" + dist + "m away). "
//                            + "Please be in college to check out.");
//        }
//
//        LocalDate today = LocalDate.now();
//        AttendanceDay record = attendanceDayRepo.findByStudentIdAndDate(student.getId(), today)
//                .orElseThrow(() -> new IllegalArgumentException(
//                        "No check-in found for today. Please check in first."));
//
//        if (record.getCheckInTime() == null)
//            throw new IllegalArgumentException("No check-in found for today.");
//        if (record.getCheckOutTime() != null)
//            throw new IllegalArgumentException("Already checked out today.");
//
//        LocalDateTime now   = LocalDateTime.now();
//        long minutes        = ChronoUnit.MINUTES.between(record.getCheckInTime(), now);
//        double hours        = minutes / 60.0;
//        AttendanceStatus status = hours >= gapHours ? AttendanceStatus.P : AttendanceStatus.HD;
//
//        record.setCheckOutTime(now);
//        record.setCheckOutLat(latitude);
//        record.setCheckOutLon(longitude);
//        record.setCheckOutFaceVerified(true);
//        record.setGapMinutes((int) minutes);
//        record.setStatus(status);
//        attendanceDayRepo.save(record);
//
//        updateMonthlySummary(student.getId(), college.getId(), record.getMonth(), record.getYear());
//
//        String msg = status == AttendanceStatus.P
//                ? "Check-out successful! Full attendance marked for today."
//                : "Check-out recorded as Half-Day. Gap was only "
//                + (int) hours + "h " + (minutes % 60) + "m (need " + gapHours + "h for full day).";
//
//        return Map.of(
//                "message",      msg,
//                "status",       status.name(),
//                "gapMinutes",   minutes,
//                "checkOutTime", now
//        );
//    }
//
//    // ------------------------------------------------------------------ //
//    //  Student Summary — SBTET portal style
//    //
//    //  FIX 1: denominator = semesterTotalWorkingDays (90), NOT count of DB records
//    //         Day 1 present → 1/90 * 100 = 1.11%  ✅
//    //         Day 2 present → 2/90 * 100 = 2.22%  ✅
//    //         Old wrong:       1/1 * 100 = 100%    ❌
//    //
//    //  FIX 2: lastCalculated = today at 05:00 AM (not current time)
//    //
//    //  SBTET formula:
//    //    effectivePresent = presentDays + halfDays  (HP = same as P)
//    //    percentage       = effectivePresent / 90 * 100
//    //
//    //  Detention rules:
//    //    >= 75%  → ELIGIBLE
//    //    >= 65%  → CONDONATION
//    //    <  65%  → DETAINED
//    // ------------------------------------------------------------------ //
//    public Map<String, Object> getStudentSummary(Long studentId) {
//        User student = userRepo.findById(studentId)
//                .orElseThrow(() -> new NoSuchElementException("Student not found."));
//
//        List<AttendanceDay> records = attendanceDayRepo.findByStudentIdOrderByDateAsc(studentId);
//
//        // ── Build monthly grid ────────────────────────────────────────────
//        // HD is shown as "HP" (Half Present) on the SBTET official portal
//        Map<String, Map<String, Object>> monthlyMap = new LinkedHashMap<>();
//        for (AttendanceDay r : records) {
//            String key = r.getYear() + "-" + String.format("%02d", r.getMonth());
//            monthlyMap.computeIfAbsent(key, k -> {
//                Map<String, Object> m = new LinkedHashMap<>();
//                m.put("year",  r.getYear());
//                m.put("month", r.getMonth());
//                m.put("days",  new TreeMap<Integer, String>());
//                return m;
//            });
//            @SuppressWarnings("unchecked")
//            Map<Integer, String> days = (Map<Integer, String>) monthlyMap.get(key).get("days");
//            // Show HD as HP to match SBTET portal
//            days.put(r.getDayOfMonth(),
//                    r.getStatus() == AttendanceStatus.HD ? "HP" : r.getStatus().name());
//        }
//
//        // Sort months chronologically
//        List<Map<String, Object>> monthlyData = monthlyMap.entrySet().stream()
//                .sorted(Map.Entry.comparingByKey())
//                .map(Map.Entry::getValue)
//                .toList();
//
//        // ── Count actual days from records ────────────────────────────────
//        long presentDays      = records.stream().filter(r -> r.getStatus() == AttendanceStatus.P).count();
//        long halfDays         = records.stream().filter(r -> r.getStatus() == AttendanceStatus.HD).count();
//        long absentDays       = records.stream().filter(r -> r.getStatus() == AttendanceStatus.A).count();
//        long errorDays        = records.stream().filter(r -> r.getStatus() == AttendanceStatus.E).count();
//
//        // HP counts same as P in SBTET numerator
//        long effectivePresent = presentDays + halfDays;
//
//        // ── FIX 1: Use fixed 90 as denominator, NOT records count ─────────
//        int totalWorkingDays = semesterTotalWorkingDays; // always 90 from properties
//
//        // percentage = effectivePresent / 90 * 100
//        double percentage = totalWorkingDays > 0
//                ? Math.round((effectivePresent * 100.0 / totalWorkingDays) * 100.0) / 100.0
//                : 0.0;
//
//        // ── Detention status ──────────────────────────────────────────────
//        String detentionStatus =
//                percentage >= 75.0 ? "ELIGIBLE" :
//                        percentage >= 65.0 ? "CONDONATION" :
//                                "DETAINED";
//
//        // ── Days needed to reach 75% ──────────────────────────────────────
//        // Need: effectivePresent + x >= 0.75 * 90  →  x = ceil(67.5) - effectivePresent
//        long daysNeededFor75 = percentage >= 75.0 ? 0
//                : Math.max(0, (long) Math.ceil(0.75 * totalWorkingDays) - effectivePresent);
//
//        // ── Per-day impact: 1/90 * 100 = 1.11% ───────────────────────────
//        double perDayImpact = totalWorkingDays > 0
//                ? Math.round((100.0 / totalWorkingDays) * 100.0) / 100.0
//                : 0.0;
//
//        // ── FIX 2: lastCalculated = today at 05:00 AM (not current time) ──
//        LocalDateTime lastCalculated = getAttendanceCalculatedTime();
//
//        // ── Build response ────────────────────────────────────────────────
//        Map<String, Object> studentMap = new LinkedHashMap<>();
//        studentMap.put("name",        student.getName());
//        studentMap.put("pinNumber",   Objects.toString(student.getPinNumber(),   ""));
//        studentMap.put("attendeeId",  Objects.toString(student.getAttendeeId(),  ""));
//        studentMap.put("branch",      Objects.toString(student.getBranch(),      ""));
//        studentMap.put("semester",    Objects.toString(student.getSemester(),     ""));
//        studentMap.put("collegeCode", Objects.toString(student.getCollegeCode(), ""));
//
//        Map<String, Object> statsMap = new LinkedHashMap<>();
//        statsMap.put("workingDays",       totalWorkingDays);   // always 90
//        statsMap.put("presentDays",       presentDays);
//        statsMap.put("halfDays",          halfDays);
//        statsMap.put("effectivePresent",  effectivePresent);   // P + HP
//        statsMap.put("absentDays",        absentDays);
//        statsMap.put("errorDays",         errorDays);
//        statsMap.put("percentage",        percentage);         // 1.11 on day 1
//        statsMap.put("detentionStatus",   detentionStatus);
//        statsMap.put("daysNeededFor75",   daysNeededFor75);
//        statsMap.put("perDayImpact",      perDayImpact);       // 1.11
//        statsMap.put("lastCalculated",    lastCalculated);     // today 05:00 AM
//
//        return Map.of(
//                "student",     studentMap,
//                "stats",       statsMap,
//                "monthlyData", monthlyData,
//                "records",     records
//        );
//    }
//
//    // ------------------------------------------------------------------ //
//    //  Today Status
//    // ------------------------------------------------------------------ //
//    public Optional<AttendanceDay> getTodayStatus(Long studentId) {
//        return attendanceDayRepo.findByStudentIdAndDate(studentId, LocalDate.now());
//    }
//
//    // ------------------------------------------------------------------ //
//    //  Monthly Summary (used after checkout + end-of-day cron)
//    //  Note: monthly % uses actual days of that month for HOD/faculty reports
//    // ------------------------------------------------------------------ //
//    @Transactional
//    public void updateMonthlySummary(Long studentId, Long collegeId, int month, int year) {
//        List<AttendanceDay> records = attendanceDayRepo.findByStudentIdAndMonthAndYear(studentId, month, year);
//        Set<AttendanceStatus> nonWorking = Set.of(AttendanceStatus.W, AttendanceStatus.H, AttendanceStatus.NONE);
//
//        int workingDays      = (int) records.stream().filter(r -> !nonWorking.contains(r.getStatus())).count();
//        int present          = (int) records.stream().filter(r -> r.getStatus() == AttendanceStatus.P).count();
//        int halfDay          = (int) records.stream().filter(r -> r.getStatus() == AttendanceStatus.HD).count();
//        int absent           = (int) records.stream().filter(r -> r.getStatus() == AttendanceStatus.A).count();
//        int error            = (int) records.stream().filter(r -> r.getStatus() == AttendanceStatus.E).count();
//        int holiday          = (int) records.stream().filter(r -> r.getStatus() == AttendanceStatus.H).count();
//        int weekend          = (int) records.stream().filter(r -> r.getStatus() == AttendanceStatus.W).count();
//        int effectivePresent = present + halfDay;
//
//        // Semester-level % (against fixed 90)
//        double pct = semesterTotalWorkingDays > 0
//                ? Math.round((effectivePresent * 100.0 / semesterTotalWorkingDays) * 100.0) / 100.0
//                : 0.0;
//
//        User    studentRef = userRepo.getReferenceById(studentId);
//        College collegeRef = collegeRepo.getReferenceById(collegeId);
//
//        AttendanceSummary summary = summaryRepo
//                .findByStudentIdAndMonthAndYear(studentId, month, year)
//                .orElseGet(AttendanceSummary::new);
//
//        summary.setStudent(studentRef);
//        summary.setCollege(collegeRef);
//        summary.setMonth(month);
//        summary.setYear(year);
//        summary.setTotalWorkingDays(workingDays);
//        summary.setDaysPresent(present);
//        summary.setDaysHalfDay(halfDay);
//        summary.setDaysAbsent(absent);
//        summary.setDaysError(error);
//        summary.setDaysHoliday(holiday);
//        summary.setDaysWeekend(weekend);
//        summary.setAttendancePercentage(pct);
//        summaryRepo.save(summary);
//    }
//
//    // ------------------------------------------------------------------ //
//    //  End-of-Day Cron (called by SchedulerService at 11:59 PM IST)
//    // ------------------------------------------------------------------ //
//    @Transactional
//    public void markEndOfDayAttendance() {
//        LocalDate today = LocalDate.now();
//
//        // 1. Confirm E for checked-in-only records (no checkout)
//        List<AttendanceDay> incomplete = attendanceDayRepo
//                .findByDateAndStatusAndCheckOutTimeIsNull(today, AttendanceStatus.E);
//        for (AttendanceDay rec : incomplete) {
//            attendanceDayRepo.save(rec);
//            updateMonthlySummary(
//                    rec.getStudent().getId(),
//                    rec.getCollege().getId(),
//                    rec.getMonth(), rec.getYear());
//        }
//
//        // 2. Mark Absent for students with no record today
//        List<College> colleges = collegeRepo.findByIsActiveTrueOrderByCreatedAtDesc();
//        for (College college : colleges) {
//            List<CollegeHoliday> holidays = holidayRepo.findByCollegeId(college.getId());
//            if (geofenceUtil.isWeekend(today) || geofenceUtil.isHoliday(today, holidays)) continue;
//
//            List<User> students = userRepo.findByCollegeIdAndRoleAndIsApprovedAndIsActive(
//                    college.getId(), Role.STUDENT, true, true);
//
//            for (User student : students) {
//                boolean hasRecord = attendanceDayRepo
//                        .findByStudentIdAndDate(student.getId(), today).isPresent();
//                if (!hasRecord) {
//                    AttendanceDay absent = new AttendanceDay();
//                    absent.setStudent(student);
//                    absent.setCollege(college);
//                    absent.setDate(today);
//                    absent.setStatus(AttendanceStatus.A);
//                    absent.setMonth(today.getMonthValue());
//                    absent.setYear(today.getYear());
//                    absent.setDayOfMonth(today.getDayOfMonth());
//                    attendanceDayRepo.save(absent);
//                    updateMonthlySummary(student.getId(), college.getId(),
//                            today.getMonthValue(), today.getYear());
//                }
//            }
//        }
//    }
//}



//fifth//

package com.sbtetAttendance.sbtet.service;

import com.sbtetAttendance.sbtet.model.*;
import com.sbtetAttendance.sbtet.Repository.*;
import com.sbtetAttendance.sbtet.util.GeofenceUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
public class AttendanceService {

    private final AttendanceDayRepository attendanceDayRepo;
    private final AttendanceSummaryRepository summaryRepo;
    private final UserRepository userRepo;
    private final CollegeRepository collegeRepo;
    private final CollegeHolidayRepository holidayRepo;
    private final GeofenceUtil geofenceUtil;

    @Value("${attendance.gap.hours:6}")
    private int gapHours;

    /**
     * Fixed semester total working days (denominator).
     * 1 present day = 1/90 * 100 = 1.11%
     * Set in application.properties: attendance.semester.working.days=90
     */
    @Value("${attendance.semester.working.days:90}")
    private int semesterTotalWorkingDays;

    public AttendanceService(AttendanceDayRepository attendanceDayRepo,
                             AttendanceSummaryRepository summaryRepo,
                             UserRepository userRepo,
                             CollegeRepository collegeRepo,
                             CollegeHolidayRepository holidayRepo,
                             GeofenceUtil geofenceUtil) {
        this.attendanceDayRepo = attendanceDayRepo;
        this.summaryRepo       = summaryRepo;
        this.userRepo          = userRepo;
        this.collegeRepo       = collegeRepo;
        this.holidayRepo       = holidayRepo;
        this.geofenceUtil      = geofenceUtil;
    }

    // ------------------------------------------------------------------ //
    //  Check-In
    // ------------------------------------------------------------------ //
    @Transactional
    public Map<String, Object> checkIn(User student, double latitude, double longitude, boolean faceVerified) {
        if (!faceVerified)
            throw new IllegalArgumentException("Face verification failed. Please try again.");

        College college = student.getCollege();
        if (college == null)
            throw new IllegalArgumentException("College not found for this student.");

        if (!geofenceUtil.isWithinCollege(latitude, longitude, college)) {
            int dist = geofenceUtil.getDistanceFromCollege(latitude, longitude, college);
            throw new IllegalArgumentException(
                    "You are outside the college premises. You are " + dist + "m away. " +
                            "Please come to college to mark attendance.");
        }

        LocalDate today = LocalDate.now();
        List<CollegeHoliday> holidays = holidayRepo.findByCollegeId(college.getId());

        if (geofenceUtil.isWeekend(today))
            throw new IllegalArgumentException("Today is a weekend. No attendance required.");
        if (geofenceUtil.isHoliday(today, holidays))
            throw new IllegalArgumentException("Today is a holiday. No attendance required.");

        Optional<AttendanceDay> existing = attendanceDayRepo.findByStudentIdAndDate(student.getId(), today);
        if (existing.isPresent()) {
            AttendanceDay rec = existing.get();
            if (rec.getCheckInTime() != null) {
                if (rec.getCheckOutTime() != null)
                    throw new IllegalArgumentException("Attendance already marked for today.");
                throw new IllegalArgumentException(
                        "Already checked in. Check out after " + gapHours + " hours.");
            }
        }

        LocalDateTime now = LocalDateTime.now();
        AttendanceDay record = existing.orElseGet(AttendanceDay::new);
        record.setStudent(student);
        record.setCollege(college);
        record.setDate(today);
        record.setMonth(today.getMonthValue());
        record.setYear(today.getYear());
        record.setDayOfMonth(today.getDayOfMonth());
        record.setCheckInTime(now);
        record.setCheckInLat(latitude);
        record.setCheckInLon(longitude);
        record.setCheckInFaceVerified(true);
        record.setStatus(AttendanceStatus.E);
        attendanceDayRepo.save(record);

        return Map.of(
                "message",     "Check-in successful! Remember to check out after " + gapHours + " hours.",
                "checkInTime", now
        );
    }

    // ------------------------------------------------------------------ //
    //  Check-Out
    // ------------------------------------------------------------------ //
    @Transactional
    public Map<String, Object> checkOut(User student, double latitude, double longitude, boolean faceVerified) {
        if (!faceVerified)
            throw new IllegalArgumentException("Face verification failed. Please try again.");

        College college = student.getCollege();
        if (!geofenceUtil.isWithinCollege(latitude, longitude, college)) {
            int dist = geofenceUtil.getDistanceFromCollege(latitude, longitude, college);
            throw new IllegalArgumentException(
                    "You are outside the college premises (" + dist + "m away). " +
                            "Please be in college to check out.");
        }

        LocalDate today = LocalDate.now();
        AttendanceDay record = attendanceDayRepo.findByStudentIdAndDate(student.getId(), today)
                .orElseThrow(() -> new IllegalArgumentException(
                        "No check-in found for today. Please check in first."));

        if (record.getCheckInTime() == null)
            throw new IllegalArgumentException("No check-in found for today.");
        if (record.getCheckOutTime() != null)
            throw new IllegalArgumentException("Already checked out today.");

        LocalDateTime now   = LocalDateTime.now();
        long minutes        = ChronoUnit.MINUTES.between(record.getCheckInTime(), now);
        double hours        = minutes / 60.0;
        AttendanceStatus status = hours >= gapHours ? AttendanceStatus.P : AttendanceStatus.HD;

        record.setCheckOutTime(now);
        record.setCheckOutLat(latitude);
        record.setCheckOutLon(longitude);
        record.setCheckOutFaceVerified(true);
        record.setGapMinutes((int) minutes);
        record.setStatus(status);
        attendanceDayRepo.save(record);

        updateMonthlySummary(student.getId(), college.getId(), record.getMonth(), record.getYear());

        String msg = status == AttendanceStatus.P
                ? "Check-out successful! Full attendance marked for today."
                : "Check-out recorded as Half-Day. Gap was only "
                + (int) hours + "h " + (minutes % 60) + "m (need " + gapHours + "h for full day).";

        return Map.of(
                "message",      msg,
                "status",       status.name(),
                "gapMinutes",   minutes,
                "checkOutTime", now
        );
    }

    // ------------------------------------------------------------------ //
    //  Student Summary — SBTET portal style
    //
    //  BUG FIX 1 — "Today shows as -" in the grid:
    //    Jackson serialises Map<Integer,String> keys as JSON strings → "5":"P"
    //    But the old code used TreeMap<Integer,String> which Jackson may send
    //    as integer keys in some versions but string keys in others.
    //    SOLUTION: explicitly use Map<String,String> with String.valueOf(day)
    //    so the key is ALWAYS the string "5", "12", etc.
    //    Frontend then reads m.days?.["5"] or m.days?.[String(d)] — always works.
    //
    //  BUG FIX 2 — "266 days needed" (wrong daysNeededFor75):
    //    Old formula used DB record count as workingDays denominator.
    //    With only 1 record: 1 day present / 1 working day = 100%,
    //    but daysNeededFor75 = ceil(0.75*1) - 1 = 0, or with wrong math = 266.
    //    SOLUTION: always use semesterTotalWorkingDays (90) as denominator.
    //    daysNeededFor75 = ceil(0.75 * 90) - effectivePresent = 68 - 1 = 67
    //
    //  BUG FIX 3 — lastCalculated shows current time:
    //    SOLUTION: always return today at 05:00 AM.
    //
    //  SBTET formula:
    //    effectivePresent = presentDays + halfDays  (HP counts same as P)
    //    percentage       = effectivePresent / 90 * 100
    //    1 present day    = 1/90 * 100 = 1.11%
    // ------------------------------------------------------------------ //
    public Map<String, Object> getStudentSummary(Long studentId) {
        User student = userRepo.findById(studentId)
                .orElseThrow(() -> new NoSuchElementException("Student not found."));

        List<AttendanceDay> records = attendanceDayRepo.findByStudentIdOrderByDateAsc(studentId);

        // ── Build monthly grid ──────────────────────────────────────────────
        // FIX 1: Use Map<String, String> so Jackson always produces string keys
        //        "5":"P"  NOT  5:"P"
        //        Frontend reads: m.days[String(d)] → always matches
        Map<String, Map<String, Object>> monthlyMap = new LinkedHashMap<>();

        for (AttendanceDay r : records) {
            // Key: "2026-04" — zero-padded month for correct sort
            String key = r.getYear() + "-" + String.format("%02d", r.getMonth());

            monthlyMap.computeIfAbsent(key, k -> {
                Map<String, Object> m = new LinkedHashMap<>();
                m.put("year",  r.getYear());
                m.put("month", r.getMonth());
                // String keys map — Jackson will produce {"5":"P", "12":"A"}
                m.put("days",  new LinkedHashMap<String, String>());
                return m;
            });

            @SuppressWarnings("unchecked")
            Map<String, String> days = (Map<String, String>) monthlyMap.get(key).get("days");

            // String.valueOf(dayOfMonth) → "5", "12", "31"
            // HD shown as HP to match SBTET portal label
            String statusLabel = r.getStatus() == AttendanceStatus.HD
                    ? "HP"
                    : r.getStatus().name();

            days.put(String.valueOf(r.getDayOfMonth()), statusLabel);
        }

        // Sort months chronologically by "2026-04" string key
        List<Map<String, Object>> monthlyData = monthlyMap.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(Map.Entry::getValue)
                .toList();

        // ── Count actual days ───────────────────────────────────────────────
        long presentDays      = records.stream().filter(r -> r.getStatus() == AttendanceStatus.P).count();
        long halfDays         = records.stream().filter(r -> r.getStatus() == AttendanceStatus.HD).count();
        long absentDays       = records.stream().filter(r -> r.getStatus() == AttendanceStatus.A).count();
        long errorDays        = records.stream().filter(r -> r.getStatus() == AttendanceStatus.E).count();

        // HP counts same as P in SBTET numerator
        long effectivePresent = presentDays + halfDays;

        // ── FIX 2: Fixed 90-day denominator ────────────────────────────────
        //   Day 1 present: 1/90*100 = 1.11%   ✅
        //   Old wrong:     1/1*100  = 100%     ❌
        int totalWorkingDays = semesterTotalWorkingDays; // 90

        double percentage = totalWorkingDays > 0
                ? Math.round((effectivePresent * 100.0 / totalWorkingDays) * 100.0) / 100.0
                : 0.0;

        // ── Detention status ────────────────────────────────────────────────
        String detentionStatus =
                percentage >= 75.0 ? "ELIGIBLE" :
                        percentage >= 65.0 ? "CONDONATION" :
                                "DETAINED";

        // ── FIX 2 continued: correct daysNeededFor75 ───────────────────────
        //   Need: effectivePresent + x >= 0.75 * 90 = 67.5 → x = ceil(67.5) - effectivePresent
        //   Example: 1 present day → need ceil(67.5) - 1 = 68 - 1 = 67 more days
        //   Old wrong: gave 266 because denominator was 1 record not 90
        long daysNeededFor75 = percentage >= 75.0
                ? 0
                : Math.max(0, (long) Math.ceil(0.75 * totalWorkingDays) - effectivePresent);

        // Per-day impact: 1/90*100 = 1.11%
        double perDayImpact = totalWorkingDays > 0
                ? Math.round((100.0 / totalWorkingDays) * 100.0) / 100.0
                : 0.0;

        // ── FIX 3: lastCalculated = today at 05:00 AM, not current time ────
        LocalDateTime lastCalculated = LocalDateTime.of(LocalDate.now(), LocalTime.of(5, 0, 0));

        // ── Build response ──────────────────────────────────────────────────
        Map<String, Object> studentMap = new LinkedHashMap<>();
        studentMap.put("name",        student.getName());
        studentMap.put("pinNumber",   Objects.toString(student.getPinNumber(),   ""));
        studentMap.put("attendeeId",  Objects.toString(student.getAttendeeId(),  ""));
        studentMap.put("branch",      Objects.toString(student.getBranch(),      ""));
        studentMap.put("semester",    Objects.toString(student.getSemester(),     ""));
        studentMap.put("collegeCode", Objects.toString(student.getCollegeCode(), ""));

        Map<String, Object> statsMap = new LinkedHashMap<>();
        statsMap.put("workingDays",      totalWorkingDays);   // always 90
        statsMap.put("presentDays",      presentDays);        // pure P count
        statsMap.put("halfDays",         halfDays);           // HP count
        statsMap.put("effectivePresent", effectivePresent);   // P + HP
        statsMap.put("absentDays",       absentDays);
        statsMap.put("errorDays",        errorDays);
        statsMap.put("percentage",       percentage);         // 1.11 on day 1
        statsMap.put("detentionStatus",  detentionStatus);
        statsMap.put("daysNeededFor75",  daysNeededFor75);    // 67 on day 1 (not 266)
        statsMap.put("perDayImpact",     perDayImpact);       // 1.11
        statsMap.put("lastCalculated",   lastCalculated);     // today 05:00 AM

        return Map.of(
                "student",     studentMap,
                "stats",       statsMap,
                "monthlyData", monthlyData,
                "records",     records
        );
    }

    // ------------------------------------------------------------------ //
    //  Today Status
    // ------------------------------------------------------------------ //
    public Optional<AttendanceDay> getTodayStatus(Long studentId) {
        return attendanceDayRepo.findByStudentIdAndDate(studentId, LocalDate.now());
    }

    // ------------------------------------------------------------------ //
    //  Monthly Summary
    // ------------------------------------------------------------------ //
    @Transactional
    public void updateMonthlySummary(Long studentId, Long collegeId, int month, int year) {
        List<AttendanceDay> records = attendanceDayRepo
                .findByStudentIdAndMonthAndYear(studentId, month, year);

        Set<AttendanceStatus> nonWorking = Set.of(
                AttendanceStatus.W, AttendanceStatus.H, AttendanceStatus.NONE);

        int workingDays      = (int) records.stream().filter(r -> !nonWorking.contains(r.getStatus())).count();
        int present          = (int) records.stream().filter(r -> r.getStatus() == AttendanceStatus.P).count();
        int halfDay          = (int) records.stream().filter(r -> r.getStatus() == AttendanceStatus.HD).count();
        int absent           = (int) records.stream().filter(r -> r.getStatus() == AttendanceStatus.A).count();
        int error            = (int) records.stream().filter(r -> r.getStatus() == AttendanceStatus.E).count();
        int holiday          = (int) records.stream().filter(r -> r.getStatus() == AttendanceStatus.H).count();
        int weekend          = (int) records.stream().filter(r -> r.getStatus() == AttendanceStatus.W).count();
        int effectivePresent = present + halfDay;

        // Semester-level % against fixed 90
        double pct = semesterTotalWorkingDays > 0
                ? Math.round((effectivePresent * 100.0 / semesterTotalWorkingDays) * 100.0) / 100.0
                : 0.0;

        User    studentRef = userRepo.getReferenceById(studentId);
        College collegeRef = collegeRepo.getReferenceById(collegeId);

        AttendanceSummary summary = summaryRepo
                .findByStudentIdAndMonthAndYear(studentId, month, year)
                .orElseGet(AttendanceSummary::new);

        summary.setStudent(studentRef);
        summary.setCollege(collegeRef);
        summary.setMonth(month);
        summary.setYear(year);
        summary.setTotalWorkingDays(workingDays);
        summary.setDaysPresent(present);
        summary.setDaysHalfDay(halfDay);
        summary.setDaysAbsent(absent);
        summary.setDaysError(error);
        summary.setDaysHoliday(holiday);
        summary.setDaysWeekend(weekend);
        summary.setAttendancePercentage(pct);
        summaryRepo.save(summary);
    }

    // ------------------------------------------------------------------ //
    //  End-of-Day Cron
    // ------------------------------------------------------------------ //
    @Transactional
    public void markEndOfDayAttendance() {
        LocalDate today = LocalDate.now();

        List<AttendanceDay> incomplete = attendanceDayRepo
                .findByDateAndStatusAndCheckOutTimeIsNull(today, AttendanceStatus.E);
        for (AttendanceDay rec : incomplete) {
            attendanceDayRepo.save(rec);
            updateMonthlySummary(
                    rec.getStudent().getId(), rec.getCollege().getId(),
                    rec.getMonth(), rec.getYear());
        }

        List<College> colleges = collegeRepo.findByIsActiveTrueOrderByCreatedAtDesc();
        for (College college : colleges) {
            List<CollegeHoliday> holidays = holidayRepo.findByCollegeId(college.getId());
            if (geofenceUtil.isWeekend(today) || geofenceUtil.isHoliday(today, holidays)) continue;

            List<User> students = userRepo.findByCollegeIdAndRoleAndIsApprovedAndIsActive(
                    college.getId(), Role.STUDENT, true, true);

            for (User student : students) {
                boolean hasRecord = attendanceDayRepo
                        .findByStudentIdAndDate(student.getId(), today).isPresent();
                if (!hasRecord) {
                    AttendanceDay absent = new AttendanceDay();
                    absent.setStudent(student);
                    absent.setCollege(college);
                    absent.setDate(today);
                    absent.setStatus(AttendanceStatus.A);
                    absent.setMonth(today.getMonthValue());
                    absent.setYear(today.getYear());
                    absent.setDayOfMonth(today.getDayOfMonth());
                    attendanceDayRepo.save(absent);
                    updateMonthlySummary(student.getId(), college.getId(),
                            today.getMonthValue(), today.getYear());
                }
            }
        }
    }
}