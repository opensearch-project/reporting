/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

const cli = require('./cli.js');
const fs = require('fs');
jest.useRealTimers();

describe('tenant option', () => {

    test('dashboard report with private tenant', async () => {
        let result = await cli(['-u', 'http://localhost:5601/app/dashboards#/view/722b74f0-b882-11e8-a6d9-e546fe2bba5f', '-a', 'basic', '-c', 'admin:admin',
            '-n', 'privatetenantdashboard', '-t', 'private'], '.');
        expect(result.code).toBe(0);
        const expectedFile = './privatetenantdashboard.pdf';
        const stats = fs.statSync(expectedFile);
        expect(stats.size >= 0).toBeTruthy();
        fs.unlinkSync(expectedFile);
    }, 150000);

    test('dashboard report with global tenant', async () => {
        let result = await cli(['-u', 'http://localhost:5601/app/dashboards#/view/edf84fe0-e1a0-11e7-b6d5-4dc382ef7f5b', '-a', 'basic', '-c', 'admin:admin',
            '-n', 'globaltenantdashboard', '-t', 'global'], '.');
        expect(result.code).toBe(0);
        const expectedFile = './globaltenantdashboard.pdf';
        const stats = fs.statSync(expectedFile);
        expect(stats.size >= 0).toBeTruthy();
        fs.unlinkSync(expectedFile);
    }, 150000);

    test('dashboard report with custom tenant', async () => {
        let result = await cli(['-u', 'http://localhost:5601/app/dashboards#/view/7adfa750-4c81-11e8-b3d7-01146121b73d', '-a', 'basic', '-c', 'admin:admin',
            '-n', 'customtenantdashboard', '-t', 'admin_tenant'], '.');
        expect(result.code).toBe(0);
        const expectedFile = './customtenantdashboard.pdf';
        const stats = fs.statSync(expectedFile);
        expect(stats.size >= 0).toBeTruthy();
        fs.unlinkSync(expectedFile);
    }, 150000);
});
