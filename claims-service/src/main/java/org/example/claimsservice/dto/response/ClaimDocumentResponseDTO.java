package org.example.claimsservice.dto.response;

// Represents evidence metadata attached to a formal claim.
// It identifies the document, its reference, uploader, and upload time.

import org.example.claimsservice.enums.DocumentType;

import java.time.LocalDateTime;

public record ClaimDocumentResponseDTO(

        Long documentId,
        Long claimId,
        DocumentType documentType,
        String fileName,
        String documentReference,
        String description,
        Long uploadedBy,
        LocalDateTime uploadedAt

) {
}