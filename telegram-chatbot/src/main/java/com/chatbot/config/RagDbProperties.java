package com.chatbot.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "rag.db")
public record RagDbProperties(
        String host,
        int port,
        String database,
        String user,
        String password
) {}
