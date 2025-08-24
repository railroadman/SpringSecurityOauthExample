package com.example.demo.config;

import com.example.demo.service.AuthService;
import com.example.demo.service.OidcAuthService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;

@Configuration
public class SecurityConfig {

    @Bean
    SecurityFilterChain security(HttpSecurity http,
                                 AuthService oauth2AuthService,
                                 OidcAuthService oidcAuthService) throws Exception {
        var failure = new SimpleUrlAuthenticationFailureHandler("/login?error");
        http
                .authorizeHttpRequests(a -> a
                        .requestMatchers("/", "/css/**", "/js/**", "/images/**").permitAll()
                        .anyRequest().authenticated())
                .oauth2Login(o -> o
                        .userInfoEndpoint(u -> u
                                .userService(oauth2AuthService)
                                .oidcUserService(oidcAuthService))
                        .failureHandler((req, res, ex) -> {
                            // тут будет точная причина: invalid_client, invalid_state_parameter, invalid_id_token и т.д.
                            org.slf4j.LoggerFactory.getLogger("OAuth2").error("OAuth2 login failed", ex);
                            failure.onAuthenticationFailure(req, res, ex);
                        })
                        .defaultSuccessUrl("/", true))
                .logout(l -> l.logoutSuccessUrl("/").invalidateHttpSession(true).clearAuthentication(true).deleteCookies("JSESSIONID"));
        return http.build();
    }
}
