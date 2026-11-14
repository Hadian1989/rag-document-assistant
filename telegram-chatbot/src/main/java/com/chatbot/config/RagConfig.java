package com.chatbot.config;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.openai.OpenAiEmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.pgvector.PgVectorEmbeddingStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(RagDbProperties.class)
public class RagConfig {

    @Bean
    EmbeddingModel embeddingModel(@Value("${OPENAI_API_KEY}") String apiKey) {
        return OpenAiEmbeddingModel.builder()
                .apiKey(apiKey)
                .modelName("text-embedding-3-small")
                .build();
    }

    @Bean
    EmbeddingStore<TextSegment> embeddingStore(RagDbProperties db) {
        return PgVectorEmbeddingStore.builder()
                .host(db.host()).port(db.port())
                .database(db.database())
                .user(db.user()).password(db.password())
                .table("embeddings")
                .dimension(1536)
                .createTable(true)
                .build();
    }
}
