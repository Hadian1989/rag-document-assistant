package com.chatbot.rag;

import java.util.List;

public record RagAnswer(String answer, List<String> sources) {
}
