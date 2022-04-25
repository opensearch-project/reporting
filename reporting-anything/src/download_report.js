/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

import _ from 'lodash';
import { ArgumentParser } from 'argparse';
import { exit } from 'process';
import { downloadVisualReport } from './download_helpers.js';

// url OR report source type and ID
const parser = new ArgumentParser({
  description: 'Argument parser for Reporting Anything'
});

parser.add_argument('-u', '--url', {help: 'url of report'});
parser.add_argument('-t', '--type', {help: 'report source type'});
parser.add_argument('-id', '--object-id', {help: 'saved object ID'});
parser.add_argument('-f', '--format', {help: 'File format of the report'});

const parsed_args = parser.parse_args();


// can only have one of url or saved object id and report source type
if (parsed_args.url != undefined && parsed_args.type != undefined) {
  console.log('Please specify one of URL or object ID and report source type');
  exit(1);
} 

downloadVisualReport(parsed_args.url, parsed_args.type, parsed_args.object_id, parsed_args.format);
