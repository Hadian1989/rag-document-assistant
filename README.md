# RAG Document Assistant (Telegram Bot + REST API)

A Spring Boot app that answers questions about **your own PDF documents** and gives **the source (file and page)** for each answer. If the answer is not in the documents, it says so instead of making something up.

![Demo](docs-assets/demo.gif)

**Example**

> 👤 `/ask How long do I have to submit my Master's thesis?`
> 🤖 Du hast nach der Anmeldung 26 Wochen Zeit.
> 📄 Quelle: Thesis guideline v07.pdf, S. 8

(The bot answers in German; the documents can be in any language.)

## Features

- 📚 **RAG with sources**: PDFs are read page by page, split into chunks, embedded and stored in **pgvector**. Each answer lists the file and page it came from.
- 🙅 **Honest refusal**: questions outside the documents get *"Das steht nicht in meinen Dokumenten."* ("That is not in my documents.")
- 🧾 **Structured output**: `/fristen <topic>` extracts deadlines and dates as Java objects (`record Deadline(String title, LocalDate date)`), sorted in Java.
- 🛠️ **Function calling**: the LLM calls Java methods (`@Tool`) for date math, for example "How many days until …?"
- 📊 **Evaluation suite**: a JUnit eval with 20 questions measures answer accuracy, source accuracy and refusal rate. I used it to choose the chunk size.
- 💬 **Telegram bot** with commands and chat memory per user
- 🌐 **REST API** to ask questions and upload new PDFs

## Architecture

```mermaid
flowchart LR
    subgraph Ingestion
        PDF[PDFs in docs/] --> L[PdfPageLoader<br/>page by page]
        UP[REST upload] --> L
        L --> S[Splitter<br/>1000 / 100]
        S --> E[OpenAI Embeddings<br/>text-embedding-3-small]
        E --> DB[(PostgreSQL<br/>+ pgvector)]
    end

    subgraph Question
        U[Telegram / REST] --> RS[RagService]
        RS --> DA[DocAssistant<br/>LangChain4j AI Service]
        DA --> R[ContentRetriever<br/>top 3 chunks]
        R --> DB
        DA --> LLM[OpenAI gpt-4o-mini]
        LLM <--> T[DateTools<br/>@Tool]
        LLM --> A[Answer + sources]
    end
```

## Tech Stack

Java 17 · Spring Boot 3.3 · LangChain4j 0.34 · OpenAI (gpt-4o-mini, text-embedding-3-small) · PostgreSQL + pgvector · Apache PDFBox · Docker Compose · JUnit 5 · TelegramBots 6.9

## Evaluation

The eval set has 20 questions: 16 whose answer is in the documents and 4 whose answer is not. Each question has expected keywords and, if it has an answer, the expected file and page.

| Chunk / Overlap | maxResults | Answer correct | Source correct | Refusal correct |
|---|---|---|---|---|
| **1000 / 100** | **3** | **94 %** | 94 % | 100 % |
| 1000 / 100 | 5 | 94 % | 94 % | 100 % |
| 500 / 50 | 5 | 88 % | 100 % | 100 % |
| 300 / 30 | 5 | 81 % | 88 % | 100 % |

**Findings**

- **Small chunks** find the right page more precisely, but they cut **tables** apart. The LLM then mixes up rows, for example the wrong exam duration or the wrong lecturer.
- **Large chunks** keep the context together, so the answers are better.
- **maxResults 3 vs. 5:** the same quality, with a shorter prompt, so it is cheaper and faster.
- **Refusal was 100 % in every setup**: no made-up answers for questions outside the documents.

➡️ Chosen setup: **chunk size 1000, overlap 100, maxResults 3**.

> ⚠️ **For local use only.** The REST API has no authentication (see [Known Limitations](#known-limitations)).

## Getting Started

### 1. Prerequisites

- Java 17+
- Docker Desktop
- An OpenAI API key
- A Telegram bot token from [@BotFather](https://t.me/BotFather)

### 2. Start the database

```bash
cd telegram-chatbot
docker compose up -d
```

### 3. Set environment variables

| Variable | Description |
|---|---|
| `OPENAI_API_KEY` | Your OpenAI API key |
| `TELEGRAM_BOT_NAME` | Bot username |
| `TELEGRAM_BOT_TOKEN` | Bot token from BotFather |
| `TELEGRAM_ADMIN_CHAT_ID` | Your Telegram chat ID (only this chat may use `/reindex`) |
| `RAG_DOCSFOLDER` | Optional. Path to the PDF folder (`rag.docs-folder`). Use `../docs` when you start the app from `telegram-chatbot/` |

### 4. Add documents and run

Put your PDFs into `docs/`. The PDFs need real text; scanned PDFs without a text layer do not work.

```bash
./mvnw spring-boot:run
```

The app starts on **http://localhost:8090**. To load the PDFs, send `/reindex` in Telegram, or start the app once with `rag.ingest-on-startup=true`.

## Usage

### Telegram commands

| Command | Description |
|---|---|
| `/ask <question>` | Ask a question about the documents (RAG, with sources) |
| `/fristen <topic>` | List the deadlines and dates for a topic (structured output) |
| `/docs` | List the loaded documents |
| `/reindex` | Read the `docs/` folder again (admin only) |
| any other message | Normal chat without documents |

### REST API

**Ask a question**

```bash
curl -X POST http://localhost:8090/api/rag/ask \
  -H "Content-Type: application/json" \
  -d '{"sessionId": 1, "question": "When is the Applied Embedded Systems exam?"}'
```

```json
{
  "answer": "Die Prüfung findet am 20.06.2026 von 10:15 bis 15:50 Uhr statt.",
  "sources": ["ESE exam schedule summerterm 2026.pdf, S. 1"]
}
```

**Upload a PDF**

```bash
curl -F "file=@my-document.pdf" http://localhost:8090/api/rag/documents
```

### Run the evaluation

The eval calls the real OpenAI API, so it has the tag `@Tag("eval")` and is not part of the normal test run. Stop the app first, because the test starts its own Telegram bot.

```bash
./mvnw test -Dgroups=eval -DexcludedGroups=
```

## CI

GitHub Actions runs `./mvnw test` on every push, with a pgvector service container. The eval tests are excluded there (`excludedGroups=eval` in `pom.xml`).

## Configuration

All RAG settings are in `application.properties`:

```properties
rag.chunk-size=1000
rag.chunk-overlap=100
rag.max-results=3
rag.min-score=0.6
```

## Known Limitations

- 🔓 **No authentication on the REST API**: anyone who can reach it can upload PDFs and cause OpenAI costs. **For local use only.** Do not expose it to the internet.
- 🖼️ **No OCR**: scanned PDFs cannot be read.
- 📋 **Tables**: a table that is split across chunks can lead to mixed-up answers (see Evaluation).
- 🧪 **Simple eval check**: answers are checked with keyword matching. Short numbers like `"20"` can match by accident (for example inside `2026`).

## What I Learned

- **Chunk size is a trade-off.** Small chunks give precise retrieval but lose context. My eval numbers showed this clearly.
- **The user message is also the retrieval query.** Put instructions into the `@SystemMessage`; otherwise they dilute the search. Moving them there fixed my `/fristen` command.
- **Measure, don't guess.** The eval confirmed that chunk size 1000 is the best choice, and it showed that `maxResults` 3 is as good as 5 but cheaper. I could not have seen that by testing a few questions by hand.
- **The LLM only knows what you tell it.** It did not know today's date (fixed with `{{current_date}}`), and it misread a negative tool result until I explained it in the `@Tool` description.
- **Debug with logs.** Request logging showed me whether the right chunks reached the LLM. That told me whether a wrong answer came from retrieval or from generation.
