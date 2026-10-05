// Represents supporting-evidence metadata attached to a claim.
// The POC stores a document reference rather than binary file content.

import {
  DocumentType
} from './claim-types.model';

export interface ClaimDocument {
  documentId: number;
  claimId: number;
  documentType: DocumentType;
  fileName: string;
  documentReference: string;
  description: string | null;
  uploadedBy: number;
  uploadedAt: string;
}