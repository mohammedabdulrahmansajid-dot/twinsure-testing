// Describes one role-specific sidebar navigation item.
// Keeping navigation data typed prevents malformed routes and labels.

export interface NavigationItem {
  label: string;
  shortLabel: string;
  route: string;
  exact: boolean;
}