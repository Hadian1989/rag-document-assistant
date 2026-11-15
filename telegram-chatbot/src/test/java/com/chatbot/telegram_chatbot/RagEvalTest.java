package com.chatbot.telegram_chatbot;

import com.chatbot.rag.IngestionService;
import com.chatbot.service.DocAssistant;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.langchain4j.service.Result;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(properties = "rag.docs-folder=../docs")
@Tag("eval")
@Slf4j
class RagEvalTest {

    record EvalQuestion(String question, List<String> mustContain,
                        String expectedFile, Integer expectedPage) {
    }

    @Autowired
    DocAssistant docAssistant;
    @Autowired
    IngestionService ingestionService;

    @Test
    void evaluate() throws Exception {
        ingestionService.reindex();
        List<EvalQuestion> questions = new ObjectMapper().readValue(
                new ClassPathResource("eval/questions.json").getInputStream(),
                new TypeReference<>() {
                });

        int answerOk = 0, answerTotal = 0;
        int sourceOk = 0;
        int refusalOk = 0, refusalTotal = 0;

        long memoryId = 1;
        for (EvalQuestion q : questions) {
            Result<String> result = docAssistant.chat(memoryId++, q.question());
            String answer = result.content().toLowerCase();

            boolean correct = q.mustContain().stream()
                    .allMatch(word -> answer.contains(word.toLowerCase()));

            if (q.expectedFile() == null) {
                // Frage NICHT in den Dokumenten → Ablehnung erwartet
                refusalTotal++;
                if (correct) refusalOk++;
            } else {
                answerTotal++;
                if (correct) answerOk++;

                boolean sourceFound = result.sources().stream()
                        .map(c -> c.textSegment().metadata())
                        .anyMatch(m -> q.expectedFile().equals(m.getString("file_name"))
                                && q.expectedPage().equals(m.getInteger("page")));
                if (sourceFound) sourceOk++;
            }

            if (!correct) {
                System.out.println("❌ " + q.question() + " → " + result.content());
            }
        }

        System.out.printf("Antwort korrekt:   %d/%d (%.0f %%)%n", answerOk, answerTotal, 100.0 * answerOk / answerTotal);
        System.out.printf("Quelle korrekt:    %d/%d (%.0f %%)%n", sourceOk, answerTotal, 100.0 * sourceOk / answerTotal);
        System.out.printf("Ablehnung korrekt: %d/%d (%.0f %%)%n", refusalOk, refusalTotal, 100.0 * refusalOk / refusalTotal);

        assertTrue(answerOk >= 0.8 * answerTotal, "Weniger als 80 % richtig!");
    }
}
