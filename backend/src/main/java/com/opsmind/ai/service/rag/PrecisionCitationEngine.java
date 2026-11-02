package com.opsmind.ai.service.rag;

import com.opsmind.ai.model.dto.CitationDto;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class PrecisionCitationEngine {

    public List<CitationDto> buildCitations(List<ScoredChunk> scoredChunks) {
        if (scoredChunks == null || scoredChunks.isEmpty()) {
            return Collections.emptyList();
        }

        Map<String, CitationDto> uniqueCitations = new LinkedHashMap<>();

        for (ScoredChunk scored : scoredChunks) {
            String filename = scored.getChunk().getDocument().getFilename();
            int pageNumber = scored.getChunk().getPageNumber();
            String key = filename + "_p" + pageNumber;

            CitationDto existing = uniqueCitations.get(key);
            if (existing == null || scored.getScore() > existing.getRelevanceScore()) {
                uniqueCitations.put(key, new CitationDto(
                        filename,
                        pageNumber,
                        scored.getMostRelevantSnippet(),
                        Math.round(scored.getScore() * 100.0) / 100.0
                ));
            }
        }

        return new ArrayList<>(uniqueCitations.values());
    }

    public String generateCitedAnswer(String rawSummary, List<CitationDto> citations) {
        if (citations.isEmpty()) {
            return rawSummary;
        }

        StringBuilder sb = new StringBuilder(rawSummary.trim());
        sb.append("\n\n**Verified Sources & Citations:**\n");
        for (int i = 0; i < citations.size(); i++) {
            CitationDto c = citations.get(i);
            sb.append(String.format("• [%d] **%s** (Page %d) — \"%s\" [Confidence: %d%%]\n",
                    i + 1,
                    c.getSourceFilename(),
                    c.getPageNumber(),
                    c.getExcerpt(),
                    (int) (c.getRelevanceScore() * 100)));
        }

        return sb.toString();
    }
}
