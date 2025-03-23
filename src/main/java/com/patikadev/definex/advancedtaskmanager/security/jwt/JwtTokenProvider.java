package com.patikadev.definex.advancedtaskmanager.security.jwt;

import com.patikadev.definex.advancedtaskmanager.config.ApplicationProperties;
import com.patikadev.definex.advancedtaskmanager.constant.SecurityConstants;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.security.Key;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@Slf4j
public class JwtTokenProvider {

    private final Key key;
    private final long jwtExpiration;
    private final String jwtIssuer;
    private final String jwtAudience;

    public JwtTokenProvider(ApplicationProperties applicationProperties) {
        Key tokenSecretKey;
        String secret = applicationProperties.getSecurity().getTokenSecret();

        if (StringUtils.hasText(secret)) {
            try {
                byte[] keyBytes = Decoders.BASE64.decode(secret);
                if (keyBytes.length >= 64) {
                    tokenSecretKey = Keys.hmacShaKeyFor(keyBytes);
                } else {
                    tokenSecretKey = Keys.secretKeyFor(SignatureAlgorithm.HS512);
                }
            } catch (IllegalArgumentException e) {
                tokenSecretKey = Keys.secretKeyFor(SignatureAlgorithm.HS512);
            }
        } else {
            tokenSecretKey = Keys.secretKeyFor(SignatureAlgorithm.HS512);
        }

        this.key = tokenSecretKey;
        this.jwtExpiration = applicationProperties.getSecurity().getTokenExpirationMs();
        this.jwtIssuer = applicationProperties.getSecurity().getJwtIssuer();
        this.jwtAudience = applicationProperties.getSecurity().getJwtAudience();
    }

    public String generateTokenFromUsername(String username, Collection<? extends GrantedAuthority> authorities) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpiration);

        String authoritiesString = authorities.stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));

        return Jwts.builder()
                .setSubject(username)
                .claim(SecurityConstants.JWT_AUTHORITIES_KEY, authoritiesString)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .setIssuer(jwtIssuer)
                .setAudience(jwtAudience)
                .signWith(key, SignatureAlgorithm.HS512)
                .compact();
    }

    public String getUsernameFromToken(String token) {
        return getClaimFromToken(token, Claims::getSubject);
    }

    public Date getExpirationDateFromToken(String token) {
        return getClaimFromToken(token, Claims::getExpiration);
    }

    public <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = getAllClaimsFromToken(token);
        return claimsResolver.apply(claims);
    }

    private Claims getAllClaimsFromToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return !isTokenExpired(token);
        } catch (SecurityException ex) {
            log.error(SecurityConstants.INVALID_JWT_SIGNATURE);
        } catch (MalformedJwtException ex) {
            log.error(SecurityConstants.INVALID_JWT_TOKEN);
        } catch (ExpiredJwtException ex) {
            log.error(SecurityConstants.EXPIRED_JWT_TOKEN);
        } catch (UnsupportedJwtException ex) {
            log.error(SecurityConstants.UNSUPPORTED_JWT_TOKEN);
        } catch (IllegalArgumentException ex) {
            log.error(SecurityConstants.EMPTY_JWT_CLAIMS);
        }
        return false;
    }

    public boolean isTokenExpired(String token) {
        final Date expiration = getExpirationDateFromToken(token);
        return expiration.before(new Date());
    }

    public Authentication getAuthentication(String token) {
        Claims claims = getAllClaimsFromToken(token);

        Collection<? extends GrantedAuthority> authorities =
                Arrays.stream(claims.get(SecurityConstants.JWT_AUTHORITIES_KEY).toString().split(","))
                        .filter(auth -> !auth.trim().isEmpty())
                        .map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toList());

        UserDetails principal = new User(claims.getSubject(), "", authorities);

        return new UsernamePasswordAuthenticationToken(principal, token, authorities);
    }

    public String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader(SecurityConstants.HEADER_STRING);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(SecurityConstants.TOKEN_PREFIX)) {
            return bearerToken.substring(7);
        }
        return null;
    }
} 