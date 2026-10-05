// Represents one active insurance plan returned by the catalogue endpoint.
// Monetary BigDecimal values arrive from JSON as numbers.

export type ProductStatus =
  | 'DRAFT'
  | 'ACTIVE'
  | 'INACTIVE';

export interface InsuranceProduct {
  productId: number;
  productCode: string;
  productName: string;
  description: string;
  basePremium: number;
  coverageLimit: number;
  deductible: number;
  status: ProductStatus;
}