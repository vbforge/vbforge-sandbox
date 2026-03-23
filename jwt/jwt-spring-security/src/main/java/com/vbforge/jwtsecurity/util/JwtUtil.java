package com.vbforge.jwtsecurity.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Function;

@Component
@Slf4j
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private long expirationMs;


    public String generateToken(String username, String role) {
        return Jwts.builder()
                .claims(Map.of( "role", role))
                .subject(username)          // "sub" claim
                .issuedAt(new Date())                        // "iat" claim
                .expiration(new Date(System.currentTimeMillis() + expirationMs)) // "exp" claim
                .signWith(getSigningKey())                   // sign with HS256
                .compact();                                  // serialize to String
    }



    public boolean isTokenValid(String token, UserDetails userDetails) {
        //one role from current token
        String extractUserRole = extractUserRole(token);

        //all existed roles from db
        Collection<? extends GrantedAuthority> grantedAuthorities = userDetails.getAuthorities();

        Set<String> rolesAuth = new HashSet<>();

        for (GrantedAuthority grantedAuthority : grantedAuthorities) {
            String authority = grantedAuthority.getAuthority();
            rolesAuth.add(authority);
        }

        //check if found role is existed in db
        if(!rolesAuth.contains(extractUserRole)){
            return false; //-->token is not valid
        }

        return !isTokenExpired(token);
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }


    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public String extractUserRole(String token) {
        return extractClaim(token, claims -> claims.get("role").toString());
    }


    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }


    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())   // verify signature
                .build()
                .parseSignedClaims(token)      // parse & validate
                .getPayload();                 // get the claims body
    }

    private SecretKey getSigningKey() {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }


}
