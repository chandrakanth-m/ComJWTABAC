package com.example.springbootapp.dto;

import lombok.Data;

@Data
public class QuestionRequest {
    private String question;
    private String document; // optional
}
