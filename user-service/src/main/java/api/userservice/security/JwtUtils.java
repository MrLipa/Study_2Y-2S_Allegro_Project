package api.userservice.security;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.stream.Collectors;
import java.util.List;
import java.util.Collections;

import javax.crypto.SecretKey;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.Getter;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import api.userservice.model.Role;
import api.userservice.model.User;

@Component
public class JwtUtils {
    @Value("${app.jwtSecret}")
    private String jwtSecret;

    @Getter
    @Value("${app.jwtExpirationMs}")
    private int jwtExpirationMs;

    @Getter
    @Value("${app.jwtRefreshExpirationMs}")
    private int jwtRefreshExpirationMs;

    private SecretKey getSigningKey() {
    return Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }

    public String generateAccessToken(User user) {
        List<String> roles = user.getRoles().stream()
                                 .map(Role::getName)
                                 .collect(Collectors.toList());

        return Jwts.builder()
                .id(user.getId().toString())
                .claim("Roles", roles)
                .issuedAt(new Date())
                .expiration(new Date((new Date()).getTime() + jwtExpirationMs))
                .signWith(getSigningKey())
                .compact();
    }

    public String generateRefreshToken(User user) {
        return Jwts.builder()
                .id(user.getId().toString())
                .issuedAt(new Date())
                .expiration(new Date((new Date()).getTime() + jwtRefreshExpirationMs))
                .signWith(getSigningKey())
                .compact();
    }

    public String getUserIdFromJwtToken(String token) {
        return Jwts.parser().verifyWith(getSigningKey()).build().parseSignedClaims(token).getPayload().getId();
    }

    public List<String> getUserRolesFromJwtToken(String token) {
        Claims claims = Jwts.parser().verifyWith(getSigningKey()).build().parseSignedClaims(token).getPayload();

        List<?> roles = claims.get("Roles", List.class);

        if (roles != null) {
            return roles.stream()
                        .filter(obj -> obj instanceof String)
                        .map(obj -> (String) obj)
                        .collect(Collectors.toList());
        }

        return Collections.emptyList();
    }

    public boolean validateJwtToken(String authToken) {
        try {
            Jwts.parser().verifyWith(getSigningKey()).build().parseSignedClaims(authToken);
            return true;
        } catch (JwtException e) {
            System.out.println("Invalid JWT token: " + e.getMessage());
        }
        return false;
    }
}
 