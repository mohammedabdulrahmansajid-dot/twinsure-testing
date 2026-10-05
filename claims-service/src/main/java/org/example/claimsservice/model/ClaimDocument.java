package org.example.claimsservice.model;

// Stores metadata for evidence submitted in support of a claim.
// The POC stores a document reference instead of binary file content,
// keeping database and API handling simple and lightweight.

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.claimsservice.enums.DocumentType;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Table("CLAIM_DOCUMENTS")
public class ClaimDocument {

    @Id
    @Column("DOCUMENT_ID")
    private Long documentId;

    @Column("CLAIM_ID")
    private Long claimId;

    @Column("DOCUMENT_TYPE")
    private DocumentType documentType;

    @Column("FILE_NAME")
    private String fileName;

    @Column("DOCUMENT_REFERENCE")
    private String documentReference;

    @Column("DESCRIPTION")
    private String description;

    @Column("UPLOADED_BY")
    private Long uploadedBy;

    @Column("UPLOADED_AT")
    private LocalDateTime uploadedAt;
}