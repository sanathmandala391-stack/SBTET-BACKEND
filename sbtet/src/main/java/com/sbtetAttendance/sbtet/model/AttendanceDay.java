package com.sbtetAttendance.sbtet.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "attendance_days",
        uniqueConstraints = @UniqueConstraint(columnNames = {"student_id", "date"})
)
@Getter
@Setter
//@NoArgsConstructor
public class AttendanceDay {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private User student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "college_id", nullable = false)
    private College college;

    @Column(nullable = false)
    private LocalDate date;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AttendanceStatus status = AttendanceStatus.NONE;

    // Check-in details
    @Column(name = "check_in_time")
    private LocalDateTime checkInTime;

    @Column(name = "check_in_lat")
    private Double checkInLat;

    @Column(name = "check_in_lon")
    private Double checkInLon;

    @Column(name = "check_in_face_verified")
    private Boolean checkInFaceVerified = false;

    // Check-out details
    @Column(name = "check_out_time")
    private LocalDateTime checkOutTime;

    @Column(name = "check_out_lat")
    private Double checkOutLat;

    @Column(name = "check_out_lon")
    private Double checkOutLon;

    @Column(name = "check_out_face_verified")
    private Boolean checkOutFaceVerified = false;

    @Column(name = "gap_minutes")
    private Integer gapMinutes;

    // Manual override
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "overridden_by")
    private User overriddenBy;

    @Enumerated(EnumType.STRING)
    @Column(name = "original_status")
    private AttendanceStatus originalStatus;

    @Column(name = "override_reason")
    private String overrideReason;

    @Column(name = "overridden_at")
    private LocalDateTime overriddenAt;

    // Denormalized for fast queries
    @Column(nullable = false)
    private Integer month;

    @Column(nullable = false)
    private Integer year;

    @Column(name = "day_of_month", nullable = false)
    private Integer dayOfMonth;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public AttendanceDay(){

    }

    public AttendanceDay(Long id, User student, College college, LocalDate date, AttendanceStatus status, LocalDateTime checkInTime, Double checkInLat, Double checkInLon, Boolean checkInFaceVerified, LocalDateTime checkOutTime, Double checkOutLat, Double checkOutLon, Boolean checkOutFaceVerified, Integer gapMinutes, User overriddenBy, AttendanceStatus originalStatus, String overrideReason, LocalDateTime overriddenAt, Integer month, Integer year, Integer dayOfMonth, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.student = student;
        this.college = college;
        this.date = date;
        this.status = status;
        this.checkInTime = checkInTime;
        this.checkInLat = checkInLat;
        this.checkInLon = checkInLon;
        this.checkInFaceVerified = checkInFaceVerified;
        this.checkOutTime = checkOutTime;
        this.checkOutLat = checkOutLat;
        this.checkOutLon = checkOutLon;
        this.checkOutFaceVerified = checkOutFaceVerified;
        this.gapMinutes = gapMinutes;
        this.overriddenBy = overriddenBy;
        this.originalStatus = originalStatus;
        this.overrideReason = overrideReason;
        this.overriddenAt = overriddenAt;
        this.month = month;
        this.year = year;
        this.dayOfMonth = dayOfMonth;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getStudent() {
        return student;
    }

    public void setStudent(User student) {
        this.student = student;
    }

    public College getCollege() {
        return college;
    }

    public void setCollege(College college) {
        this.college = college;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public AttendanceStatus getStatus() {
        return status;
    }

    public void setStatus(AttendanceStatus status) {
        this.status = status;
    }

    public LocalDateTime getCheckInTime() {
        return checkInTime;
    }

    public void setCheckInTime(LocalDateTime checkInTime) {
        this.checkInTime = checkInTime;
    }

    public Double getCheckInLat() {
        return checkInLat;
    }

    public void setCheckInLat(Double checkInLat) {
        this.checkInLat = checkInLat;
    }

    public Double getCheckInLon() {
        return checkInLon;
    }

    public void setCheckInLon(Double checkInLon) {
        this.checkInLon = checkInLon;
    }

    public Boolean getCheckInFaceVerified() {
        return checkInFaceVerified;
    }

    public void setCheckInFaceVerified(Boolean checkInFaceVerified) {
        this.checkInFaceVerified = checkInFaceVerified;
    }

    public LocalDateTime getCheckOutTime() {
        return checkOutTime;
    }

    public void setCheckOutTime(LocalDateTime checkOutTime) {
        this.checkOutTime = checkOutTime;
    }

    public Double getCheckOutLat() {
        return checkOutLat;
    }

    public void setCheckOutLat(Double checkOutLat) {
        this.checkOutLat = checkOutLat;
    }

    public Double getCheckOutLon() {
        return checkOutLon;
    }

    public void setCheckOutLon(Double checkOutLon) {
        this.checkOutLon = checkOutLon;
    }

    public Boolean getCheckOutFaceVerified() {
        return checkOutFaceVerified;
    }

    public void setCheckOutFaceVerified(Boolean checkOutFaceVerified) {
        this.checkOutFaceVerified = checkOutFaceVerified;
    }

    public Integer getGapMinutes() {
        return gapMinutes;
    }

    public void setGapMinutes(Integer gapMinutes) {
        this.gapMinutes = gapMinutes;
    }

    public User getOverriddenBy() {
        return overriddenBy;
    }

    public void setOverriddenBy(User overriddenBy) {
        this.overriddenBy = overriddenBy;
    }

    public AttendanceStatus getOriginalStatus() {
        return originalStatus;
    }

    public void setOriginalStatus(AttendanceStatus originalStatus) {
        this.originalStatus = originalStatus;
    }

    public String getOverrideReason() {
        return overrideReason;
    }

    public void setOverrideReason(String overrideReason) {
        this.overrideReason = overrideReason;
    }

    public LocalDateTime getOverriddenAt() {
        return overriddenAt;
    }

    public void setOverriddenAt(LocalDateTime overriddenAt) {
        this.overriddenAt = overriddenAt;
    }

    public Integer getMonth() {
        return month;
    }

    public void setMonth(Integer month) {
        this.month = month;
    }

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    public Integer getDayOfMonth() {
        return dayOfMonth;
    }

    public void setDayOfMonth(Integer dayOfMonth) {
        this.dayOfMonth = dayOfMonth;
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