package com.chatbot.rag;

import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;

import static dev.langchain4j.store.embedding.filter.MetadataFilterBuilder.metadataKey;

@Service
@Slf4j
@RequiredArgsConstructor
public class IngestionService {
    private final EmbeddingModel embeddingModel;
    private final EmbeddingStore<TextSegment> embeddingStore;
    private final PdfPageLoader pdfPageLoader;
    @Value("${rag.docs-folder}")
    private String docsFolder;

    @Value("${rag.chunk-size}")
    private int chunkSize;

    @Value("${rag.chunk-overlap}")
    private int chunkOverlap;

    public List<String> listDocuments() throws IOException {
        try (var files = Files.list(Path.of(docsFolder))) {
            return files.map(p -> p.getFileName().toString())
                    .filter(name -> name.endsWith(".pdf"))
                    .toList();
        }
    }

    public void ingestFolder(Path folder) throws IOException {


        try (var files = Files.list(folder)) {
            for (Path pdf : files.filter(p -> p.toString().endsWith(".pdf")).toList()) {
                ingestor().ingest(pdfPageLoader.loadPages(pdf));
                log.info("Ingested {}", pdf.getFileName());
            }
        }
    }

    public void reindex() throws IOException {
        embeddingStore.removeAll();
        ingestFolder(Path.of(docsFolder));
    }

    private EmbeddingStoreIngestor ingestor() {
        return EmbeddingStoreIngestor.builder()
                .documentSplitter(DocumentSplitters.recursive(chunkSize, chunkOverlap))
                .embeddingModel(embeddingModel)
                .embeddingStore(embeddingStore)
                .build();
    }

    public void addDocument(String fileName, InputStream content) throws IOException {
        Path target = Path.of(docsFolder).resolve(Path.of(fileName).getFileName());
        Files.copy(content, target, StandardCopyOption.REPLACE_EXISTING);
        // Remove old chunks of this file first, otherwise a second upload stores them twice
        embeddingStore.removeAll(metadataKey("file_name").isEqualTo(target.getFileName().toString()));
        ingestor().ingest(pdfPageLoader.loadPages(target));
        log.info("Uploaded and ingested {}", target.getFileName());
    }
}
