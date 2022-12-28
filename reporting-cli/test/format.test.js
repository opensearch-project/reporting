/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

const cli = require('./cli.js');
const fs = require('fs');
jest.useRealTimers();

describe('report format option', () => {

    test('default format', async () => {
        let result = await cli(['-u', 'https://opensearch.org/', '-n', 'testdownloadpdf'], '.');
        expect(result.code).toBe(0);
        const expectedFile = './testdownloadpdf.pdf';
        const stats = fs.statSync(expectedFile);
        expect(stats.size >= 0).toBeTruthy();
        fs.unlinkSync(expectedFile);
    }, 30000);

    test('download pdf report', async () => {
        let result = await cli(['-u', 'https://opensearch.org/', '-n', 'testdownloadpdf', '-f', 'pdf'], '.');
        expect(result.code).toBe(0);
        const expectedFile = './testdownloadpdf.pdf';
        const stats = fs.statSync(expectedFile);
        expect(stats.size >= 0).toBeTruthy();
        fs.unlinkSync(expectedFile);
    }, 30000);

    test('download png report', async () => {
        let result = await cli(['-u', 'https://opensearch.org/', '-n', 'testdownloadpng', '-f', 'png'], '.');
        expect(result.code).toBe(0);
        const expectedFile = './testdownloadpng.png';
        const stats = fs.statSync(expectedFile);
        expect(stats.size >= 0).toBeTruthy();
        fs.unlinkSync(expectedFile);
    }, 30000);

    test('download csv report', async () => {
        let result = await cli(['-u', 'http://localhost:5601/app/discover#/view/3ba638e0-b894-11e8-a6d9-e546fe2bba5f', '-a', 'basic', '-c', 'admin:admin',
            '-n', 'testdownloadcsv', '-f', 'csv'], '.');
        expect(result.code).toBe(0);
        const expectedFile = './testdownloadcsv.csv';
        const stats = fs.statSync(expectedFile);
        expect(stats.size >= 0).toBeTruthy();
        fs.unlinkSync(expectedFile);
    }, 150000);
});
