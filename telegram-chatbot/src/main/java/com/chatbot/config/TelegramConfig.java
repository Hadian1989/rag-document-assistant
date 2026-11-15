package com.chatbot.config;

import com.chatbot.bot.TelegramBot;
import com.chatbot.rag.IngestionService;
import com.chatbot.rag.RagAnswer;
import com.chatbot.rag.RagService;
import com.chatbot.service.Assistant;
import com.chatbot.service.DeadlineExtractor;
import com.chatbot.service.DocAssistant;
import org.springframework.beans.factory.annotation.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

@Slf4j
@Configuration
public class TelegramConfig {
    @Bean
    public TelegramBot telegramBot(@Value("${bot.name}") String botName,
                                   @Value("${bot.token}") String botToken,
                                   @Value("${bot.admin-chat-id}") Long adminChatId,
                                   Assistant assistant, RagService ragService,
                                   IngestionService ingestionService,
                                   DeadlineExtractor deadlineExtractor) {
        TelegramBot telegramBot = new TelegramBot(botName, botToken, adminChatId, assistant, ragService, ingestionService, deadlineExtractor);
        try {
            var telegramBotApi = new TelegramBotsApi(DefaultBotSession.class);
            telegramBotApi.registerBot(telegramBot);

        } catch (TelegramApiException e) {
            log.error("Execution during registration telegram api: {}", e.getMessage());
        }
        return telegramBot;
    }
}
