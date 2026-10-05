// Represents one stored action permission for an AI Twin.

import {
  ActionType,
  PermissionLevel
} from './ai-twin-types.model';

export interface TwinPermissionResponse {
  permissionId: number;
  twinId: number;
  actionType: ActionType;
  permissionLevel: PermissionLevel;
  actionLimit: number | null;
  active: boolean;
  createdAt: string;
  updatedAt: string;
}