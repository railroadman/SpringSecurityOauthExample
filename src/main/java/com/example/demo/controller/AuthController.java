package com.example.demo.controller;

import com.example.demo.entities.User;
import com.example.demo.models.dto.RegisterDto;
import com.example.demo.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder encoder;

    public AuthController(UserRepository userRepository, PasswordEncoder encoder) {
        this.userRepository = userRepository;
        this.encoder = encoder;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterDto dto) {
        if (userRepository.findByEmail(dto.email()).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Email already in use");
        }
        var user = User.builder()
                .id(UUID.randomUUID())
                .email(dto.email())
                .firstName(dto.firstName())
                .lastName(dto.lastName())
                .authProvider("local")
                .passwordHash(encoder.encode(dto.password())) // ВАЖНО: хэшируем
                .build();
        userRepository.save(user);
        return ResponseEntity.ok().build();
    }
}

