// Combines an application with complete insurance-product details.

import { InsuranceProductDetails } from '../../insurance-products/models/insurance-product-details.model';
import { PolicyApplicationResponse } from './policy-application-response.model';

export interface PolicyApplicationDetails {
  application: PolicyApplicationResponse;
  product: InsuranceProductDetails;
}
