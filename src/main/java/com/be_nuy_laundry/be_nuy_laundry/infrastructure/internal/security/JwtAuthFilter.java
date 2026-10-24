package com.be_nuy_laundry.be_nuy_laundry.infrastructure.internal.security;

import com.be_nuy_laundry.be_nuy_laundry.application.ports.out.AuthRepositoryPort;
import com.be_nuy_laundry.be_nuy_laundry.domain.model.UserSessionModel;
import com.be_nuy_laundry.be_nuy_laundry.infrastructure.internal.cache.SessionCacheService;
import com.be_nuy_laundry.be_nuy_laundry.infrastructure.internal.security.contract.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final SessionCacheService sessionCacheService;
    private final AuthRepositoryPort authRepositoryPort;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {

        String path = request.getServletPath();

        return path.startsWith("/api/auth")
                || path.startsWith("/public")
                || path.startsWith("/swagger")
                || path.equals("/actuator/health")
                || path.startsWith("/actuator/health/");
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        try {

            String authHeader = request.getHeader("Authorization");

            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                filterChain.doFilter(request, response);
                return;
            }

            String token = authHeader.substring(7);

            if (!jwtService.validateToken(token)) {
                unauthorized(response, "Invalid token");
                return;
            }

            String sessionId = jwtService.extractSessionId(token);

            UserSessionModel sessionModel = sessionCacheService.get(sessionId).orElse(null);

            if (sessionModel == null) {
                sessionModel = authRepositoryPort.findById(sessionId).orElse(null);

                if (sessionModel != null)
                    sessionCacheService.save(sessionModel);
            }

            if (sessionModel == null) {
                unauthorized(response, "Session not found");
                return;
            }

            if (!sessionModel.getIsActive()) {
                unauthorized(response, "Session revoked");
                return;
            }

            if (sessionModel.getExpiresAt() != null &&
                    sessionModel.getExpiresAt().isBefore(java.time.LocalDateTime.now())) {
                unauthorized(response, "Session expired");
                return;
            }

            String username = jwtService.extractUsername(token);
            String role = jwtService.extractRole(token);

            List<SimpleGrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_" + role));

            UsernamePasswordAuthenticationToken authenticationToken =
                    new UsernamePasswordAuthenticationToken(username, null, authorities);

            SecurityContextHolder.getContext().setAuthentication(authenticationToken);

        } catch (Exception ex) {
            log.error("JWT Filter error: {}", ex.getMessage());
            unauthorized(response, "Unauthorized");
            return;
        }

        filterChain.doFilter(request, response);
    }

    private void unauthorized(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(String.format("{\"message\": \"%s\"}", message));
    }

}
