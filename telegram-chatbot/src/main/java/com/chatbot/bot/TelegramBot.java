package com.chatbot.bot;

import com.chatbot.service.Assistant;
import com.chatbot.service.DocAssistant;
import dev.langchain4j.service.Result;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.stream.Collectors;

@Slf4j
public class TelegramBot extends TelegramLongPollingBot {
    private final String botName;
    private final Assistant assistant;
    private final DocAssistant docAssistant;

    public TelegramBot(String botName, String botToken, Assistant assistant, DocAssistant docAssistant) {
        super(botToken);
        this.botName = botName;
        this.assistant = assistant;
        this.docAssistant = docAssistant;
    }

    @Override
    public void onUpdateReceived(Update update) {

        if (update.hasMessage() && update.getMessage().hasText()) {
            var message = update.getMessage();
            String text = message.getText();
            String response;
            var chatId = message.getChatId();
            log.info("Message received: {}", message.getText());
            if (text.startsWith("/ask ")) {
                Result<String> result = docAssistant.chat(chatId, text.substring(5));
                String sources = result.sources().stream()
                        .map(c -> c.textSegment().metadata())
                        .map(m -> m.getString("file_name") + ", S. " + m.getInteger("page"))
                        .distinct()
                        .collect(Collectors.joining("; "));
                response = result.content();
                if (!sources.isBlank() && !response.contains("nicht in meinen Dokumenten")) {
                    response += "\n\n Quelle: " + sources;
                }
            } else {
                response = assistant.chat(chatId, message.getText());
            }
            try {
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
