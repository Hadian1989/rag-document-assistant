package com.chatbot.rag;

import com.chatbot.service.DocAssistant;
import dev.langchain4j.service.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RagService {
    private final DocAssistant docAssistant;

    public RagAnswer ask(long memoryId, String question) {
        Result<String> result = docAssistant.chat(memoryId, question);
        String answer = result.content();
        if (answer.contains("nicht in meinen Dokumenten")) {
            return new RagAnswer(answer, List.of());
        }
        List<String> sources = result.sources().stream()
                .map(c -> c.textSegment().metadata())
                .map(m -> m.getString("file_name") + ", S. " + m.getInteger("page"))
                .distinct()
                .toList();
        return new RagAnswer(answer, sources);
    }
}