/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

const path = require('path');
const exec = require('child_process').exec;
const fs = require('fs');
jest.useRealTimers();;

describe('download report options', () => {

  test('missing url error', async () => {
    let result = await cli(['', ''], '.');
    expect(result.code).toBe(1);
    expect(result.stderr).toContain('Please specify URL');
  });

  test('missing credentials error', async () => {
    let result = await cli(['-u', 'https://test.com', '-a', 'basic'], '.');
    expect(result.code).toBe(1);
    expect(result.stderr).toContain('Please specify a valid username or password');
  });

  test('missing password error', async () => {
    let result = await cli(['-u', 'https://test.com', '-a', 'basic', '-c', 'name:'], '.');
    expect(result.code).toBe(1);
    expect(result.stderr).toContain('Please specify a valid username or password');
  });

  test('invalid report format error', async () => {
    let result = await cli(['-f', 'txt'], '.');
    expect(result.code).toBe(1);
    expect(result.stderr).toContain('error: option \'-f, --format <type>\' argument \'txt\' is invalid. Allowed choices are pdf, png, csv.');
  });


  test('download pdf report', async () => {
    let result = await cli(['-u', 'https://aws.com', '-n', 'testdownloadpdf'], '.');
    expect(result.code).toBe(0);
    const expectedFile = './testdownloadpdf.pdf';
    const stats = fs.statSync(expectedFile);
    expect(stats.size >= 0).toBeTruthy();
    fs.unlinkSync(expectedFile);
  }, 30000);

  test('download png report', async () => {
    let result = await cli(['-u', 'https://aws.com', '-n', 'testdownloadpng', '-f', 'png'], '.');
    expect(result.code).toBe(0);
    const expectedFile = './testdownloadpng.png';
    const stats = fs.statSync(expectedFile);
    expect(stats.size >= 0).toBeTruthy();
    fs.unlinkSync(expectedFile);
  }, 30000);

  test('basic auth with default tenant', async () => {
    let result = await cli(['-u', 'http://localhost:5601/app/dashboards#/view/7adfa750-4c81-11e8-b3d7-01146121b73d', '-a', 'basic', '-c', 'admin:admin',
      '-n', 'basicauthdashboard'], '.');
    expect(result.code).toBe(0);
    const expectedFile = './basicauthdashboard.pdf';
    const stats = fs.statSync(expectedFile);
    expect(stats.size >= 0).toBeTruthy();
    fs.unlinkSync(expectedFile);
  }, 100000);

  test('download csv report', async () => {
    let result = await cli(['-u', 'http://localhost:5601/app/discover#/view/571aaf70-4c88-11e8-b3d7-01146121b73d', '-a', 'basic', '-c', 'admin:admin',
      '-n', 'testdownloadcsv', '-f', 'csv'], '.');
    expect(result.code).toBe(0);
    const expectedFile = './testdownloadcsv.csv';
    const stats = fs.statSync(expectedFile);
    expect(stats.size >= 0).toBeTruthy();
    fs.unlinkSync(expectedFile);
  }, 100000);


  test('global tenant', async () => {
    let result = await cli(['-u', 'http://localhost:5601/app/dashboards#/view/7adfa750-4c81-11e8-b3d7-01146121b73d', '-a', 'basic', '-c', 'admin:admin',
      '-n', 'globaltenantdashboard', '-t', 'global'], '.');
    expect(result.code).toBe(0);
    const expectedFile = './globaltenantdashboard.pdf';
    const stats = fs.statSync(expectedFile);
    expect(stats.size >= 0).toBeTruthy();
    fs.unlinkSync(expectedFile);
  }, 100000);

  test('private tenant', async () => {
    let result = await cli(['-u', 'http://localhost:5601/app/dashboards#/view/722b74f0-b882-11e8-a6d9-e546fe2bba5f', '-a', 'basic', '-c', 'admin:admin',
      '-n', 'privatetenantdashboard', '-t', 'private'], '.');
    expect(result.code).toBe(0);
    const expectedFile = './privatetenantdashboard.pdf';
    const stats = fs.statSync(expectedFile);
    expect(stats.size >= 0).toBeTruthy();
    fs.unlinkSync(expectedFile);
  }, 100000);

  test('custom tenant', async () => {
    let result = await cli(['-u', 'http://localhost:5601/app/dashboards#/view/edf84fe0-e1a0-11e7-b6d5-4dc382ef7f5b', '-a', 'basic', '-c', 'admin:admin',
      '-n', 'customtenantdashboard', '-t', 'testcustomtenant'], '.');
    expect(result.code).toBe(0);
    const expectedFile = './customtenantdashboard.pdf';
    const stats = fs.statSync(expectedFile);
    expect(stats.size >= 0).toBeTruthy();
    fs.unlinkSync(expectedFile);
  }, 100000);

  test('download csv report from custom tenant', async () => {
    let result = await cli(['-u', 'http://localhost:5601/app/discover#/view/9e8a6810-7bf8-11ed-9232-251172581503', '-a', 'basic', '-c', 'admin:admin',
      '-n', 'testcsvoncustomtenant', '-f', 'csv', '-t', 'testcustomtenant'], '.');
    expect(result.code).toBe(0);
    const expectedFile = './testcsvoncustomtenant.csv';
    const stats = fs.statSync(expectedFile);
    expect(stats.size >= 0).toBeTruthy();
    fs.unlinkSync(expectedFile);
  }, 100000);
});

function cli(args, cwd) {
  return new Promise(resolve => {
    exec(`node ${path.resolve('./src/index.js')} ${args.join(' ')}`,
      { cwd },
      (error, stdout, stderr) => {
        resolve({
          code: error && error.code ? error.code : 0,
          error,
          stdout,
          stderr
        })
      })
  })
}