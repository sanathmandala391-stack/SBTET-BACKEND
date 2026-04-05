package com.sbtetAttendance.sbtet.controller;

import com.sbtetAttendance.sbtet.model.*;
import com.sbtetAttendance.sbtet.Repository.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/college")
public class CollegeController {

    private final CollegeRepository collegeRepo;

    public CollegeController(CollegeRepository collegeRepo) {
        this.collegeRepo = collegeRepo;
    }

    // Public endpoint to fetch colleges by code (for registration form)
    @GetMapping
    public ResponseEntity<?> getColleges() {
        List<College> colleges = collegeRepo.findByIsActiveTrueOrderByCreatedAtDesc();
        return ResponseEntity.ok(colleges.stream().map(c -> Map.of(
                "id", c.getId(),
                "name", c.getCollegeName(),
                "collegeCode", c.getCollegeCode(),
                "district", c.getDistrict()
        )).toList());
    }
//
//    @GetMapping("/{code}")
//    public ResponseEntity<?> getByCode(@PathVariable String code) {
//        return collegeRepo.findByCollegeCode(code.toUpperCase())
//                .map(ResponseEntity::ok)
//                .orElse(ResponseEntity.notFound().build());
//    }



    @GetMapping("/{code}")
    public ResponseEntity<?> getByCode(@PathVariable String code) {
        return collegeRepo.findByCollegeCode(code.toUpperCase())
                .map(c -> {
                    try {
                        ObjectMapper mapper = new ObjectMapper();

                        List<Map<String, String>> branches = new ArrayList<>();

                        if (c.getBranchesJson() != null && !c.getBranchesJson().isEmpty()) {
                            branches = mapper.readValue(
                                    c.getBranchesJson(),
                                    new TypeReference<List<Map<String, String>>>() {}
                            );
                        }

                        return ResponseEntity.ok(Map.of(
                                "collegeName", c.getCollegeName(),
                                "collegeCode", c.getCollegeCode(),
                                "district", c.getDistrict(),
                                "branches", branches // ✅ NOW ARRAY
                        ));

                    } catch (Exception e) {
                        return ResponseEntity.internalServerError().body("Error parsing branches");
                    }
                })
                .orElse(ResponseEntity.notFound().build());
    }

}