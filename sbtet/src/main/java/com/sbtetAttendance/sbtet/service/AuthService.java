//package com.sbtetAttendance.sbtet.service;
//
//
//
//import com.sbtetAttendance.sbtet.model.College;
//import com.sbtetAttendance.sbtet.model.Role;
//import com.sbtetAttendance.sbtet.model.User;
//import com.sbtetAttendance.sbtet.Repository.UserRepository;
//import com.sbtetAttendance.sbtet.Repository.CollegeRepository;
//import com.sbtetAttendance.sbtet.security.JwtUtil;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.security.crypto.password.PasswordEncoder;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.time.Instant;
//import java.util.Map;
//
//@Service
//public class AuthService {
//
//    @Autowired private UserRepository userRepo;
//    @Autowired private CollegeRepository collegeRepo;
//    @Autowired private PasswordEncoder passwordEncoder;
//    @Autowired private JwtUtil jwtUtil;
//    @Autowired private CloudinaryService cloudinaryService;
//
//    @Transactional
//    public Map<String, Object> registerStudent(Map<String, Object> body) throws Exception {
//        String collegeCode = ((String) body.get("collegeCode")).toUpperCase();
//        College college = collegeRepo.findByCollegeCode(collegeCode)
//                .orElseThrow(() -> new IllegalArgumentException("College code not found."));
//
//        String email = (String) body.get("email");
//        String pinNumber = (String) body.get("pinNumber");
//
//        if (userRepo.existsByEmail(email))
//            throw new IllegalArgumentException("Email already registered.");
//        if (pinNumber != null && userRepo.existsByPinNumber(pinNumber))
//            throw new IllegalArgumentException("PIN number already registered.");
//
//        String faceImage = "", faceImagePublicId = "";
//        String faceImageBase64 = (String) body.get("faceImageBase64");
//        if (faceImageBase64 != null && !faceImageBase64.isBlank()) {
//            Map<String, String> uploaded = cloudinaryService.uploadBase64(faceImageBase64, "sbtet_faces");
//            faceImage = uploaded.get("url");
//            faceImagePublicId = uploaded.get("publicId");
//        }
//
//        User user = new User();
//        user.setName((String) body.get("name"));
//        user.setEmail(email);
//        user.setPassword(passwordEncoder.encode((String) body.get("password")));
//        user.setRole(Role.STUDENT);
//        user.setPinNumber(pinNumber);
//        user.setBranch((String) body.get("branch"));
//        user.setSemester((String) body.get("semester"));
//        user.setCollege(college);
//        user.setCollegeCode(college.getCollegeCode());
//        user.setFaceImage(faceImage);
//        user.setFaceImagePublicId(faceImagePublicId);
//        user.setFaceDescriptor((String) body.getOrDefault("faceDescriptor", "[]"));
//        user.setAttendeeId(Instant.now().toEpochMilli() + "-" + (int)(Math.random() * 1000));
//        user.setIsApproved(false);
//        userRepo.save(user);
//
//        return Map.of("message", "Registration successful! Awaiting approval from your HOD.", "userId", user.getId());
//    }
//
//    @Transactional
//    public Map<String, Object> registerFaculty(Map<String, Object> body) throws Exception {
//        String collegeCode = ((String) body.get("collegeCode")).toUpperCase();
//        College college = collegeRepo.findByCollegeCode(collegeCode)
//                .orElseThrow(() -> new IllegalArgumentException("College code not found."));
//
//        String email = (String) body.get("email");
//        if (userRepo.existsByEmail(email))
//            throw new IllegalArgumentException("Email already registered.");
//
//        String faceImage = "", faceImagePublicId = "";
//        String faceImageBase64 = (String) body.get("faceImageBase64");
//        if (faceImageBase64 != null && !faceImageBase64.isBlank()) {
//            Map<String, String> uploaded = cloudinaryService.uploadBase64(faceImageBase64, "sbtet_faces");
//            faceImage = uploaded.get("url");
//            faceImagePublicId = uploaded.get("publicId");
//        }
//
//        String roleStr = (String) body.getOrDefault("role", "faculty");
//        Role role = "hod".equalsIgnoreCase(roleStr) ? Role.HOD : Role.FACULTY;
//
//        User user = new User();
//        user.setName((String) body.get("name"));
//        user.setEmail(email);
//        user.setPassword(passwordEncoder.encode((String) body.get("password")));
//        user.setRole(role);
//        user.setCollege(college);
//        user.setCollegeCode(college.getCollegeCode());
//        user.setDepartment((String) body.get("department"));
//        user.setSubjectsJson((String) body.getOrDefault("subjects", "[]"));
//        user.setFaceImage(faceImage);
//        user.setFaceImagePublicId(faceImagePublicId);
//        user.setFaceDescriptor((String) body.getOrDefault("faceDescriptor", "[]"));
//        user.setIsApproved(false);
//        userRepo.save(user);
//
//        return Map.of("message", role.name() + " registration successful! Awaiting approval.");
//    }
//
//    public Map<String, Object> login(String email, String password) {
//        User user = userRepo.findByEmail(email)
//                .orElseThrow(() -> new IllegalArgumentException("Invalid credentials."));
//
//        if (!passwordEncoder.matches(password, user.getPassword()))
//            throw new IllegalArgumentException("Invalid credentials.");
//
//        if (!user.getIsActive())
//            throw new IllegalStateException("Account is deactivated.");
//
//        String token = jwtUtil.generateToken(user.getId());
//
//        return Map.of(
//                "token", token,
//                "user", Map.of(
//                        "id", user.getId(),
//                        "name", user.getName(),
//                        "email", user.getEmail(),
//                        "role", user.getRole(),
//                        "isApproved", user.getIsApproved(),
//                        "pinNumber", user.getPinNumber() != null ? user.getPinNumber() : "",
//                        "branch", user.getBranch() != null ? user.getBranch() : "",
//                        "semester", user.getSemester() != null ? user.getSemester() : "",
//                        "collegeCode", user.getCollegeCode() != null ? user.getCollegeCode() : "",
//                        "faceImage", user.getFaceImage() != null ? user.getFaceImage() : "",
//                        "department", user.getDepartment() != null ? user.getDepartment() : ""
//                )
//        );
//    }
//
//    @Transactional
//    public void updateFaceDescriptor(User user, String faceDescriptor, String faceImageBase64) throws Exception {
//        user.setFaceDescriptor(faceDescriptor);
//        if (faceImageBase64 != null && !faceImageBase64.isBlank()) {
//            if (user.getFaceImagePublicId() != null && !user.getFaceImagePublicId().isBlank()) {
//                cloudinaryService.deleteImage(user.getFaceImagePublicId());
//            }
//            Map<String, String> uploaded = cloudinaryService.uploadBase64(faceImageBase64, "sbtet_faces");
//            user.setFaceImage(uploaded.get("url"));
//            user.setFaceImagePublicId(uploaded.get("publicId"));
//        }
//        userRepo.save(user);
//    }
//}

package com.sbtetAttendance.sbtet.service;

import com.sbtetAttendance.sbtet.model.College;
import com.sbtetAttendance.sbtet.model.Role;
import com.sbtetAttendance.sbtet.model.User;
import com.sbtetAttendance.sbtet.Repository.UserRepository;
import com.sbtetAttendance.sbtet.Repository.CollegeRepository;
import com.sbtetAttendance.sbtet.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Service
public class AuthService {

    @Autowired private UserRepository userRepo;
    @Autowired private CollegeRepository collegeRepo;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private JwtUtil jwtUtil;
    @Autowired private CloudinaryService cloudinaryService;

    @Transactional
    public Map<String, Object> registerStudent(Map<String, Object> body) throws Exception {
        String collegeCode = ((String) body.get("collegeCode")).toUpperCase();
        College college = collegeRepo.findByCollegeCode(collegeCode)
                .orElseThrow(() -> new IllegalArgumentException("College code not found."));

        String email = (String) body.get("email");
        String pinNumber = (String) body.get("pinNumber");

        if (userRepo.existsByEmail(email))
            throw new IllegalArgumentException("Email already registered.");
        if (pinNumber != null && userRepo.existsByPinNumber(pinNumber))
            throw new IllegalArgumentException("PIN number already registered.");

        String faceImage = "", faceImagePublicId = "";
        String faceImageBase64 = (String) body.get("faceImageBase64");
        if (faceImageBase64 != null && !faceImageBase64.isBlank()) {
            Map<String, String> uploaded = cloudinaryService.uploadBase64(faceImageBase64, "sbtet_faces");
            faceImage = uploaded.get("url");
            faceImagePublicId = uploaded.get("publicId");
        }

        User user = new User();
        user.setName((String) body.get("name"));
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode((String) body.get("password")));
        user.setRole(Role.STUDENT);
        user.setPinNumber(pinNumber);
        user.setBranch((String) body.get("branch"));
        user.setSemester((String) body.get("semester"));
        user.setCollege(college);
        user.setCollegeCode(college.getCollegeCode());
        user.setFaceImage(faceImage);
        user.setFaceImagePublicId(faceImagePublicId);
        user.setFaceDescriptor((String) body.getOrDefault("faceDescriptor", "[]"));
        user.setAttendeeId(Instant.now().toEpochMilli() + "-" + (int)(Math.random() * 1000));
        user.setApproved(false);
        userRepo.save(user);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Registration successful! Awaiting approval from your HOD.");
        response.put("userId", user.getId());
        return response;
    }

    @Transactional
    public Map<String, Object> registerFaculty(Map<String, Object> body) throws Exception {
        String collegeCode = ((String) body.get("collegeCode")).toUpperCase();
        College college = collegeRepo.findByCollegeCode(collegeCode)
                .orElseThrow(() -> new IllegalArgumentException("College code not found."));

        String email = (String) body.get("email");
        if (userRepo.existsByEmail(email))
            throw new IllegalArgumentException("Email already registered.");

        String faceImage = "", faceImagePublicId = "";
        String faceImageBase64 = (String) body.get("faceImageBase64");
        if (faceImageBase64 != null && !faceImageBase64.isBlank()) {
            Map<String, String> uploaded = cloudinaryService.uploadBase64(faceImageBase64, "sbtet_faces");
            faceImage = uploaded.get("url");
            faceImagePublicId = uploaded.get("publicId");
        }

        String roleStr = (String) body.getOrDefault("role", "faculty");
        Role role = "hod".equalsIgnoreCase(roleStr) ? Role.HOD : Role.FACULTY;

        User user = new User();
        user.setName((String) body.get("name"));
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode((String) body.get("password")));
        user.setRole(role);
        user.setCollege(college);
        user.setCollegeCode(college.getCollegeCode());
        user.setDepartment((String) body.get("department"));
        user.setSubjectsJson((String) body.getOrDefault("subjects", "[]"));
        user.setFaceImage(faceImage);
        user.setFaceImagePublicId(faceImagePublicId);
        user.setFaceDescriptor((String) body.getOrDefault("faceDescriptor", "[]"));
        user.setApproved(false);
        userRepo.save(user);

        Map<String, Object> response = new HashMap<>();
        response.put("message", role.name() + " registration successful! Awaiting approval.");
        return response;
    }

    public Map<String, Object> login(String email, String password) {
        User user = userRepo.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Invalid credentials."));

        if (!passwordEncoder.matches(password, user.getPassword()))
            throw new IllegalArgumentException("Abba ");

        if (!user.getActive())
            throw new IllegalStateException("Account is deactivated.");

        String token = jwtUtil.generateToken(user.getId());

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

        Map<String, Object> response = new HashMap<>();
        response.put("token", token);
        response.put("user", userMap);

        return response;
    }



    @Transactional
    public void updateFaceDescriptor(User user, String faceDescriptor, String faceImageBase64) throws Exception {
        user.setFaceDescriptor(faceDescriptor);
        if (faceImageBase64 != null && !faceImageBase64.isBlank()) {
            if (user.getFaceImagePublicId() != null && !user.getFaceImagePublicId().isBlank()) {
                cloudinaryService.deleteImage(user.getFaceImagePublicId());
            }
            Map<String, String> uploaded = cloudinaryService.uploadBase64(faceImageBase64, "sbtet_faces");
            user.setFaceImage(uploaded.get("url"));
            user.setFaceImagePublicId(uploaded.get("publicId"));
        }
        userRepo.save(user);
    }


    //temp//
    @Transactional
    public String resetAdminPasswordTemporary() {
        User admin = userRepo.findByEmail("admin@sbtet.telangana.gov.in")
                .orElseThrow(() -> new IllegalArgumentException("Admin not found"));

        // Reset password to a known temporary value
        String tempPassword = "Admin@123456";
        admin.setPassword(passwordEncoder.encode(tempPassword));  // encode it properly
        admin.setActive(true);       // make sure account is active
        admin.setApproved(true);     // make sure account is approved
        userRepo.save(admin);

        return "Temporary admin password reset to " + tempPassword;
    }
}