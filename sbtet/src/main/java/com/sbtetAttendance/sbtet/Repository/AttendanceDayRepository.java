package com.sbtetAttendance.sbtet.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.sbtetAttendance.sbtet.model.AttendanceDay;
import com.sbtetAttendance.sbtet.model.AttendanceStatus;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface AttendanceDayRepository extends JpaRepository<AttendanceDay, Long> {
    Optional<AttendanceDay> findByStudentIdAndDate(Long studentId, LocalDate date);
    List<AttendanceDay> findByStudentIdOrderByDateAsc(Long studentId);
    List<AttendanceDay> findByStudentIdAndMonthAndYear(Long studentId, int month, int year);
    List<AttendanceDay> findByStudentIdInAndDate(List<Long> studentIds, LocalDate date);
    List<AttendanceDay> findByStudentIdInAndMonthAndYear(List<Long> studentIds, int month, int year);
    List<AttendanceDay> findByDateAndStatus(LocalDate date, AttendanceStatus status);
    List<AttendanceDay> findByDateAndStatusAndCheckOutTimeIsNull(LocalDate date, AttendanceStatus status);
    long countByDateAndStatus(LocalDate date, AttendanceStatus status);
}
