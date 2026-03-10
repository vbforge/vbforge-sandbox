package com.vbforge.jwtbasics.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

/**
 * User entity — stored in MySQL, also implements UserDetails
 * so Spring Security can use it directly without extra mapping.
 *
 * Key concept: by implementing UserDetails here we keep things simple.
 * In later projects (RBAC) we'll separate concerns further.
 */
@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false, unique = true, length = 150)
    private String email;

    // ----------------------------------------------------------------
    // UserDetails contract
    // ----------------------------------------------------------------

    /**
     * No roles in this project — that's Project 3 (RBAC).
     * We return an empty list here intentionally to keep focus on JWT mechanics.
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of();
    }

    /** Account is always active for this demo */
    @Override public boolean isAccountNonExpired()    { return true; }
    @Override public boolean isAccountNonLocked()     { return true; }
    @Override public boolean isCredentialsNonExpired(){ return true; }
    @Override public boolean isEnabled()              { return true; }
}
