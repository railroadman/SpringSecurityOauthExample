package com.example.demo.service;

import com.example.demo.entities.User;
import com.example.demo.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.UUID;

// Сервис именно для OIDC (Google)
@Service
@Slf4j
public class OidcAuthService extends org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService {
    private final UserRepository userRepository;

    public OidcAuthService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public org.springframework.security.oauth2.core.oidc.user.OidcUser loadUser(
            org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest userRequest) {

        var oidcUser = super.loadUser(userRequest); // подтянет ID Token (+ при необходимости UserInfo)

        try {
            String email = oidcUser.getEmail(); // или (String) oidcUser.getClaims().get("email")
            if (email != null && userRepository.findByEmail(email).isEmpty()) {
                User user = User.builder()
                        .id(UUID.randomUUID())
                        .email(email)
                        .authProvider(userRequest.getClientRegistration().getRegistrationId()) // "google"
                        .firstName((String) oidcUser.getClaims().get("given_name"))
                        .lastName((String) oidcUser.getClaims().get("family_name"))
                        .createdAt(OffsetDateTime.now())
                        .updatedAt(OffsetDateTime.now())
                        .build();
                userRepository.save(user);
            }
        } catch (Exception e) {
            log.error("Failed to persist OIDC user", e);
        }

        return oidcUser;
    }
}
