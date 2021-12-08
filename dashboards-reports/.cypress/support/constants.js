/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

export const ADMIN_AUTH = {
  username: 'admin',
  password: 'admin',
};

export function visitReportingLandingPage() {
  cy.visit(`${Cypress.env('opensearchDashboards')}/app/reports-dashboards#/`);
  cy.location('pathname', { timeout: 60000 }).should(
    'include',
    '/reports-dashboards'
  );
}