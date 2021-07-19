/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * The OpenSearch Contributors require contributions made to
 * this file be licensed under the Apache-2.0 license or a
 * compatible open source license.
 *
 * Modifications Copyright OpenSearch Contributors. See
 * GitHub history for details.
 */

/*
 * Copyright 2020 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * A copy of the License is located at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * or in the "license" file accompanying this file. This file is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

describe('Cypress', () => {
  it('Visit edit page, update name and description', () => {
    cy.visit(`${Cypress.env('opensearchDashboards')}/app/reports-dashboards#/`);
    cy.location('pathname', { timeout: 60000 }).should(
      'include',
      '/reports-dashboards'
    );

    cy.wait(12500);

    cy.get('#reportDefinitionDetailsLink').first().click();

    cy.get('#editReportDefinitionButton').should('exist');

    cy.get('#editReportDefinitionButton').click();

    cy.url().should('include', 'edit');

    cy.wait(1000);

    // update the report name
    cy.get('#reportSettingsName').type(' update name');

    // update report description
    cy.get('#reportSettingsDescription').type(' update description');

    cy.get('#editReportDefinitionButton').click({ force: true });

    cy.wait(12500);
    
    // check that re-direct to home page
    cy.get('#reportDefinitionDetailsLink').should('exist');
  });

  it('Visit edit page, change report trigger', () => {
    cy.visit(`${Cypress.env('opensearchDashboards')}/app/reports-dashboards#/`);
    cy.location('pathname', { timeout: 60000 }).should(
      'include',
      '/reports-dashboards'
    );

    cy.wait(12500);

    cy.get('#reportDefinitionDetailsLink').first().click();

    cy.get('#editReportDefinitionButton').should('exist');

    cy.get('#editReportDefinitionButton').click();

    cy.url().should('include', 'edit');

    cy.wait(1000);
    cy.get('#reportDefinitionTriggerTypes > div:nth-child(2)').click({ force: true });

    cy.get('#Schedule').check({ force: true });
    cy.get('#editReportDefinitionButton').click({ force: true });

    cy.wait(12500);
    
    // check that re-direct to home page
    cy.get('#reportDefinitionDetailsLink').should('exist');
  });

  it('Visit edit page, change report trigger back', () => {
    cy.visit(`${Cypress.env('opensearchDashboards')}/app/reports-dashboards#/`);
    cy.location('pathname', { timeout: 60000 }).should(
      'include',
      '/reports-dashboards'
    );

    cy.wait(12500);

    cy.get('#reportDefinitionDetailsLink').first().click();

    cy.get('#editReportDefinitionButton').should('exist');

    cy.get('#editReportDefinitionButton').click();

    cy.url().should('include', 'edit');

    cy.wait(1000);

    cy.get('#reportDefinitionTriggerTypes > div:nth-child(1)').click({ force: true });

    cy.get('#editReportDefinitionButton').click({ force: true });

    cy.wait(12500);
    
    // check that re-direct to home page
    cy.get('#reportDefinitionDetailsLink').should('exist');
  });
});
