// Represents complete Customer-facing insurance-product details.

import { InsuranceProduct } from './insurance-product.model';
import { ProductCoverage } from './product-coverage.model';
import { ProductExclusion } from './product-exclusion.model';

export interface InsuranceProductDetails extends InsuranceProduct {
  coverages: ProductCoverage[];
  exclusions: ProductExclusion[];
  createdAt: string;
  updatedAt: string;
}
