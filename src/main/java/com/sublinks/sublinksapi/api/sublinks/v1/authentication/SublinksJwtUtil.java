package com.sublinks.sublinksapi.api.sublinks.v1.authentication;

import com.sublinks.sublinksapi.person.entities.Person;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.io.Serial;
import java.io.Serializable;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class SublinksJwtUtil implements Serializable {

  public static final long JWT_TOKEN_VALIDITY = 24 * 60 * 60;
  @Serial
  private static final long serialVersionUID = -2550185165626007488L;
  private final String secret;

  public SublinksJwtUtil(@Value("${jwt.secret}") final String secret) {

    this.secret = secret;
  }

  public String generateToken(final Person person) {

    final Map<String, Object> claims = new HashMap<>();
    return doGenerateToken(claims, person.getUsername());
  }

  public Boolean validateToken(final String token, final Person person) {

    final String tokenUsername = extractUsername(token);
    return (tokenUsername.equals(person.getUsername()) && !isTokenExpired(token));
  }

  public String extractUsername(final String token) {

    return extractClaim(token, Claims::getSubject);
  }

  public Date extractExpiration(final String token) {

    return extractClaim(token, Claims::getExpiration);
  }

  public <T> T extractClaim(final String token, final Function<Claims, T> claimsResolver) {

    final Claims claims = extractAllClaims(token);
    return claimsResolver.apply(claims);
  }

  private Claims extractAllClaims(final String token) {

    final SecretKey key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
    return Jwts.parser()
        .verifyWith(key)
        .build()
        .parseSignedClaims(token)
        .getPayload();
  }

  private Boolean isTokenExpired(final String token) {

    return extractExpiration(token).before(new Date());
  }

  private String doGenerateToken(final Map<String, Object> claims, final String subject) {

    final byte[] keyBytes = Decoders.BASE64.decode(secret);
    final Key key = Keys.hmacShaKeyFor(keyBytes);
    return Jwts.builder()
        .claims(claims)
        .subject(subject)
        .issuedAt(new Date(System.currentTimeMillis()))
        .expiration(new Date(System.currentTimeMillis() + JWT_TOKEN_VALIDITY * 1000))
        .signWith(key)
        .compact();
  }

}
