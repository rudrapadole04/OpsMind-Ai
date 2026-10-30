package com.opsmind.ai.controller;

import com.opsmind.ai.service.ingestion.SampleSopGeneratorService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class DocumentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private SampleSopGeneratorService generatorService;

    @Test
    @DisplayName("GET /api/documents should return list of documents")
    void shouldReturnDocumentList() throws Exception {
        mockMvc.perform(get("/api/documents"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", isA(java.util.List.class)));
    }

    @Test
    @DisplayName("POST /api/documents/upload with PDF should return 201 Created")
    void shouldUploadPdfSuccessfully() throws Exception {
        SampleSopGeneratorService.GeneratedPdf pdf = generatorService.generateRefundSop();

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "Uploaded_Refund_Policy.pdf",
                "application/pdf",
                pdf.bytes()
        );

        mockMvc.perform(multipart("/api/documents/upload").file(file))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.filename").value("Uploaded_Refund_Policy.pdf"))
                .andExpect(jsonPath("$.totalPages").value(3))
                .andExpect(jsonPath("$.status").value("INDEXED"))
                .andExpect(jsonPath("$.chunks", hasSize(greaterThanOrEqualTo(3))));
    }

    @Test
    @DisplayName("POST /api/documents/upload with non-PDF should return 400 Bad Request")
    void shouldRejectNonPdfUpload() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "notes.txt",
                "text/plain",
                "This is not a pdf file".getBytes()
        );

        mockMvc.perform(multipart("/api/documents/upload").file(file))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Only PDF documents (.pdf) are supported"));
    }
}
