package com.sbtetAttendance.sbtet.Repository;
import com.sbtetAttendance.sbtet.model.College;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CollegeRepository extends JpaRepository<College, Long> {

    Optional<College> findByCollegeCode(String collegeCode);

    boolean existsByCollegeCode(String collegeCode);

    List<College> findByIsActiveTrueOrderByCreatedAtDesc();
}
