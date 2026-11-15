package com.chatbot.controller;

import com.chatbot.controller.dto.QuestionDto;
import com.chatbot.rag.IngestionService;
import com.chatbot.rag.RagAnswer;
import com.chatbot.rag.RagService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("api/rag")
public class RagController {
    private final RagService ragService;
    private final IngestionService ingestionService;

    @PostMapping("/ask")
    public RagAnswer ask(@RequestBody QuestionDto q) {
        return ragService.ask(q.sessionId(), q.question());
    }

    @PostMapping(value = "/documents", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> upload(@RequestPart("file") MultipartFile file) {
        try {
            String name = file.getOriginalFilename();
            if (name == null || !name.toLowerCase().endsWith(".pdf")) {
                return ResponseEntity.badRequest().body("Nur PDF-Dateien erlaubt!");
            }
            ingestionService.addDocument(name, file.getInputStream());
            return ResponseEntity.ok("Hochgeladen: " + name);
        } catch (IOException e) {
            log.error("Error beim import: ", e);
            return ResponseEntity.badRequest().body("Fehler beim Import!");
        }

    }
}
