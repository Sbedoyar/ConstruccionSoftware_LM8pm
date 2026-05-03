package app.infrastructure.security;

import app.domain.models.person.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtUtil {

    private static final String SECRET_KEY =
            "BANK_PROJECT_SECRET_KEY_FOR_JWT_AUTHENTICATION_2026_MINIMUM_32_CHARS";

    private static final long EXPIRATION_MILLISECONDS = 1000 * 60 * 60 * 2;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(SECRET_KEY.getBytes(StandardCharsets.UTF_8));
    }

    public String generateToken(User user) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + EXPIRATION_MILLISECONDS);

        return Jwts.builder()
                .subject(user.getUsername())
                .claim("identificationNumber", user.getIdentificationNumber())
                .claim("role", user.getSystemRole().name())
                .issuedAt(now)
                .expiration(expiration)
                .signWith(getSigningKey())
                .compact();
    }

    public Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public String extractUsername(String token) {
        return extractAllClaims(token).getSubject();
    }

    public String extractRole(String token) {
        return extractAllClaims(token).get("role", String.class);
    }

    public String extractIdentificationNumber(String token) {
        return extractAllClaims(token).get("identificationNumber", String.class);
    }

    public boolean isTokenValid(String token) {
        try {
            Date expiration = extractAllClaims(token).getExpiration();
            return expiration.after(new Date());
        } catch (Exception ex) {
            return false;
        }
    }
}