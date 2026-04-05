package com.sbtetAttendance.sbtet.Repository;


import com.sbtetAttendance.sbtet.model.Role;
import com.sbtetAttendance.sbtet.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsByPinNumber(String pinNumber);

    Optional<User> findByEmailOrPinNumber(String email, String pinNumber);

    List<User> findByCollegeIdAndRole(Long collegeId, Role role);

    List<User> findByCollegeIdAndRoleAndIsApprovedAndIsActive(
            Long collegeId, Role role, Boolean isApproved, Boolean isActive);

    List<User> findByIsApprovedFalseAndRoleNot(Role role);

    List<User> findByCollegeIdAndIsApprovedFalseAndRoleIn(Long collegeId, List<Role> roles);

    List<User> findByRoleNotAndIsActiveTrue(Role role);

    long countByRoleAndIsActiveTrue(Role role);

    long countByIsApprovedFalseAndRoleNot(Role role);
}