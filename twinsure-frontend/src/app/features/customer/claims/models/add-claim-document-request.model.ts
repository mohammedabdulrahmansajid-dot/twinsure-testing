// Matches supporting-evidence metadata accepted by Claims Service.
// The POC stores a document reference instead of binary content.

import {
  DocumentType
} from './claim-types.model';

export interface AddClaimDocumentRequest {
  documentType: DocumentType;
  fileName: string;
  documentReference: string;
  description: string | null;
}