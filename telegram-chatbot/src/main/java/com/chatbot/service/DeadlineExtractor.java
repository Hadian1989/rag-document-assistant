package com.chatbot.service;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;

public interface DeadlineExtractor {
    @SystemMessage("""
            Finde alle Fristen und Termine in den bereitgestellten Informationen.
            Nutze NUR diese Informationen.
            """)
    DeadlineList extract(@UserMessage String topic);
}
