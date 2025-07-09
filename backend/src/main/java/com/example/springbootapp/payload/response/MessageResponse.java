package com.example.springbootapp.payload.response;

import lombok.Data;
import lombok.AllArgsConstructor;

@Data
@AllArgsConstructor
public class MessageResponse {
    private String message;
}