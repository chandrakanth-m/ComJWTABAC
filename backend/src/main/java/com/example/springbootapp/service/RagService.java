package com.example.springbootapp.service;

    import dev.langchain4j.data.document.Document;
    import dev.langchain4j.data.document.DocumentParser;
    import dev.langchain4j.data.document.loader.ClassPathDocumentLoader;
    import dev.langchain4j.data.document.loader.FileSystemDocumentLoader;
    import dev.langchain4j.data.document.splitter.DocumentSplitters;
    import dev.langchain4j.data.segment.TextSegment;
    import dev.langchain4j.model.ollama.OllamaChatModel;
import dev.langchain4j.model.ollama.OllamaEmbeddingModel;
    import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
    import dev.langchain4j.rag.query.Query;
    import dev.langchain4j.store.embedding.EmbeddingStore;
    import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
    import org.springframework.beans.factory.annotation.Value;
    import org.springframework.core.io.ClassPathResource;
    import org.springframework.stereotype.Service;

    import java.io.FileInputStream;
    import java.io.InputStream;
    import java.nio.file.Files;
    import java.nio.file.Path;
    import java.util.List;

@Service
public class RagService {
    private  OllamaChatModel chatModel;
    private  EmbeddingStoreContentRetriever retriever;

    public RagService(@Value("${ollama.base-url}") String baseUrl,
                      @Value("${ollama.model}") String model,
                      @Value("${ollama.embedding.model}") String embeddingModelName)  {
        this.chatModel = chatModel;
        this.retriever = retriever;
       try {
           // Initialize chat model
           this.chatModel = OllamaChatModel.builder().baseUrl(baseUrl).modelName(model).build();

           // Initialize embedding model
           OllamaEmbeddingModel embeddingModel =
                   OllamaEmbeddingModel.builder()
                           .baseUrl(baseUrl)
                           .modelName(embeddingModelName)
                           .build();

           // Load document
           // Step 4: Split into chunks
           ClassPathResource resource = new ClassPathResource("documents/sample.txt");
           String text = new String(resource.getInputStream().readAllBytes());
           Document document = Document.from(text);

           List<TextSegment> segments = DocumentSplitters
                   .recursive(500, 100)
                   .split(document);
           // Create in-memory embedding store
           EmbeddingStore<TextSegment> embeddingStore =
                   new InMemoryEmbeddingStore<>();

           // Generate and store embeddings
           embeddingStore.addAll(
                   embeddingModel.embedAll(segments).content(),
                   segments
           );

           // Create retriever
           this.retriever =    EmbeddingStoreContentRetriever.builder()
                   .embeddingStore(embeddingStore)
                   .embeddingModel(embeddingModel)
                   .maxResults(3)
                   .build();
       } catch (Exception e) {
           e.printStackTrace();
       }
    }

    public String    ask(String question) {
        var retrievedContents = retriever.retrieve(Query.from(question));

        StringBuilder context = new StringBuilder();
        retrievedContents.forEach(content ->
                context.append(content).append("\n"));

        String prompt = """
                Answer the question based only on the context below.
                If the answer is not in the context, say "I don't know".

                Context:
                %s

                Question: %s
                """.formatted(context, question);

        return chatModel.chat(prompt);
    }
}
