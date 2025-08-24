package com.example.demo.service;

import java.util.UUID;

import com.example.demo.entities.User;
import com.example.demo.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;

@Service
public class AuthService extends OidcUserService {

    private final UserRepository userRepository;
    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    public AuthService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public OidcUser loadUser(OidcUserRequest userRequest) throws OAuth2AuthenticationException {
        OidcUser oidcUser;
        try {
            oidcUser = super.loadUser(userRequest);
        } catch (OAuth2AuthenticationException e) {
            log.error("Failed to load user from OAuth2 provider", e);
            throw e;
        }

        String email = oidcUser.getEmail();
        if (email != null && userRepository.findByEmail(email).isEmpty()) {
            User user = User.builder()
                    .id(UUID.randomUUID())
                    .email(email)
                    .authProvider(userRequest.getClientRegistration().getRegistrationId())
                    .firstName(oidcUser.getGivenName())
                    .lastName(oidcUser.getFamilyName())
                    .build();
            userRepository.save(user);
        }

        return oidcUser;
    }
}
