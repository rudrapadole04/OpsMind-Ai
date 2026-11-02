package com.opsmind.ai.service.rag;

import com.opsmind.ai.model.entity.DocumentChunkEntity;
import com.opsmind.ai.repository.DocumentChunkRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class VectorSearchService {

    private static final Logger log = LoggerFactory.getLogger(VectorSearchService.class);
    private static final Pattern WORD_SPLIT = Pattern.compile("[^a-zA-Z0-9]+");

    private static final Set<String> STOP_WORDS = Set.of(
            "a", "about", "above", "after", "again", "against", "all", "am", "an", "and", "any", "are", "aren't",
            "as", "at", "be", "because", "been", "before", "being", "below", "between", "both", "but", "by",
            "can", "can't", "cannot", "could", "did", "do", "does", "doing", "don't", "down", "during", "each",
            "few", "for", "from", "further", "had", "has", "have", "having", "he", "her", "here", "hers", "herself",
            "him", "himself", "his", "how", "i", "if", "in", "into", "is", "it", "its", "itself", "let's", "me",
            "more", "most", "my", "myself", "no", "nor", "not", "of", "off", "on", "once", "only", "or", "other",
            "ought", "our", "ours", "ourselves", "out", "over", "own", "same", "she", "should", "so", "some",
            "such", "than", "that", "the", "their", "theirs", "them", "themselves", "then", "there", "these",
            "they", "this", "those", "through", "to", "too", "under", "until", "up", "very", "was", "we", "were",
            "what", "when", "where", "which", "while", "who", "whom", "why", "with", "would", "you", "your", "yours"
    );

    private final DocumentChunkRepository chunkRepository;
    private final int maxRetrievedChunks;

    public VectorSearchService(
            DocumentChunkRepository chunkRepository,
            @Value("${opsmind.rag.max-retrieved-chunks:5}") int maxRetrievedChunks) {
        this.chunkRepository = chunkRepository;
        this.maxRetrievedChunks = maxRetrievedChunks;
    }

    public List<ScoredChunk> search(String query) {
        List<DocumentChunkEntity> allChunks = chunkRepository.findAll();
        if (allChunks.isEmpty()) {
            log.warn("Vector search invoked but chunk repository is empty.");
            return Collections.emptyList();
        }

        List<String> queryTerms = tokenize(query);
        if (queryTerms.isEmpty()) {
            return Collections.emptyList();
        }

        int totalDocs = allChunks.size();

        // 1. Compute Document Frequency (DF) across all chunks
        Map<String, Integer> docFrequency = new HashMap<>();
        List<List<String>> tokenizedChunks = new ArrayList<>(totalDocs);

        for (DocumentChunkEntity chunk : allChunks) {
            List<String> tokens = tokenize(chunk.getContent());
            tokenizedChunks.add(tokens);
            Set<String> uniqueTokens = new HashSet<>(tokens);
            for (String t : uniqueTokens) {
                docFrequency.put(t, docFrequency.getOrDefault(t, 0) + 1);
            }
        }

        // 2. Compute IDF for query terms
        Map<String, Double> idfMap = new HashMap<>();
        for (String term : queryTerms) {
            int df = docFrequency.getOrDefault(term, 0);
            double idf = Math.log(1.0 + (double) totalDocs / (df + 1.0)) + 1.0;
            idfMap.put(term, idf);
        }

        // 3. Build Query Vector
        Map<String, Double> queryVector = buildTfIdfVector(queryTerms, idfMap);

        // 4. Calculate Cosine Similarity & Boosts for each chunk
        List<ScoredChunk> scoredChunks = new ArrayList<>();
        for (int i = 0; i < totalDocs; i++) {
            DocumentChunkEntity chunk = allChunks.get(i);
            List<String> chunkTokens = tokenizedChunks.get(i);
            if (chunkTokens.isEmpty()) {
                continue;
            }

            Map<String, Double> chunkVector = buildTfIdfVector(chunkTokens, idfMap);
            double similarity = computeCosineSimilarity(queryVector, chunkVector);

            // Compute Term Overlap Ratio
            int matches = 0;
            Set<String> chunkTokenSet = new HashSet<>(chunkTokens);
            for (String q : queryTerms) {
                if (chunkTokenSet.contains(q)) {
                    matches++;
                }
            }
            double overlapRatio = (double) matches / queryTerms.size();

            // Phrase and continuous n-gram boost
            double phraseBoost = computePhraseBoost(queryTerms, chunk.getContent());
            double combinedScore = (similarity * 0.5) + (overlapRatio * 0.5) + phraseBoost;
            double finalScore = Math.min(1.0, Math.max(0.0, combinedScore));

            if (finalScore > 0.05) {
                String bestSnippet = extractBestSnippet(queryTerms, chunk.getContent());
                scoredChunks.add(new ScoredChunk(chunk, finalScore, bestSnippet));
            }
        }

        // Sort descending by score and limit to top-k
        Collections.sort(scoredChunks);
        return scoredChunks.stream().limit(maxRetrievedChunks).collect(Collectors.toList());
    }

    private Map<String, Double> buildTfIdfVector(List<String> tokens, Map<String, Double> idfMap) {
        Map<String, Integer> counts = new HashMap<>();
        for (String token : tokens) {
            counts.put(token, counts.getOrDefault(token, 0) + 1);
        }

        int totalTokens = tokens.size();
        Map<String, Double> vector = new HashMap<>();
        for (Map.Entry<String, Integer> entry : counts.entrySet()) {
            String term = entry.getKey();
            double tf = (double) entry.getValue() / totalTokens;
            double idf = idfMap.getOrDefault(term, 1.0);
            vector.put(term, tf * idf);
        }
        return vector;
    }

    private double computeCosineSimilarity(Map<String, Double> v1, Map<String, Double> v2) {
        double dotProduct = 0.0;
        double norm1 = 0.0;
        double norm2 = 0.0;

        for (Map.Entry<String, Double> e : v1.entrySet()) {
            double val1 = e.getValue();
            norm1 += val1 * val1;
            Double val2 = v2.get(e.getKey());
            if (val2 != null) {
                dotProduct += val1 * val2;
            }
        }

        for (double val2 : v2.values()) {
            norm2 += val2 * val2;
        }

        if (norm1 == 0.0 || norm2 == 0.0) {
            return 0.0;
        }

        return dotProduct / (Math.sqrt(norm1) * Math.sqrt(norm2));
    }

    private double computePhraseBoost(List<String> queryTerms, String chunkContent) {
        String lowerChunk = chunkContent.toLowerCase();
        double boost = 0.0;

        // Bigram match bonus
        for (int i = 0; i < queryTerms.size() - 1; i++) {
            String t1 = queryTerms.get(i);
            String t2 = queryTerms.get(i + 1);
            if (lowerChunk.contains(t1) && lowerChunk.contains(t2)) {
                int pos1 = lowerChunk.indexOf(t1);
                int pos2 = lowerChunk.indexOf(t2);
                if (Math.abs(pos1 - pos2) < 50) {
                    boost += 0.15;
                }
            }
        }

        return Math.min(0.30, boost);
    }

    private String extractBestSnippet(List<String> queryTerms, String content) {
        String[] sentences = content.split("(?<=[.!?\\n])\\s+");
        String bestSentence = "";
        double maxScore = -1.0;

        for (String sentence : sentences) {
            String clean = sentence.trim();
            if (clean.length() < 15) {
                continue;
            }

            // Downweight header-like lines
            boolean isHeader = clean.startsWith("Document ID:") || clean.startsWith("OPSMIND ENTERPRISE");

            int overlap = 0;
            String lower = clean.toLowerCase();
            for (String term : queryTerms) {
                if (lower.contains(term)) {
                    overlap++;
                }
            }

            double score = overlap - (isHeader ? 1.5 : 0.0);
            if (score > maxScore) {
                maxScore = score;
                bestSentence = clean;
            }
        }

        if (bestSentence.isEmpty()) {
            bestSentence = content.length() > 200 ? content.substring(0, 197) + "..." : content;
        }

        return bestSentence;
    }

    public List<String> tokenize(String text) {
        if (text == null || text.isBlank()) {
            return Collections.emptyList();
        }

        String[] raw = WORD_SPLIT.split(text.toLowerCase());
        List<String> tokens = new ArrayList<>();
        for (String r : raw) {
            if (r.length() > 1 && !STOP_WORDS.contains(r)) {
                String normalized = normalizeStem(r);
                if (!STOP_WORDS.contains(normalized)) {
                    tokens.add(normalized);
                }
            }
        }
        return tokens;
    }

    private String normalizeStem(String word) {
        String w = word.trim();
        if (w.endsWith("ies") && w.length() > 4) {
            return w.substring(0, w.length() - 3) + "y";
        }
        if (w.endsWith("es") && w.length() > 3) {
            return w.substring(0, w.length() - 2);
        }
        if (w.endsWith("s") && !w.endsWith("ss") && w.length() > 2) {
            return w.substring(0, w.length() - 1);
        }
        if (w.endsWith("ing") && w.length() > 4) {
            return w.substring(0, w.length() - 3);
        }
        if (w.endsWith("ed") && w.length() > 3) {
            return w.substring(0, w.length() - 2);
        }
        if (w.endsWith("ment") && w.length() > 5) {
            return w.substring(0, w.length() - 4);
        }
        if (w.endsWith("tion") && w.length() > 5) {
            return w.substring(0, w.length() - 4);
        }
        return w;
    }
}
