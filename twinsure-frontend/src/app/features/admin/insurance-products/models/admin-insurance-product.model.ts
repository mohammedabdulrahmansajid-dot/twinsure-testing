// Defines insurance-product contracts used by the Admin workspace.
// Coverage and exclusion requests match Insurance Policy Service exactly.

export type AdminProductStatus =
  | 'DRAFT'
  | 'ACTIVE'
  | 'INACTIVE';

export type InsuranceActionType =
  | 'TRAVEL_BOOKING'
  | 'ONLINE_PURCHASE'
  | 'SUBSCRIPTION_MANAGEMENT';

export type ViolationType =
  | 'WRONG_ACTION'
  | 'APPROVAL_MISSING'
  | 'LIMIT_EXCEEDED'
  | 'DUPLICATE_ACTION'
  | 'MISSED_CANCELLATION';

export interface AdminInsuranceProduct {
  productId: number;
  productCode: string;
  productName: string;
  description: string;
  basePremium: number;
  coverageLimit: number;
  deductible: number;
  status: AdminProductStatus;
}

export interface AdminProductCoverage {
  coverageId: number;
  productId: number;
  actionType: InsuranceActionType;
  violationType: ViolationType;
  coverageLimit: number | null;
  active: boolean;
  description: string;
}

export interface AdminProductExclusion {
  exclusionId: number;
  productId: number;
  exclusionCode: string;
  description: string;
  active: boolean;
}

export interface AdminInsuranceProductDetails
  extends AdminInsuranceProduct {
  coverages: AdminProductCoverage[];
  exclusions: AdminProductExclusion[];
  createdAt: string;
  updatedAt: string;
}

export interface CreateInsuranceProductRequest {
  productCode: string;
  productName: string;
  description: string;
  basePremium: number;
  coverageLimit: number;
  deductible: number;
}

export interface UpdateInsuranceProductRequest {
  productName: string;
  description: string;
  basePremium: number;
  coverageLimit: number;
  deductible: number;
}

export interface UpdateProductStatusRequest {
  status: AdminProductStatus;
}

export interface ProductCoverageRequest {
  actionType: InsuranceActionType;
  violationType: ViolationType;
  coverageLimit: number | null;
  description: string;
}

export interface ProductExclusionRequest {
  exclusionCode: string;
  description: string;
}