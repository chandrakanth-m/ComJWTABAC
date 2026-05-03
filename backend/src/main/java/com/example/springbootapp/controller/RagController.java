package com.example.springbootapp.controller;

import com.example.springbootapp.dto.QuestionRequest;
import com.example.springbootapp.service.RagService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/rag")
public class RagController {

    private final RagService ragService;

    public RagController(RagService ragService) {
        this.ragService = ragService;
    }

   /* @GetMapping("/ask")
    public String ask(@RequestParam String question) {
        return ragService.ask(question);
    }*/

    @PostMapping("/ask")
    public String ask(@RequestBody QuestionRequest request) {

        if (request.getDocument() != null) {
            return ragService.ask(request.getQuestion(), request.getDocument());
        }

        return ragService.ask(request.getQuestion(),null    );
    }
}
