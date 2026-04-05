package com.sbtetAttendance.sbtet.Repository;

import com.sbtetAttendance.sbtet.model.AttendanceSummary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AttendanceSummaryRepository extends JpaRepository<AttendanceSummary, Long> {
    Optional<AttendanceSummary> findByStudentIdAndMonthAndYear(Long studentId, int month, int year);
    List<AttendanceSummary> findByCollegeIdAndMonthAndYear(Long collegeId, int month, int year);
}