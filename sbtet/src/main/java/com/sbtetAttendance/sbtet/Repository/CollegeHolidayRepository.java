package com.sbtetAttendance.sbtet.Repository;

import com.sbtetAttendance.sbtet.model.CollegeHoliday;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface CollegeHolidayRepository extends JpaRepository<CollegeHoliday, Long> {

    List<CollegeHoliday> findByCollegeId(Long collegeId);

    boolean existsByCollegeIdAndDate(Long collegeId, LocalDate date);
}