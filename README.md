# Telegram Chatbot with ChatGPT (Spring Boot + LangChain4j)

A Spring Boot application that connects a **Telegram bot** and a **REST API** to OpenAI's **GPT-4o mini** using [LangChain4j](https://github.com/langchain4j/langchain4j).

The bot remembers the conversation per user, and the API can turn free text into **structured Java objects**: you describe a book in plain words and get back a `BookModel` as JSON.

## Features

- 💬 **Telegram bot**: answers messages in a chat, using long polling
- 🧠 **Chat memory per user**: the last 10 messages are remembered for each user (`MessageWindowChatMemory`, `@MemoryId`)
- 🧾 **Structured output**: extracts title, author, genre, publication date and a short description from text into a Java object
- 🎯 **System prompts** with LangChain4j AI Services (`@SystemMessage`, `@UserMessage`, `@V`)
- 🌐 **REST API** for testing without Telegram

## Tech stack

Java 17 · Spring Boot 3.3 · LangChain4j 0.34 · OpenAI GPT-4o mini · TelegramBots 6.9 · Lombok · Maven

## How it works

```
Telegram user ──► TelegramBot ──┐
                                ├──► Assistant (LangChain4j AI Service) ──► OpenAI GPT-4o mini
REST client ──► GenerativeController ──► GenAIService ──┘
```

- `AIConfig` creates the `ChatLanguageModel` and the `Assistant` with chat memory.
- `Assistant` is an interface. LangChain4j generates the implementation from the annotations.
- `GenAIServiceImp` contains the business logic, for example the list of allowed book genres.

## Getting started

### 1. Prerequisites

- Java 17+
- A Telegram bot token from [@BotFather](https://t.me/BotFather)

### 2. Configuration

In `telegram-chatbot/src/main/resources/application.properties`:

```properties
bot.name=YOUR_BOT_NAME
bot.token=YOUR_BOT_TOKEN
```

The project uses LangChain4j's `demo` API key for OpenAI, so you can try it without your own key. For real use, replace it in `AIConfig` with your own OpenAI key.

### 3. Run

```bash
cd telegram-chatbot
./mvnw spring-boot:run
```

The app starts on **http://localhost:8090**.

## API examples

**Chat**

```bash
curl -X POST http://localhost:8090/api/chat \
  -H "Content-Type: application/json" \
  -d '{"question": "What is Spring Boot?", "userId": 1}'
```

Example response:

```json
{ "response": "Spring Boot is a framework that makes it easy to build ..." }
```

**Extract a book from text**

```bash
curl -X POST http://localhost:8090/api/chat/book \
  -H "Content-Type: application/json" \
  -d '{"question": "The Hobbit is a fantasy novel by J.R.R. Tolkien, published in 1937, about Bilbo who goes on an adventure with dwarves."}'
```

Example response (the exact text comes from the model):

```json
{
  "title": "The Hobbit",
  "author": "J.R.R. Tolkien",
  "genre": "Fantasy",
  "dateOfPublication": "1937-09-21",
  "description": "Bilbo Baggins joins a group of dwarves on an adventure."
}
```

## What I learned

- How to integrate an LLM into a Spring Boot application with LangChain4j
- Prompting with system messages and template variables
- Keeping separate chat memory for each user
- Getting reliable structured data (Java objects) from an LLM instead of free text

## Next steps

- Read the OpenAI API key from an environment variable
- Add tests for the service layer
- Add a vector store for retrieval-augmented generation (RAG)
