package com.example.smbackend.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Stateless JWT utility service.
 *
 * <p>Tokens are signed with HMAC-SHA-256.  The signing key is resolved in order:
 * <ol>
 *   <li>The {@code SM_JWT_SECRET} environment variable (Base64-encoded 256-bit key).</li>
 *   <li>A hard-coded development fallback — <strong>replace before production</strong>.</li>
 * </ol>
 */
@Service
public class JwtService {

    /**
     * Fallback secret used only during local development.
     * Generate a production key with: {@code openssl rand -base64 32}
     * and export it as the {@code SM_JWT_SECRET} environment variable.
     */
    private static final String DEV_FALLBACK_SECRET =
            "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970";

    /** Token validity period: 24 hours expressed in milliseconds. */
    private static final long TOKEN_EXPIRY_MS = 24 * 60 * 60 * 1_000L;

    // ──────────────────────────────────────────────────────────────────────────
    // Public API
    // ──────────────────────────────────────────────────────────────────────────

    /**
     * Generates a signed JWT for the given {@link UserDetails}.
     *
     * @param userDetails the authenticated principal
     * @return a compact, URL-safe JWT string
     */
    public String generateToken(UserDetails userDetails) {
        return generateToken(new HashMap<>(), userDetails);
    }

    /**
     * Generates a signed JWT with additional custom claims.
     *
     * @param extraClaims additional claims to embed in the token body
     * @param userDetails the authenticated principal
     * @return a compact, URL-safe JWT string
     */
    public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {
        return Jwts.builder()
                .claims(extraClaims)
                .subject(userDetails.getUsername())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + TOKEN_EXPIRY_MS))
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * Extracts the {@code sub} claim (username / email) from a token.
     *
     * @param token compact JWT string
     * @return the subject claim value
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Validates a token against the supplied {@link UserDetails}.
     *
     * <p>A token is valid when:
     * <ul>
     *   <li>Its {@code sub} claim matches the username in {@code userDetails}.</li>
     *   <li>It has not yet expired.</li>
     * </ul>
     *
     * @param token       compact JWT string
     * @param userDetails the principal to validate against
     * @return {@code true} if the token is valid; {@code false} otherwise
     */
    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Internal helpers
    // ──────────────────────────────────────────────────────────────────────────

    /**
     * Extracts an arbitrary claim from the token using a resolver function.
     *
     * @param token          compact JWT string
     * @param claimsResolver function that maps {@link Claims} to the desired value
     * @param <T>            the type of the extracted claim
     * @return the resolved claim value
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Resolves the HMAC-SHA-256 {@link SecretKey} from the environment or the dev fallback.
     */
    private SecretKey getSigningKey() {
        String secret = System.getenv("SM_JWT_SECRET");
        if (secret == null || secret.isBlank()) {
            secret = DEV_FALLBACK_SECRET;
        }
        byte[] keyBytes = Decoders.BASE64.decode(secret);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
