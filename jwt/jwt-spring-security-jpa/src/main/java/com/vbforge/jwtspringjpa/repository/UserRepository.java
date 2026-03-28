package com.vbforge.jwtspringjpa.repository;

import com.vbforge.jwtspringjpa.dto.response.AdminResponse;
import com.vbforge.jwtspringjpa.entity.Role;
import com.vbforge.jwtspringjpa.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    boolean existsByUsername(String username);

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsByRole(Role role);

    List<User> findByRole(Role role);


}
