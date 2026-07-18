package com.inridemart.ai;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.filter.OncePerRequestFilter;
import javax.crypto.SecretKey;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

@Configuration
class SecurityConfig {
    @Bean SecurityFilterChain chain(HttpSecurity http, JwtFilter filter) throws Exception {
        return http.csrf(AbstractHttpConfigurer::disable).sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(a -> a.requestMatchers("/actuator/health/**", "/v3/api-docs/**", "/swagger-ui.html", "/swagger-ui/**").permitAll().anyRequest().authenticated())
                .addFilterBefore(filter, UsernamePasswordAuthenticationFilter.class).build();
    }
    @Bean JwtFilter jwtFilter(@Value("${security.jwt.secret}") String secret, @Value("${security.jwt.issuer}") String issuer) { return new JwtFilter(secret, issuer); }
    static final class JwtFilter extends OncePerRequestFilter {
        private final SecretKey key; private final String issuer;
        JwtFilter(String secret, String issuer) { key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8)); this.issuer = issuer; }
        @Override protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws ServletException, IOException {
            String header = request.getHeader("Authorization");
            if (header != null && header.startsWith("Bearer ")) try {
                Claims claims = Jwts.parser().verifyWith(key).requireIssuer(issuer).build().parseSignedClaims(header.substring(7)).getPayload();
                if ("access".equals(claims.get("token_use", String.class))) SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(UUID.fromString(claims.getSubject()), null, List.of()));
            } catch (RuntimeException ignored) { SecurityContextHolder.clearContext(); }
            chain.doFilter(request, response);
        }
    }
}
