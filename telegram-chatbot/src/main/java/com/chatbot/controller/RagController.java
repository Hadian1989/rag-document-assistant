package com.chatbot.controller;

import com.chatbot.controller.dto.ChatRequest;
import com.chatbot.controller.dto.ChatResponse;
import com.chatbot.controller.dto.QuestionDto;
import com.chatbot.model.BookModel;
import com.chatbot.rag.RagAnswer;
import com.chatbot.rag.RagService;
import com.chatbot.service.GenAIService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/rag")
public class RagController {
    private final RagService ragService;

  @PostMapping("/ask")
    public RagAnswer ask(@RequestBody QuestionDto q){
        return ragService.ask(q.sessionId(), q.question());
  }
}
