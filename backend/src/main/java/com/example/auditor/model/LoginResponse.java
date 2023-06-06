package com.example.auditor.model;

public class LoginResponse {

    private final Boolean success;

    private final String message;

    public LoginResponse(Boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public Boolean getSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }
}
