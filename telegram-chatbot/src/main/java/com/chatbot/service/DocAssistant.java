package com.chatbot.service;

import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.UserMessage;

public interface DocAssistant {
    String chat(@MemoryId long chatId, @UserMessage String userMessage);
}
