package com.railbit.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.railbit.domain.Role;
import com.railbit.domain.User;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsernameIgnoreCase(String username);

    boolean existsByUsernameIgnoreCase(String username);

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCaseAndIdNot(String email, Long id);

    @EntityGraph(attributePaths = "department")
    Optional<User> findWithDepartmentById(Long id);

    @EntityGraph(attributePaths = "department")
    List<User> findByRoleOrderByFullNameAsc(Role role);

    @EntityGraph(attributePaths = "department")
    List<User> findByRoleAndDepartmentIdOrderByFullNameAsc(Role role, Long departmentId);

    List<User> findByRoleAndDepartmentIdAndEnabledTrue(Role role, Long departmentId);

    long countByRole(Role role);

    long countByRoleAndDepartmentId(Role role, Long departmentId);
}
