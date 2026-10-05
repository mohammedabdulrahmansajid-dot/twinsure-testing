// Represents one active coverage rule configured for an insurance product.

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

export interface ProductCoverage {
  coverageId: number;
  productId: number;
  actionType: InsuranceActionType;
  violationType: ViolationType;
  coverageLimit: number | null;
  active: boolean;
  description: string;
}