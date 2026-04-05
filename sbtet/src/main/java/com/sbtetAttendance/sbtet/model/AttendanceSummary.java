package com.sbtetAttendance.sbtet.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "attendance_summaries",
        uniqueConstraints = @UniqueConstraint(columnNames = {"student_id", "month", "year"})
)
@Getter
@Setter
//@NoArgsConstructor
public class AttendanceSummary {

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
    private Integer month;

    @Column(nullable = false)
    private Integer year;

    @Column(name = "total_working_days")
    private Integer totalWorkingDays = 0;

    @Column(name = "days_present")
    private Integer daysPresent = 0;

    @Column(name = "days_absent")
    private Integer daysAbsent = 0;

    @Column(name = "days_half_day")
    private Integer daysHalfDay = 0;

    @Column(name = "days_error")
    private Integer daysError = 0;

    @Column(name = "days_holiday")
    private Integer daysHoliday = 0;

    @Column(name = "days_weekend")
    private Integer daysWeekend = 0;

    @Column(name = "attendance_percentage")
    private Double attendancePercentage = 0.0;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public AttendanceSummary(){

    }

    public AttendanceSummary(Long id, User student, College college, Integer month, Integer year, Integer totalWorkingDays, Integer daysPresent, Integer daysAbsent, Integer daysHalfDay, Integer daysError, Integer daysHoliday, Integer daysWeekend, Double attendancePercentage, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.student = student;
        this.college = college;
        this.month = month;
        this.year = year;
        this.totalWorkingDays = totalWorkingDays;
        this.daysPresent = daysPresent;
        this.daysAbsent = daysAbsent;
        this.daysHalfDay = daysHalfDay;
        this.daysError = daysError;
        this.daysHoliday = daysHoliday;
        this.daysWeekend = daysWeekend;
        this.attendancePercentage = attendancePercentage;
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

    public Integer getTotalWorkingDays() {
        return totalWorkingDays;
    }

    public void setTotalWorkingDays(Integer totalWorkingDays) {
        this.totalWorkingDays = totalWorkingDays;
    }

    public Integer getDaysPresent() {
        return daysPresent;
    }

    public void setDaysPresent(Integer daysPresent) {
        this.daysPresent = daysPresent;
    }

    public Integer getDaysAbsent() {
        return daysAbsent;
    }

    public void setDaysAbsent(Integer daysAbsent) {
        this.daysAbsent = daysAbsent;
    }

    public Integer getDaysHalfDay() {
        return daysHalfDay;
    }

    public void setDaysHalfDay(Integer daysHalfDay) {
        this.daysHalfDay = daysHalfDay;
    }

    public Integer getDaysError() {
        return daysError;
    }

    public void setDaysError(Integer daysError) {
        this.daysError = daysError;
    }

    public Integer getDaysHoliday() {
        return daysHoliday;
    }

    public void setDaysHoliday(Integer daysHoliday) {
        this.daysHoliday = daysHoliday;
    }

    public Integer getDaysWeekend() {
        return daysWeekend;
    }

    public void setDaysWeekend(Integer daysWeekend) {
        this.daysWeekend = daysWeekend;
    }

    public Double getAttendancePercentage() {
        return attendancePercentage;
    }

    public void setAttendancePercentage(Double attendancePercentage) {
        this.attendancePercentage = attendancePercentage;
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
