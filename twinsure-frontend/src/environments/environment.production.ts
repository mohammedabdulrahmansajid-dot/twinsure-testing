// Defines production application settings.
// A relative API URL allows the deployed frontend and Gateway
// to share the same origin in a production environment.

export const environment = {
  production: true,
  apiBaseUrl: '',
} as const;
