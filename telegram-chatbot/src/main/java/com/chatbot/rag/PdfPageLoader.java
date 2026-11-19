package com.chatbot.rag;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.Metadata;
import java.nio.file.Path;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
public class PdfPageLoader {

    public List<Document> loadPages(Path pdf) throws IOException {
        List<Document> pages = new ArrayList<>();
        try (PDDocument doc = PDDocument.load(pdf.toFile())) {
            PDFTextStripper stripper = new PDFTextStripper();
            for (int i = 1; i <= doc.getNumberOfPages(); i++) {
                stripper.setStartPage(i);
                stripper.setEndPage(i);
                String text = stripper.getText(doc);
                if (text.isBlank()) continue;
                Metadata meta = new Metadata()
                        .put("file_name", pdf.getFileName().toString())
                        .put("page", i);
                pages.add(Document.from(text, meta));
            }
        }
        return pages;
    }

    public int countPages(Path pdf) throws IOException {
        try (PDDocument doc = PDDocument.load(pdf.toFile())) {
            return doc.getNumberOfPages();
        }
    }
}
