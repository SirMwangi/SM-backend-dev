package com.example.smbackend.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Service responsible for all JSON Web Token (JWT) lifecycle operations.
 *
 * <p>Uses JJWT 0.12.x API. The HMAC-SHA256 signing key is derived from the
 * Base64-encoded secret stored in {@code jwt.secret} application property.
 */
@Service
public class JwtService {

    /** Base64-encoded HMAC-SHA256 secret, injected from application.yml / environment. */
    @Value("${jwt.secret}")
    private String jwtSecret;

    /** Token validity window in milliseconds (default 24 h = 86_400_000 ms). */
    @Value("${jwt.expiration-ms}")
    private long jwtExpirationMs;

    // -------------------------------------------------------------------------
    // Public API
    // -------------------------------------------------------------------------

    /**
     * Generates a JWT for the given {@link UserDetails} principal.
     *
     * @param userDetails the authenticated principal; its {@code username} becomes the JWT subject
     * @return signed, compact JWT string
     */
    public String generateToken(UserDetails userDetails) {
        return generateToken(new HashMap<>(), userDetails);
    }

    /**
     * Generates a JWT with additional claims.
     *
     * @param extraClaims additional key/value pairs to embed in the JWT payload
     * @param userDetails the authenticated principal
     * @return signed, compact JWT string
     */
    public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {
        long now = System.currentTimeMillis();
        return Jwts.builder()
                .claims(extraClaims)
                .subject(userDetails.getUsername())
                .issuedAt(new Date(now))
                .expiration(new Date(now + jwtExpirationMs))
                .signWith(signingKey())
                .compact();
    }

    /**
     * Extracts the {@code sub} (subject / username) claim from a JWT.
     *
     * @param token compact JWT string
     * @return the username embedded as the subject claim
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Returns {@code true} when the token is cryptographically valid and has not expired
     * for the supplied {@link UserDetails}.
     *
     * @param token       compact JWT string
     * @param userDetails the user to validate against
     * @return {@code true} if valid; {@code false} otherwise
     */
    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    /**
     * Extracts an arbitrary claim using the provided resolver function.
     *
     * @param token    compact JWT string
     * @param resolver function that maps {@link Claims} to the desired value
     * @param <T>      type of the claim value
     * @return the resolved claim value
     */
    private <T> T extractClaim(String token, Function<Claims, T> resolver) {
        Claims claims = extractAllClaims(token);
        return resolver.apply(claims);
    }

    /**
     * Parses and validates the JWT signature, then returns all claims.
     *
     * @param token compact JWT string
     * @return parsed {@link Claims}
     * @throws io.jsonwebtoken.JwtException if the token is malformed or the signature is invalid
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(signingKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Returns {@code true} when the token's expiration date is in the past.
     *
     * @param token compact JWT string
     * @return {@code true} if expired
     */
    private boolean isTokenExpired(String token) {
        return extractClaim(token, Claims::getExpiration).before(new Date());
    }

    /**
     * Builds the {@link SecretKey} used to sign and verify tokens.
     * The key is derived from the Base64-encoded {@code jwt.secret} property.
     *
     * @return HMAC-SHA key
     */
    private SecretKey signingKey() {
        byte[] keyBytes = Decoders.BASE64.decode(jwtSecret);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
