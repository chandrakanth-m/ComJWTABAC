package com.example.springbootapp.service;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.ollama.OllamaChatModel;
import dev.langchain4j.model.ollama.OllamaEmbeddingModel;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.rag.query.Query;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

//search with Intent and filters
//Hybrid search :: Semantic similarity (vector) + keyword match (exact intent)
@Service
public class RagService {
    private  OllamaChatModel chatModel;
    private  EmbeddingStoreContentRetriever retriever;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    OllamaEmbeddingModel embeddingModel = null;

    public RagService(@Value("${ollama.base-url}") String baseUrl,
                      @Value("${ollama.model}") String model,
                      @Value("${ollama.embedding.model}") String embeddingModelName)  {
        this.chatModel = chatModel;
        this.retriever = retriever;
       try {
           // Initialize chat model
           this.chatModel = OllamaChatModel.builder().baseUrl(baseUrl).temperature(0.0).modelName(model).build();

           // Initialize embedding model
           embeddingModel =
                   OllamaEmbeddingModel.builder()
                           .baseUrl(baseUrl)
                           .modelName(embeddingModelName)
                           .build();
       } catch (Exception e) {
           e.printStackTrace();
       }
    }

    /**
     * Loading multiple files
     * */
    @PostConstruct
    public void load() throws Exception {

        Path folder = Path.of("D:\\Work\\Sampl\\backend\\src\\main\\resources\\documents\\multiFiles");
        int docCount = 0;
        try {
            for(Path file: Files.list(folder).toList()){
                String fileName = file.getFileName().toString();
                String text = Files.readString(file);
                text = text.replace("Key Features:", "\n\nKey Features:\n");
                String category ;
                String product;

                if (fileName.equals("file1.txt")) {
                    category = "internet";
                    product = "basic";
                } else if (fileName.equals("file2.txt")) {
                    category = "ethernet";
                    product = "enterprise";
                } else {
                    category = "order";
                    product = "general";
                }

                List<TextSegment> segments =
                        DocumentSplitters.recursive(300, 50).split(Document.from(text));

                int chunkIndex = 0;

                for (TextSegment segment : segments) {

                    var embedding = embeddingModel.embed(segment.text()).content();
                    jdbcTemplate.update(
                            """
                            INSERT INTO embeddings 
                            (document_name, chunk_index, content, embedding, category, product, content_tsv)
                            VALUES (?, ?, ?, ?::vector, ?, ?, to_tsvector('english', ?))
                            """,
                            fileName,
                            chunkIndex++,
                            segment.text(),
                            toPgVector(embedding.vector()),
                            category,
                            product,
                            segment.text()
                    );
                }

                docCount++;
                System.out.println("Loaded: " + fileName);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        System.out.println("✅ Stored in Postgres");
    }

    private String toPgVector(float[] vector) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < vector.length; i++) {
            sb.append(vector[i]);
            if (i < vector.length - 1) sb.append(",");
        }
        sb.append("]");
        return sb.toString();
    }

    public String    ask(String question,String category    ) {

        List<String> results = null;

        if(null != category)
            results =  findRelevant(question,category);
        else
            results =  findRelevant(question);

        String context = String.join("\n",results);
        System.out.println("=== FINAL CONTEXT ===");
        System.out.println(context);

        String prompt = """
You are a strict extractor.

Task:
Find ONLY the value related to the requested field.

Rules:
- Match the exact field name from context
- Ignore similar fields
- "download speed" is NOT "upload speed"
- Return ONLY the matching value
- No explanation
- If not found return: NOT_FOUND

Context:
%s

Question:
%s
""".formatted(context, question);

//        System.out.println("======== FINAL PROMPT ========");
//        System.out.println(prompt);
//        System.out.println("================================");

            return chatModel.chat(prompt);
    }

    public List<String> findRelevant(String query) {

        var queryEmbedding = embeddingModel.embed(query).content();

        String vector = toPgVector(queryEmbedding.vector());
//        Add Threshold + limit starter
        List<String> results = jdbcTemplate.query(
                """
                SELECT content,document_name, embedding <-> ?::vector AS distance 
                FROM embeddings
              --  WHERE embedding <-> ? ::vector < 0.75 
                ORDER BY distance ASC
                LIMIT 5
                """,
                (rs, rowNum) -> {
                    double distance = rs.getDouble("distance");
                    String content = rs.getString("content");
                    String doc = rs.getString("document_name");

                    System.out.println("DIST: " + distance + " | DOC: " + doc);
//                    System.out.println("CONTENT: " + content);
                    System.out.println("-------------");

                    return content; // 👈 NO FILTER HERE
                },vector).stream().filter(Objects::nonNull).toList();

        String normalized = query.toLowerCase();

        if(normalized.contains("download")) {
            results = results.stream()
                    .filter(r -> r.toLowerCase().contains("download"))
                    .toList();
        }

        if (results.isEmpty()) {
            return Collections.singletonList("Not found in provided documents");
        }
        return results;
    }

    public List<String> findRelevant(String query, String category) {

            var queryEmbedding = embeddingModel.embed(query).content();
        String vector = toPgVector(queryEmbedding.vector());
        List<String> results = jdbcTemplate.query(
                """
                SELECT content,
                       document_name,
                       embedding <-> ?::vector AS distance,
                       ts_rank(content_tsv,
                               websearch_to_tsquery('english', ?)) AS keyword_score,
                       ((1 - (embedding <-> ?::vector)) * 0.7 +
                        ts_rank(content_tsv,
                                websearch_to_tsquery('english', ?)) * 0.3) AS final_score
                FROM embeddings
                WHERE category = ?
                ORDER BY final_score DESC
                LIMIT 5
                """,
                (rs, rowNum) -> {

                    double distance = rs.getDouble("distance");
                    double keywordScore = rs.getDouble("keyword_score");
                    double finalScore = rs.getDouble("final_score");

                    String content = rs.getString("content");
                    String doc = rs.getString("document_name");

                    System.out.println("DIST: " + distance +
                            " | KEY: " + keywordScore +
                            " | FINAL: " + finalScore +
                            " | DOC: " + doc);

                    /*if (finalScore > 0.4) {
                        return "[" + doc + "] " + content;
                    }

                    return null;*/
                    return content;
                },
                vector, query, vector, query, category
        ).stream().filter(Objects::nonNull).toList();
        String normalized = query.toLowerCase();

        if(normalized.contains("download")) {
            results = results.stream()
                    .filter(r -> r.toLowerCase().contains("download"))
                    .toList();
        }
        if (results.isEmpty()) {
            return Collections.singletonList("Not found in provided documents");
        }
        return results;
    }

    public void debugRetrieval(String question, String category) {

        System.out.println("=== DEBUG MODE (NO LLM) ===");

        // Step 1: Generate embedding
        var queryEmbedding = embeddingModel.embed(question).content();
        String vector = toPgVector(queryEmbedding.vector());

        System.out.println("QUESTION: " + question);
        System.out.println("VECTOR (first 5 values): ");
        for (int i = 0; i < 5; i++) {
            System.out.print(queryEmbedding.vector()[i] + ", ");
        }
        System.out.println("\n--------------------------");

        // Step 2: Query DB
        List<String> results = jdbcTemplate.query(
                """
                SELECT content, document_name,
                       embedding <-> ?::vector AS distance
                FROM embeddings
                WHERE (? IS NULL OR category = ?)
                ORDER BY distance ASC
                LIMIT 5
                """,
                (rs, rowNum) -> {
                    double distance = rs.getDouble("distance");
                    String content = rs.getString("content");
                    String doc = rs.getString("document_name");

//                    System.out.println("DIST: " + distance + " | DOC: " + doc);
                    System.out.printf("DIST: %.3f | DOC: %s\n", distance, doc);
                    System.out.println("CONTENT:\n" + content);
                    System.out.println("====================================");

                    return content;
                },
                vector, category, category
        );

        // Step 3: Build context
        String context = String.join("\n---\n", results);

        System.out.println("\n=== FINAL CONTEXT (USED BY LLM) ===");

        System.out.println(context);
    }

}
