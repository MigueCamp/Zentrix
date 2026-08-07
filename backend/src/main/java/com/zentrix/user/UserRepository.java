package com.zentrix.user;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Integer> {

    Optional<User> findByEmailAndStatus(String email, UserStatus status);

    boolean existsByEmail(String email);

    List<User> findByCompanyId(Integer companyId);

    Optional<User> findByIdAndCompanyId(Integer id, Integer companyId);
}
