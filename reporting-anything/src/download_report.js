/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

import _ from 'lodash';
import { ArgumentParser } from 'argparse';
import { exit } from 'process';
import { downloadVisualReport } from './download_helpers.js';
import { REPORT_TYPE } from './constants.js';

// url OR report source type and ID
const parser = new ArgumentParser({
  description: 'Argument parser for Reporting Anything'
});

parser.add_argument('-u', '--url', {help: 'url of report'});
parser.add_argument('-t', '--type', {help: 'report source type'});
parser.add_argument('-id', '--object-id', {help: 'saved object ID'});
parser.add_argument('-f', '--format', {help: 'File format of the report'});
parser.add_argument('-w', '--width', { help: 'Window width of the report'});
parser.add_argument('-ht', '--height', {help: 'Window height of the report'});

const parsed_args = parser.parse_args();

if (parsed_args.url != undefined && parsed_args.type != undefined) {
  console.log('Please specify one of URL or object ID and report source type');
  exit(1);
}

if (parsed_args.type.toUpperCase() !== REPORT_TYPE.DASHBOARD.toUpperCase() && 
    parsed_args.type.toUpperCase() !== REPORT_TYPE.VISUALIZATION.toUpperCase() &&
    parsed_args.type.toUpperCase() !== REPORT_TYPE.NOTEBOOK.toUpperCase()) {
  console.log('Please specify a valid report source type: one of Dashboard, Visualization or Notebook');
  exit(1);
}

if (parsed_args.format !== undefined && 
    parsed_args.format.toUpperCase() !== 'PDF' && 
    parsed_args.format.toUpperCase() !== 'PNG') {
  console.log('Please specify a valid file format: one of PDF or PNG');
  exit(1);
}

let format;

// default format is PDF
if (parsed_args.format === undefined) {
  format = 'pdf';
} else {
  format = parsed_args.format;
}

let width, height;
// set default width and height if not specified
if (parsed_args.width === undefined) {
  width = 1680;
}
if (parsed_args.height  === undefined) {
  height = 2560;
}

const data = await downloadVisualReport(parsed_args.url, parsed_args.type, parsed_args.object_id, format, width, height);
