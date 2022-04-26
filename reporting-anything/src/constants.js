/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

export const CHROMIUM_PATH = '../.chromium/headless_shell';

export const REPORT_TYPE_URLS = {
  DASHBOARD: 'dashboards#/view/',
  VISUALIZATION: 'visualize#/edit/',
  SAVED_SEARCH: 'discover#/view/',
  NOTEBOOK: 'observability-dashboards#/notebooks'
}

export const REPORT_TYPE = {
  SAVED_SEARCH: 'Saved search',
  DASHBOARD: 'Dashboard',
  VISUALIZATION: 'Visualization',
  NOTEBOOK: 'Notebook',
}

export const SELECTOR = {
  DASHBOARD: '#dashboardViewport',
  VISUALIZATION: '.visEditor__content',
  NOTEBOOK: '.euiPageBody',
}

export const FORMAT = {
  PDF: 'pdf',
  PNG: 'png'
}
