/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

describe('Cypress', () => {
  it('Visit report definition details page', () => {
    cy.visit(`${Cypress.env('opensearchDashboards')}/app/reports-dashboards#/`);
    cy.location('pathname', { timeout: 60000 }).should(
      'include',
      '/reports-dashboards'
    );

    cy.wait(12500);

    cy.get('#reportDefinitionDetailsLink').first().click();

    cy.url().should('include', 'report_definition_details');

    cy.get('#deleteReportDefinitionButton').should('exist');

    cy.get('#editReportDefinitionButton').should('exist');

    if (cy.get('body').contains('Schedule details')) {
      cy.wait(1000);
      cy.get('#changeStatusFromDetailsButton').click();
    } else {
      cy.wait(1000);
      cy.get('#generateReportFromDetailsButton').click();
    }

    cy.get('#deleteReportDefinitionButton').click();
  });

  it('Visit report details page', () => {
    cy.visit(`${Cypress.env('opensearchDashboards')}/app/reports-dashboards#/`);
    cy.location('pathname', { timeout: 60000 }).should(
      'include',
      '/reports-dashboards'
    );

    cy.wait(12500);
    cy.get('#reportDetailsLink').first().click();

    cy.url().should('include', 'report_details');
  });
});
