# Mini RAG System (Local) – Documentation

## 📌 Overview

This project implements a **Mini Retrieval-Augmented Generation (RAG) system** using:

* Java + Spring Boot
* LangChain4j
* Ollama (local LLM + embeddings)

The system retrieves relevant information from local documents and uses an LLM to generate context-aware answers.

---

## 🧠 Core Concept

RAG combines:

1. **Retrieval** → Find relevant text chunks
2. **Generation** → Use LLM to answer based on retrieved context

### Flow

```
User Query
   ↓
Convert to embedding
   ↓
Search similar text chunks
   ↓
Send context + query to LLM
   ↓
Generate answer
```

---

## 🏗️ Tech Stack

| Component  | Tool               |
| ---------- | ------------------ |
| Language   | Java 17            |
| Framework  | Spring Boot        |
| LLM        | Ollama (llama3)    |
| Embeddings | nomic-embed-text   |
| Library    | LangChain4j        |
| Storage    | InMemory (initial) |

---

## ⚙️ Setup Instructions

### 1. Install Ollama

Download from: https://ollama.com

Start server:

```bash
ollama serve
```

---

### 2. Pull Required Models

```bash
ollama pull llama3
ollama pull nomic-embed-text
```

---

### 3. Verify Ollama

```bash
curl http://localhost:11434
```

---

## 📂 Project Structure

```
src/main/java/
    ├── service/
    │     └── RagService.java
    ├── controller/
    │     └── RagController.java
    └── config/

src/main/resources/
    └── sample.txt
```

---

## 🧩 Implementation Steps

### Step 1: Load Document

```java
String text = Files.readString(Path.of("src/main/resources/sample.txt"));
```

---

### Step 2: Split Text

```java
List<TextSegment> segments =
    DocumentSplitters.recursive(300, 50).split(text);
```

---

### Step 3: Initialize Embedding Model

```java
EmbeddingModel embeddingModel = OllamaEmbeddingModel.builder()
        .baseUrl("http://localhost:11434")
        .modelName("nomic-embed-text")
        .build();
```

---

### Step 4: Store Embeddings

```java
InMemoryEmbeddingStore<TextSegment> store = new InMemoryEmbeddingStore<>();

for (TextSegment segment : segments) {
    store.add(embeddingModel.embed(segment.text()).content(), segment);
}
```

---

### Step 5: Query Processing

```java
String query = "What is this document about?";
var queryEmbedding = embeddingModel.embed(query).content();
var results = store.findRelevant(queryEmbedding, 3);
```

---

### Step 6: LLM Integration

```java
ChatLanguageModel model = OllamaChatModel.builder()
        .baseUrl("http://localhost:11434")
        .modelName("llama3")
        .build();
```

---

### Step 7: Prompt Construction

```java
String context = results.stream()
        .map(r -> r.embedded().text())
        .reduce("", (a, b) -> a + "\n" + b);

String prompt = """
Answer the question based only on the context below:

Context:
%s

Question:
%s
""".formatted(context, query);
```

---

### Step 8: Generate Response

```java
String response = model.generate(prompt);
System.out.println(response);
```

---

## 🧪 Example Output

**Input Query:**

```
What is this document about?
```

**Output:**

```
The document explains...
```

---

## ⚠️ Common Issues

### 1. Ollama not running

```bash
ollama serve
```

### 2. Model not found

```bash
ollama pull llama3
ollama pull nomic-embed-text
```

### 3. Port issues

Ensure:

```
http://localhost:11434
```

---

## 🚀 Future Enhancements

### Phase 2

* REST API (Spring Boot controller)
* Multi-document ingestion

### Phase 3

* Persistent vector store:

    * PostgreSQL + pgvector
    * FAISS

### Phase 4

* UI (Angular / React)
* Chat history memory

---

## 📊 Key Learnings

* RAG separates knowledge retrieval from generation
* Embeddings enable semantic search
* Local models (Ollama) allow privacy + cost control

---

## 🧭 Summary

This project demonstrates a **fully local RAG pipeline**:

* No cloud dependency
* No API cost
* Fully controllable architecture

---

## ✅ Status

✔ Document ingestion
✔ Embedding generation
✔ Similarity search
✔ LLM response generation

---

## 🔚 End
