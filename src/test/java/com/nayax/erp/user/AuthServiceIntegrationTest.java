package com.nayax.erp.user;

import com.example.demo.entities.User;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
class AuthServiceIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DataSource dataSource;

    @Test
    void loadUserPersistsUserToSqlite() {
        Map<String, Object> attributes = Map.of(
                "email", "test@example.com",
                "given_name", "Test",
                "family_name", "User"
        );
        OAuth2User oauth2User = new DefaultOAuth2User(
                List.of(new SimpleGrantedAuthority("ROLE_USER")),
                attributes,
                "email"
        );

        ClientRegistration registration = ClientRegistration.withRegistrationId("google")
                .clientId("client")
                .clientSecret("secret")
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .redirectUri("http://localhost")
                .authorizationUri("http://auth")
                .tokenUri("http://token")
                .userInfoUri("http://userinfo")
                .userNameAttributeName("email")
                .build();

        OAuth2AccessToken token = new OAuth2AccessToken(
                OAuth2AccessToken.TokenType.BEARER,
                "token",
                Instant.now(),
                Instant.now().plusSeconds(60)
        );
        OAuth2UserRequest userRequest = new OAuth2UserRequest(registration, token);

        AuthService authService = new AuthService(userRepository) {
            @Override
            public OAuth2User loadUser(OAuth2UserRequest req) {
                OAuth2User user = oauth2User;
                String email = user.getAttribute("email");
                if (email != null && userRepository.findByEmail(email).isEmpty()) {
                    User entity = User.builder()
                            .id(UUID.randomUUID())
                            .email(email)
                            .authProvider(req.getClientRegistration().getRegistrationId())
                            .firstName(user.getAttribute("given_name"))
                            .lastName(user.getAttribute("family_name"))
                            .build();
                    userRepository.save(entity);
                }
                return user;
            }
        };

        authService.loadUser(userRequest);

        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        Map<String, Object> row = jdbcTemplate.queryForMap(
                "SELECT email, auth_provider, first_name, last_name FROM user WHERE email = ?",
                "test@example.com"
        );

        assertEquals("test@example.com", row.get("email"));
        assertEquals("google", row.get("auth_provider"));
        assertEquals("Test", row.get("first_name"));
        assertEquals("User", row.get("last_name"));
    }
}
