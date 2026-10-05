// Matches the permission configuration accepted by AI Twin Service.
// The same route creates or updates the selected action configuration.

import {
  ActionType,
  PermissionLevel
} from './ai-twin-types.model';

export interface TwinPermissionRequest {
  actionType: ActionType;
  permissionLevel: PermissionLevel;
  actionLimit: number | null;
}