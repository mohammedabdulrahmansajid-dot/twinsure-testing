// Represents one circumstance excluded from insurance coverage.

export interface ProductExclusion {
  exclusionId: number;
  productId: number;
  exclusionCode: string;
  description: string;
  active: boolean;
}