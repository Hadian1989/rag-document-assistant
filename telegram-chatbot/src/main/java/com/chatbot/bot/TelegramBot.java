package com.chatbot.bot;

import com.chatbot.rag.IngestionService;
import com.chatbot.rag.RagAnswer;
import com.chatbot.rag.RagService;
import com.chatbot.service.*;
import dev.langchain4j.service.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.IOException;
import java.util.Comparator;
import java.util.stream.Collectors;

@Slf4j
public class TelegramBot extends TelegramLongPollingBot {
    private final String botName;
    private final Assistant assistant;
    private final RagService ragService;
    private final IngestionService ingestionService;
    private final DeadlineExtractor deadlineExtractor;
    private final Long adminChatId;

    public TelegramBot(String botName,
                       String botToken,
                       Long adminChatId,
                       Assistant assistant, RagService ragService,
                       IngestionService ingestionService,
                       DeadlineExtractor deadlineExtractor) {
        super(botToken);
        this.botName = botName;
        this.assistant = assistant;
        this.ragService = ragService;
        this.ingestionService = ingestionService;
        this.adminChatId = adminChatId;
        this.deadlineExtractor = deadlineExtractor;
    }

    @Override
    public void onUpdateReceived(Update update) {

        if (update.hasMessage() && update.getMessage().hasText()) {
            var message = update.getMessage();
            String text = message.getText();
            String response;
            var chatId = message.getChatId();
            log.info("Message received: {}: {}", chatId, text);
            if (text.startsWith("/ask ")) {
                RagAnswer rag = ragService.ask(chatId, text.substring(5));
                response = rag.answer();
                if (!rag.sources().isEmpty()) {
                    response += "\n\n Quelle: " + String.join("; ", rag.sources());
                }
            } else if (text.startsWith("/docs")) {
                try {
                    response = "Dokumente:\n- " + String.join("\n- ", ingestionService.listDocuments());
                } catch (IOException e) {
                    log.error("Could not list documents!", e);
                    response = "Fehler beim Lesen der Dokumente";
                }
            } else if (text.equals("/reindex")) {
                if (!chatId.equals(adminChatId)) {
                    response = "Nur der Admin darf das";
                } else {
                    try {
                        ingestionService.reindex();
                        response = "Dokumente neu eingelesen.";
                    } catch (IOException e) {
                        log.error("Reindex failed!", e);
                        response = "Fehler beim Neu-Einlesen.";
                    }
                }

            } else if (text.startsWith("/fristen ")) {
                DeadlineList list = deadlineExtractor.extract(text.substring(9));
                if (list.deadlines() == null || list.deadlines().isEmpty()) {
                    response = "Keine Fristen gefunden!";
                } else {
                    response = list.deadlines().stream()
                            .map(d -> "📅 " + (d.date() != null ? d.date() + ": " : "") + d.title())
                            .collect(Collectors.joining("\n"));

                    response = list.deadlines().stream()
                            .sorted(Comparator.comparing(Deadline::date, Comparator.nullsLast(Comparator.naturalOrder())))
                            .map(d -> "📅 " + (d.date() != null ? d.date() + ": " : "") + d.title())
                            .collect(Collectors.joining("\n"));
                }

            } else {
                response = assistant.chat(chatId, message.getText());
            }
            try {
                log.info("Sending response: {}", response);
                execute(new SendMessage(chatId.toString(), response));
            } catch (TelegramApiException e) {
                log.error("Execution during telegram api: {}", e.getMessage());
            }
        } else {
            log.info("update has no text: {}", update);
        }

    }

    @Override
    public String getBotUsername() {
        return this.botName;
    }
}
