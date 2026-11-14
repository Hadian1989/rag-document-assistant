package com.chatbot.rag;

import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Service
@Slf4j
@RequiredArgsConstructor
public class IngestionService {
    private final EmbeddingModel embeddingModel;
    private final EmbeddingStore<TextSegment> embeddingStore;
    private final PdfPageLoader pdfPageLoader;

    public void ingestFolder(Path folder) throws IOException {
        EmbeddingStoreIngestor ingestor = EmbeddingStoreIngestor.builder()
                .documentSplitter(DocumentSplitters.recursive(1000, 100))
                .embeddingModel(embeddingModel)
                .embeddingStore(embeddingStore)
                .build();

        try (var files = Files.list(folder)) {
            for (Path pdf : files.filter(p -> p.toString().endsWith(".pdf")).toList()) {
                ingestor.ingest(pdfPageLoader.loadPages(pdf));
                log.info("Ingested {}", pdf.getFileName());
            }
        }
    }
}
