package api.userservice.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import api.userservice.model.User;
import api.userservice.repository.RoleRepository;
import api.userservice.repository.UserRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.oauth2.core.user.OAuth2User;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

// http://localhost:8080/oauth2/authorization/github

@Component
public class OAuth2AuthenticationSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler {

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
            Authentication authentication) throws ServletException, IOException {

        try {
            OAuth2User oauthUser = (OAuth2User) authentication.getPrincipal();
            String username = oauthUser.getAttribute("login");
            String email = oauthUser.getAttribute("email");

            User user = userRepository.findByUsername(username)
                    .orElseGet(() -> {
                        User newUser = new User();
                        newUser.setUsername(username);
                        newUser.setEmail(email);

                        roleRepository.findByName("ROLE_USER").ifPresent(role -> newUser.setRoles(Set.of(role)));

                        return userRepository.save(newUser);
                    });

            String accessToken = jwtUtils.generateAccessToken(user);
            String refreshToken = jwtUtils.generateRefreshToken(user);

            user.setRefreshToken(refreshToken);
            userRepository.save(user);

            JwtAuthenticationFilter.setTokenCookie(response, "accessToken", accessToken,
                    jwtUtils.getJwtExpirationMs() / 1000);
            JwtAuthenticationFilter.setTokenCookie(response, "refreshToken", refreshToken,
                    jwtUtils.getJwtRefreshExpirationMs() / 1000);


            List<GrantedAuthority> authorities = user.getRoles().stream()
                    .map(role -> new SimpleGrantedAuthority(role.getName()))
                    .collect(Collectors.toList());
            UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                    user, null, authorities);
            SecurityContextHolder.getContext().setAuthentication(authenticationToken);

            super.onAuthenticationSuccess(request, response, authentication);
        } catch (Exception e) {
            throw new ServletException("Error processing OAuth2 authentication", e);
        }
    }
}
