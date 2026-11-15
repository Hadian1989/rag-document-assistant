package com.chatbot.service;

import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.Result;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;

public interface DocAssistant {
    @SystemMessage("""
        Du beantwortest Fragen NUR auf Basis der bereitgestellten Dokumentauszüge.
        Wenn die Antwort nicht in den Auszügen steht, antworte:
        "Das steht nicht in meinen Dokumenten."
        Antworte kurz und auf Deutsch. Heute ist der {{current_date}}.
        """)
    Result<String> chat(@MemoryId Long chatId, @UserMessage String question);
}
