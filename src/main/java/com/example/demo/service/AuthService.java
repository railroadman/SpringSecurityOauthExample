package com.example.demo.service;

import java.util.UUID;

import com.example.demo.entities.User;
import com.example.demo.repository.UserRepository;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@Service
public class AuthService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    public AuthService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oauth2User = super.loadUser(userRequest);

        String email = oauth2User.getAttribute("email");
        if (email != null && userRepository.findByEmail(email).isEmpty()) {
            User user = User.builder()
                    .id(UUID.randomUUID())
                    .email(email)
                    .authProvider(userRequest.getClientRegistration().getRegistrationId())
                    .firstName(oauth2User.getAttribute("given_name"))
                    .lastName(oauth2User.getAttribute("family_name"))
                    .build();
            userRepository.save(user);
        }

        return oauth2User;
    }
}
