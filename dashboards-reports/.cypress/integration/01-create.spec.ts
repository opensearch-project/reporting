/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

describe('Adding sample data', () => {
  it('Adds sample data', () => {
    cy.visit(`${Cypress.env('opensearchDashboards')}/app/home#/tutorial_directory/sampleData`);
    cy.get('div[data-test-subj="sampleDataSetCardflights"]').contains(/(Add|View) data/).click();
    cy.wait(3000);
    cy.visit(`${Cypress.env('opensearchDashboards')}/app/home#/tutorial_directory/sampleData`);
    cy.get('div[data-test-subj="sampleDataSetCardecommerce"]').contains(/(Add|View) data/).click();
    cy.wait(3000);
    cy.visit(`${Cypress.env('opensearchDashboards')}/app/home#/tutorial_directory/sampleData`);
    cy.get('div[data-test-subj="sampleDataSetCardlogs"]').contains(/(Add|View) data/).click();
    cy.wait(3000);
  });
});

describe('Cypress', () => {
  it('Visits Reporting homepage', () => {
    cy.visit(`${Cypress.env('opensearchDashboards')}/app/reports-dashboards#/`);
    cy.location('pathname', { timeout: 60000 }).should(
      'include',
      '/reports-dashboards'
    );
  });

  it('Visit Create page', () => {
    cy.visit(`${Cypress.env('opensearchDashboards')}/app/reports-dashboards#/`);
    cy.location('pathname', { timeout: 60000 }).should(
      'include',
      '/reports-dashboards'
    );
    cy.wait(12500); // wait for the page to load
    cy.get('#createReportHomepageButton').click({ force: true });
  });

  it('Create a new on-demand report definition', () => {
    cy.visit(`${Cypress.env('opensearchDashboards')}/app/reports-dashboards#/`);
    cy.location('pathname', { timeout: 60000 }).should(
      'include',
      '/reports-dashboards'
    );
    cy.wait(12500);
    cy.get('#createReportHomepageButton').click();

    // enter a report name
    cy.get('#reportSettingsName').type('Create cypress test on-demand report');

    // enter a report description
    cy.get('#reportSettingsDescription').type('Description for cypress test');

    // select a report source
    cy.get('[data-test-subj="comboBoxInput"]').eq(0).click({ force: true });

    // select drop-down option in report source list
    cy.contains('[Logs] Web Traffic').click();

    cy.wait(500);

    // create an on-demand report definition
    cy.get('#createNewReportDefinition').click({ force: true });

    cy.wait(12500);
    
    // check that re-direct to 
    cy.get('#reportDefinitionDetailsLink').should('exist');
  });

  it('Create a new scheduled report definition', () => {
    cy.visit(`${Cypress.env('opensearchDashboards')}/app/reports-dashboards#/`);
    cy.location('pathname', { timeout: 60000 }).should(
      'include',
      '/reports-dashboards'
    );
    cy.wait(12500);
    cy.get('#createReportHomepageButton').click();

    // enter a report name
    cy.get('#reportSettingsName').type('Create cypress test scheduled report');

    // enter a report description
    cy.get('#reportSettingsDescription').type('Description for cypress test');

    // select a report source
    cy.get('[data-test-subj="comboBoxInput"]').eq(0).click({ force: true });

    // select drop-down option in report source list
    cy.contains('[Logs] Web Traffic').click();

    cy.wait(500);

    // set report trigger to Schedule option
    cy.get('[type="radio"]').check({ force: true });

    // set source back to saved search for testing purpose
    cy.get('#savedSearchReportSource').check({ force: true });

    // create scheduled report definition
    cy.get('#createNewReportDefinition').click({ force: true });

    cy.wait(12500);
    
    // check that re-direct to 
    cy.get('#reportDefinitionDetailsLink').should('exist');
  });
});
