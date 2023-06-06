package com.example.auditor.model;

import org.springframework.boot.jackson.JsonComponent;

@JsonComponent
public class LoginRequest {

    private String username;

    private String password;

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }
}