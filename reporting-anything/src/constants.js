/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

export const CHROMIUM_PATH = '../.chromium/headless_shell';

export const REPORT_TYPES = {
  DASHBOARD: 'dashboards#/view/',
  VISUALIZATION: 'visualize#/edit/',
  SAVED_SEARCH: 'discover#/view/',
  NOTEBOOK: 'observability-dashboards#/notebooks'
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
