package com.sbtetAttendance.sbtet.service;



import com.sbtetAttendance.sbtet.model.*;
import com.sbtetAttendance.sbtet.Repository.*;
import com.sbtetAttendance.sbtet.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import java.util.*;

@Service
public class AdminService {

    @Autowired private CollegeRepository collegeRepo;
    @Autowired private UserRepository userRepo;
    @Autowired private CollegeHolidayRepository holidayRepo;
    @Autowired private AttendanceDayRepository attendanceDayRepo;
   @Autowired private AttendanceService attendanceService;
    @Transactional
    public College registerCollege(Map<String, Object> body, User registeredBy) {
        String code = ((String) body.get("collegeCode")).toUpperCase();
        if (collegeRepo.existsByCollegeCode(code))
            throw new IllegalArgumentException("College code already registered.");

        College college = new College();
        college.setCollegeName((String) body.get("name"));
        college.setCollegeCode(code);
        college.setAddress((String) body.get("address"));
        college.setDistrict((String) body.get("district"));
        college.setPrincipal((String) body.get("principal"));
        college.setPhone((String) body.get("phone"));
        college.setEmail((String) body.get("email"));
        college.setLatitude(Double.parseDouble(body.get("latitude").toString()));
        college.setLongitude(Double.parseDouble(body.get("longitude").toString()));
        Object radius = body.get("radius");
        college.setGeofenceRadius(radius != null ? Double.parseDouble(radius.toString()) : 200.0);
//        college.setBranchesJson((String) body.getOrDefault("branches", "[]"));
        Object branchesObj = body.get("branches");

        if (branchesObj instanceof List<?>) {
            List<?> branchesList = (List<?>) branchesObj;

            try {
                String branchesJson = new com.fasterxml.jackson.databind.ObjectMapper()
                        .writeValueAsString(branchesList);

                college.setBranchesJson(branchesJson);
            } catch (Exception e) {
                throw new RuntimeException("Error converting branches to JSON", e);
            }

        } else {
            college.setBranchesJson("[]");
        }
        college.setRegisteredBy(registeredBy);
        return collegeRepo.save(college);
    }

    public List<Map<String, Object>> getAllColleges() {
        return collegeRepo.findAll().stream().map(c -> {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("id", c.getId());
            map.put("name", c.getCollegeName());
            map.put("collegeCode", c.getCollegeCode());
            map.put("address", c.getAddress());
            map.put("district", c.getDistrict());
            map.put("principal", c.getPrincipal());
            map.put("phone", c.getPhone());
            map.put("email", c.getEmail());
            map.put("latitude", c.getLatitude());
            map.put("longitude", c.getLongitude());
            map.put("geofenceRadius", c.getGeofenceRadius());
            map.put("isActive", c.getActive());
            map.put("createdAt", c.getUpdatedAt());
            map.put("studentCount", userRepo.countByRoleAndIsActiveTrue(Role.STUDENT));
            map.put("facultyCount", userRepo.countByRoleAndIsActiveTrue(Role.FACULTY));
            map.put("hodCount", userRepo.countByRoleAndIsActiveTrue(Role.HOD));
            return map;
        }).toList();
    }

    public Map<String, Object> getDashboardStats() {
        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("totalColleges", collegeRepo.findByIsActiveTrueOrderByCreatedAtDesc().size());
        stats.put("totalStudents", userRepo.countByRoleAndIsActiveTrue(Role.STUDENT));
        stats.put("totalFaculty", userRepo.countByRoleAndIsActiveTrue(Role.FACULTY));
        stats.put("totalHods", userRepo.countByRoleAndIsActiveTrue(Role.HOD));
        stats.put("pendingApprovals", userRepo.countByIsApprovedFalseAndRoleNot(Role.ADMIN));
        LocalDate today = LocalDate.now();
        stats.put("todayPresent", attendanceDayRepo.countByDateAndStatus(today, AttendanceStatus.P));
        stats.put("todayAbsent", attendanceDayRepo.countByDateAndStatus(today, AttendanceStatus.A));
        stats.put("recentColleges", collegeRepo.findByIsActiveTrueOrderByCreatedAtDesc()
                .stream().limit(5).toList());
        return stats;
    }

    //@Transactional
    //public void updateUserApproval(Long userId, boolean isApproved) {
      //  User user = userRepo.findById(userId).orElseThrow(() -> new NoSuchElementException("User not found."));
     //   user.setApproved(isApproved);
       // userRepo.save(user);
  //  }


    // Find this method and add backfill after saving
public void updateUserApproval(Long userId, Boolean isApproved) {
    User user = userRepo.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found."));
    user.setApproved(isApproved);
    userRepo.save(user);
    
    // NEW: If student just got approved, backfill their past attendance
    if (isApproved && user.getRole() == Role.STUDENT) {
        attendanceService.backfillStudentAttendance(user);
    }
}

    
    public List<User> getPendingApprovals() {
        return userRepo.findByIsApprovedFalseAndRoleNot(Role.ADMIN);
    }

    public List<User> getAllUsers(String role, String collegeCode, Boolean isApproved) {
        return userRepo.findAll().stream()
                .filter(u -> u.getRole() != Role.ADMIN)
                .filter(u -> role == null || u.getRole().name().equalsIgnoreCase(role))
                .filter(u -> collegeCode == null || collegeCode.equalsIgnoreCase(u.getCollegeCode()))
                .filter(u -> isApproved == null || isApproved.equals(u.getApproved()))
                .toList();
    }

    @Transactional
    public void addHoliday(Long collegeId, String date, String reason) {
        College college = collegeRepo.findById(collegeId)
                .orElseThrow(() -> new NoSuchElementException("College not found."));
        CollegeHoliday holiday = new CollegeHoliday();
        holiday.setCollege(college);
        holiday.setDate(LocalDate.parse(date));
        holiday.setReason(reason);
        holidayRepo.save(holiday);
    }

    @Transactional
    public College updateCollege(Long collegeId, Map<String, Object> body) {
        College college = collegeRepo.findById(collegeId)
                .orElseThrow(() -> new NoSuchElementException("College not found."));
        if (body.containsKey("name")) college.setCollegeName((String) body.get("name"));
        if (body.containsKey("address")) college.setAddress((String) body.get("address"));
        if (body.containsKey("district")) college.setDistrict((String) body.get("district"));
        if (body.containsKey("principal")) college.setPrincipal((String) body.get("principal"));
        if (body.containsKey("phone")) college.setPhone((String) body.get("phone"));
        if (body.containsKey("email")) college.setEmail((String) body.get("email"));
        if (body.containsKey("latitude")) college.setLatitude(Double.parseDouble(body.get("latitude").toString()));
        if (body.containsKey("longitude")) college.setLongitude(Double.parseDouble(body.get("longitude").toString()));
        if (body.containsKey("radius")) college.setGeofenceRadius(Double.parseDouble(body.get("radius").toString()));
        return collegeRepo.save(college);
    }
}
