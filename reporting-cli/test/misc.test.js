/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

const fs = require('fs');
const cli = require('./cli.js');

jest.useRealTimers();

describe('download report errors', () => {

    test('dashboard report with basic auth and default tenant', async () => {
        let result = await cli(['-u', 'http://localhost:5601/app/dashboards#/view/722b74f0-b882-11e8-a6d9-e546fe2bba5f', '-a', 'basic', '-c', 'admin:admin',
            '-n', 'basicauthdashboard'], '.');
        expect(result.code).toBe(0);
        const expectedFile = './basicauthdashboard.pdf';
        const stats = fs.statSync(expectedFile);
        expect(stats.size >= 0).toBeTruthy();
        fs.unlinkSync(expectedFile);
    }, 150000);

    test('download csv report from custom tenant', async () => {
        let result = await cli(['-u', 'http://localhost:5601/app/discover#/view/571aaf70-4c88-11e8-b3d7-01146121b73d', '-a', 'basic', '-c', 'admin:admin',
            '-n', 'testcsvoncustomtenant', '-t', 'admin_tenant', '-f', 'csv'], '.');
        expect(result.code).toBe(0);
        const expectedFile = './testcsvoncustomtenant.csv';
        const stats = fs.statSync(expectedFile);
        expect(stats.size >= 0).toBeTruthy();
        fs.unlinkSync(expectedFile);
    }, 150000);
});
