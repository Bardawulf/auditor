package com.example.auditor.controller.login;

import com.example.auditor.model.LoginRequest;
import com.example.auditor.model.LoginResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class LoginController {
    private static final String login = "test";
    private static final String password = "1234";

    @PostMapping("/login")
    @ResponseBody
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest loginRequest) {

        if (login.equals(loginRequest.getUsername()) && password.equals(loginRequest.getPassword())) {
            return ResponseEntity.ok(new LoginResponse(true, "Authentication successful"));
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new LoginResponse(false, "Authentication failed"));
        }
    }
}
