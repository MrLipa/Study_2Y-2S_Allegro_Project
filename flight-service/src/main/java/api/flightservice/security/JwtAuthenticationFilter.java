package api.flightservice.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import api.flightservice.model.User;
import api.flightservice.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private UserRepository userRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        try {
            List<String> userRoles = null;
            Long userId = null;
            String accessToken = getTokenFromCookie(request, "accessToken");

            if (accessToken != null && jwtUtils.validateJwtToken(accessToken)) {
                userId = Long.parseLong(jwtUtils.getUserIdFromJwtToken(accessToken));
                userRoles = jwtUtils.getUserRolesFromJwtToken(accessToken);
            } else {
                // Access token is either missing or expired
                String refreshToken = getTokenFromCookie(request, "refreshToken");

                if (refreshToken != null && jwtUtils.validateJwtToken(refreshToken)) {
                    userId = Long.parseLong(jwtUtils.getUserIdFromJwtToken(refreshToken));
                    User user = userRepository.findById(userId).orElse(null);

                    if (user != null) {
                        userRoles = user.getRoles().stream()
                            .map(role -> role.getName())
                            .collect(Collectors.toList());
                        String newAccessToken = jwtUtils.generateAccessToken(user);
                        setTokenCookie(response, "accessToken", newAccessToken, jwtUtils.getJwtExpirationMs()/1000);
                    }
                }
            }
            
            if (userRoles != null) {
                List<GrantedAuthority> authorities = userRoles.stream()
                    .map(role -> new SimpleGrantedAuthority(role))
                    .collect(Collectors.toList());

                UserDetails userDetails = new org.springframework.security.core.userdetails.User(String.valueOf(userId), "", authorities);

                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    userDetails, null, userDetails.getAuthorities());

                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                SecurityContextHolder.getContext().setAuthentication(authentication);
            }

        } catch (Exception e) {
            logger.error("Cannot set user authentication: {}", e);
        }

        filterChain.doFilter(request, response);
    }

    private String getTokenFromCookie(HttpServletRequest request, String cookieName) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookieName.equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

    public static void setTokenCookie(HttpServletResponse response, String cookieName, String token, int maxAgeSeconds) {
        Cookie tokenCookie = new Cookie(cookieName, token);
        tokenCookie.setPath("/");
        tokenCookie.setHttpOnly(true);
        tokenCookie.setMaxAge(maxAgeSeconds);
        response.addCookie(tokenCookie);
    }

    public static void clearTokenCookie(HttpServletResponse response, String cookieName) {
        Cookie cookie = new Cookie(cookieName, null);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setMaxAge(0);
        response.addCookie(cookie);
    }
}
