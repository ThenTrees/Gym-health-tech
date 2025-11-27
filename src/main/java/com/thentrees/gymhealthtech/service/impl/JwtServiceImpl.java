package com.thentrees.gymhealthtech.service.impl;

import com.thentrees.gymhealthtech.model.User;
import com.thentrees.gymhealthtech.service.JwtService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class JwtServiceImpl implements JwtService {
  @Value("${app.jwt.secret}")
  private String jwtSecret;

  @Value("${app.jwt.expiration}")
  private long jwtExpiration;

  @Value("${app.jwt.refresh-expiration:604800000}") // 7 days default
  private long refreshExpiration;

  public String extractUsername(String token) {
    return extractClaim(token, Claims::getSubject);
  }

  public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
    final Claims claims = extractAllClaims(token);
    return claimsResolver.apply(claims);
  }

  public String generateToken(UserDetails userDetails) {
    return generateToken(new HashMap<>(), userDetails);
  }

  public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {
    return buildToken(extraClaims, userDetails, jwtExpiration);
  }

  public String generateRefreshToken(UserDetails userDetails) {
    return buildToken(new HashMap<>(), userDetails, refreshExpiration);
  }

  public String generateTokenForUser(User user) {
    try {
      Map<String, Object> claims = new HashMap<>();
      claims.put("userId", user.getId() != null ? user.getId().toString() : null);
      claims.put("email", user.getEmail());
      claims.put("role", user.getRole().getDisplayName());
      claims.put("status", user.getStatus().getDisplayName());
      claims.put("emailVerified", user.getEmailVerified());

      if (user.getProfile() != null) {
        claims.put("fullName", user.getProfile().getFullName());
      }

      return Jwts.builder()
          .setClaims(claims)
          .setSubject(user.getEmail())
          .setIssuedAt(new Date())
          .setExpiration(new Date(System.currentTimeMillis() + jwtExpiration))
          .signWith(getSignInKey(), SignatureAlgorithm.HS256) // ✅ Key trước, alg sau
          .compact();
    } catch (Exception e) {
      log.error("Error generating token for user {}: {}", user.getEmail(), e.getMessage());
      throw e;
    }
  }

  private String buildToken(
      Map<String, Object> extraClaims, UserDetails userDetails, long expiration) {
    return Jwts.builder()
        .setClaims(extraClaims)
        .setSubject(userDetails.getUsername())
        .setIssuedAt(new Date())
        .setExpiration(new Date(System.currentTimeMillis() + expiration))
        .signWith(getSignInKey(), SignatureAlgorithm.HS256)
        .compact();
  }

  public boolean isTokenValid(String token, UserDetails userDetails) {
    final String username = extractUsername(token);
    return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
  }

  public boolean isTokenValid(String token) {
    try {
      extractAllClaims(token);
      return !isTokenExpired(token);
    } catch (JwtException | IllegalArgumentException e) {
      log.error("Invalid JWT token: {}", e.getMessage());
      return false;
    }
  }

  @Override
  public UUID extractUserId(String token) {
    try {
      Claims claims = extractAllClaims(token);
      String userIdStr = claims.get("userId", String.class);
      if (userIdStr != null) {
        return UUID.fromString(userIdStr);
      }
    } catch (Exception e) {
      log.error("Error extracting userId from token: {}", e.getMessage());
    }
    return null;
  }

  private boolean isTokenExpired(String token) {
    try {
      return extractExpiration(token).before(new Date());
    } catch (JwtException e) {
      log.error("Error checking token expiration: {}", e.getMessage());
      return true;
    }
  }

  private Date extractExpiration(String token) {
    return extractClaim(token, Claims::getExpiration);
  }

  private Claims extractAllClaims(String token) {
    return Jwts.parser().setSigningKey(getSignInKey()).build().parseClaimsJws(token).getBody();
  }

  private Key getSignInKey() {
    byte[] keyBytes = Decoders.BASE64.decode(jwtSecret);
    return Keys.hmacShaKeyFor(keyBytes);
  }
}
