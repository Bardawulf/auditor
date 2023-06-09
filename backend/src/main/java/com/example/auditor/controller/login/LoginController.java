package com.example.auditor.controller.login;

import com.example.auditor.domain.user.User;
import com.example.auditor.model.LoginRequest;
import com.example.auditor.model.LoginResponse;
import com.example.auditor.repository.user.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class LoginController {

    private final UserRepository userRepository;

    public LoginController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @PostMapping("/login")
    @ResponseBody
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest loginRequest) {
        User user = userRepository.findByUsername(loginRequest.getUsername());
        if (user != null && new BCryptPasswordEncoder().matches(loginRequest.getPassword(), user.getPassword())) {
            return ResponseEntity.ok(new LoginResponse(true, "Authentication successful"));
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new LoginResponse(false, "Authentication failed"));
        }
    }
}
