package com.vbforge.jwtbasics.repository;

import com.vbforge.jwtbasics.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Spring Data repository for User.
 *
 * findByUsername is used by UserDetailsService to load the user
 * during authentication and during JWT filter validation.
 */
public interface UserRepository extends JpaRepository<User, Long>{

    Optional<User> findByUsername(String username);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);

}
