package com.opsmind.ai.service.ingestion;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Service
public class SampleSopGeneratorService {

    private static final Logger log = LoggerFactory.getLogger(SampleSopGeneratorService.class);

    public record GeneratedPdf(String filename, byte[] bytes) {}

    public List<GeneratedPdf> generateAllSampleSops() {
        List<GeneratedPdf> list = new ArrayList<>();
        try {
            PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
            Resource[] resources = resolver.getResources("classpath*:sample-sops/*.pdf");
            for (Resource r : resources) {
                String filename = r.getFilename();
                try (InputStream is = r.getInputStream()) {
                    list.add(new GeneratedPdf(filename, is.readAllBytes()));
                }
            }
            log.info("Loaded {} bundled sample SOP PDFs from classpath", list.size());
        } catch (IOException e) {
            log.error("Failed to load sample SOP PDFs from classpath", e);
        }
        return list;
    }

    public GeneratedPdf generateRefundSop() throws IOException {
        return loadResourcePdf("Customer_Refund_and_Chargeback_SOP.pdf");
    }

    public GeneratedPdf generateSickLeaveSop() throws IOException {
        return loadResourcePdf("HR_Extended_Sick_Leave_Policy.pdf");
    }

    public GeneratedPdf generateItVpnSop() throws IOException {
        return loadResourcePdf("IT_Remote_Access_and_VPN_Protocol.pdf");
    }

    private GeneratedPdf loadResourcePdf(String filename) throws IOException {
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        Resource r = resolver.getResource("classpath:sample-sops/" + filename);
        if (r.exists()) {
            try (InputStream is = r.getInputStream()) {
                return new GeneratedPdf(filename, is.readAllBytes());
            }
        }
        throw new IOException("Resource not found: sample-sops/" + filename);
    }
}
