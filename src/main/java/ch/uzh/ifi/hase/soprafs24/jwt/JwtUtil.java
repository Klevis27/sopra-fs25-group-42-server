package ch.uzh.ifi.hase.soprafs24.jwt;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.Claims;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtUtil {

    private final String secretKey = "QxkrD1mqZgFDM82gsV1rJ6cRfNdvMoH4";  // Make sure to keep this secret

    // Generate Access Token (short-lived)
    public String generateAccessToken(Long id) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("id", id.toString());

        SecretKey key = new SecretKeySpec(secretKey.getBytes(), "HmacSHA256");

        return Jwts.builder()
                .claims(claims)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 24 * 14)) // 14 Days expiration = no automatic logout
                .signWith(key)
                .compact();
    }

    // Extract id from token
    public String extractId(String token) {
        return extractClaims(token).get("id", String.class);
    }

    public Long extractIdAsLong(String token) {
        return Long.parseLong(extractId(token));
    }

    // Static method for convenience in services/controllers
    public static Long getUserIdFromToken(String token) {
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        JwtUtil util = new JwtUtil(); // avoid this if using @Autowired, but for static usage this is simplest
        return util.extractIdAsLong(token);
    }

    // Extract claims from token
    private Claims extractClaims(String token) {
        SecretKey key = new SecretKeySpec(secretKey.getBytes(), "HmacSHA256");

        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    // Check if the token is expired
    public boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    // Extract expiration date from token
    public Date extractExpiration(String token) {
        return extractClaims(token).getExpiration();
    }

    // Validate the token
    public boolean validateToken(String token, String id) {
        return (id.equals(extractId(token)) && !isTokenExpired(token));
    }
}
