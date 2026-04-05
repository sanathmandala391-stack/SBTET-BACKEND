package com.sbtetAttendance.sbtet.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "colleges")
@Getter
@Setter
//@NoArgsConstructor
public class College {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String collegeName;

    @Column(name = "college_code", nullable = false, unique = true)
    private String collegeCode;

    @Column(nullable = false)
    private String address;

    @Column(nullable = false)
    private String district;

    private String principal;
    private String phone;
    private String email;

    // Geolocation for geofencing
    @Column(nullable = false)
    private Double latitude;

    @Column(nullable = false)
    private Double longitude;

    @Column(name = "geofence_radius")
    private Double geofenceRadius = 200.0; // meters

    // Branches (stored as JSON string, e.g. "[{\"code\":\"CS\",\"name\":\"Computer Science\"}]")
    @Column(columnDefinition = "TEXT")
    private String branchesJson;

    // Holidays stored as embedded list before
//    @OneToMany(mappedBy = "college", cascade = CascadeType.ALL, orphanRemoval = true)
//    private List<CollegeHoliday> holidays = new ArrayList<>();

    // AFTER:
    @OneToMany(mappedBy = "college", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<CollegeHoliday> holidays = new ArrayList<>();


    @Column(name = "is_active")
    private Boolean isActive = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "registered_by")
    private User registeredBy;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public College(){

    }
    public College(Long id, String collegeName, String collegeCode, String address, String district, String principal, String phone, String email, Double latitude, Double longitude, Double geofenceRadius, String branchesJson, List<CollegeHoliday> holidays, Boolean isActive, User registeredBy, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.collegeName = collegeName;
        this.collegeCode = collegeCode;
        this.address = address;
        this.district = district;
        this.principal = principal;
        this.phone = phone;
        this.email = email;
        this.latitude = latitude;
        this.longitude = longitude;
        this.geofenceRadius = geofenceRadius;
        this.branchesJson = branchesJson;
        this.holidays = holidays;
        this.isActive = isActive;
        this.registeredBy = registeredBy;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCollegeName() {
        return collegeName;
    }

    public void setCollegeName(String collegeName) {
        this.collegeName = collegeName;
    }

    public String getCollegeCode() {
        return collegeCode;
    }

    public void setCollegeCode(String collegeCode) {
        this.collegeCode = collegeCode;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getDistrict() {
        return district;
    }

    public void setDistrict(String district) {
        this.district = district;
    }

    public String getPrincipal() {
        return principal;
    }

    public void setPrincipal(String principal) {
        this.principal = principal;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public Double getGeofenceRadius() {
        return geofenceRadius;
    }

    public void setGeofenceRadius(Double geofenceRadius) {
        this.geofenceRadius = geofenceRadius;
    }

    public String getBranchesJson() {
        return branchesJson;
    }

    public void setBranchesJson(String branchesJson) {
        this.branchesJson = branchesJson;
    }

    public List<CollegeHoliday> getHolidays() {
        return holidays;
    }

    public void setHolidays(List<CollegeHoliday> holidays) {
        this.holidays = holidays;
    }

    public Boolean getActive() {
        return isActive;
    }

    public void setActive(Boolean active) {
        isActive = active;
    }

    public User getRegisteredBy() {
        return registeredBy;
    }

    public void setRegisteredBy(User registeredBy) {
        this.registeredBy = registeredBy;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}