// Defines development-time application settings.
// All frontend API requests use the central API Gateway.

export const environment = {
  production: false,
  apiBaseUrl: 'http://localhost:8080'
} as const;