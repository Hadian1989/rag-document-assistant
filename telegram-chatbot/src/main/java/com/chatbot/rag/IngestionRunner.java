package com.chatbot.rag;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.nio.file.Path;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "rag.ingest-on-startup", havingValue = "true")
public class IngestionRunner implements ApplicationRunner {

    private final IngestionService ingestionService;

    @Value("${rag.docs-folder}")
    private String docsFolder;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        Path folder = Path.of(docsFolder).toAbsolutePath();
        log.info("Ingesting folder {}", folder);
        ingestionService.ingestFolder(folder);
    }
}
