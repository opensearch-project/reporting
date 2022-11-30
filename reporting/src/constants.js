/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

export const REPORT_TYPE = {
  DASHBOARD: 'Dashboard',
  VISUALIZATION: 'Visualization',
  NOTEBOOK: 'Notebook',
  DISCOVER: 'Saved search',
  OTHER: 'Other',
}

export const SELECTOR = {
  DASHBOARD: '#dashboardViewport',
  VISUALIZATION: '.visEditor__content',
  NOTEBOOK: '.euiPageBody',
  DISCOVER: 'button[id="downloadReport"]'
}

export const FORMAT = {
  PDF: 'pdf',
  PNG: 'png',
  CSV: 'csv'
}

export const AUTH = {
  BASIC_AUTH: 'basic',
  COGNITO_AUTH: 'cognito',
  SAML_AUTH: 'saml',
  NONE: 'none',
}

export const URL_SOURCE = {
  DASHBOARDS: 'dashboards#',
  VISUALIZE: "Visualize",
  DISCOVER: "discover#",
  NOTEBOOKS: "notebooks",
}
