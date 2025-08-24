package com.example.demo.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.oauth2Login;

import java.util.Optional;

import com.example.demo.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class OAuth2LoginIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @SpyBean
    AuthService authService;

    @MockBean
    UserRepository userRepository;

    @Test
    void oauth2LoginInvokesAuthServiceAndPersistsNewUser() throws Exception {
        when(userRepository.findByEmail("new@example.com")).thenReturn(Optional.empty());

        mockMvc.perform(oauth2Login().attributes(attrs -> {
            attrs.put("email", "new@example.com");
            attrs.put("given_name", "New");
            attrs.put("family_name", "User");
        }));

        verify(authService).loadUser(any());
        verify(userRepository).save(any());
    }
}
