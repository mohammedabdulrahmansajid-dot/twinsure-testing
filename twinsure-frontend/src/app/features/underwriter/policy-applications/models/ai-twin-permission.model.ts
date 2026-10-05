// Represents one AI Twin action permission shown during underwriting review.

export interface AiTwinPermission {
  permissionId: number;
  twinId: number;
  actionType: string;
  permissionLevel: string;
  actionLimit: number | null;
  active: boolean;
  createdAt: string;
  updatedAt: string;
}
