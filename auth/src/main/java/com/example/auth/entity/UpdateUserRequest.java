package com.example.auth.entity;

import lombok.Data;

@Data
public class UpdateUserRequest {
    private String username;
    private String email;
}
