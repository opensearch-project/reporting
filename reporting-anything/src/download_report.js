#!/usr/bin/env node
/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

import { ArgumentParser } from 'argparse';
import { exit } from 'process';
import { downloadVisualReport } from './report_helpers.js';
import dotenv from "dotenv";
dotenv.config();

"use strict";

// url OR report source type and ID
const parser = new ArgumentParser({
  description: 'Argument parser for Reporting CLI'
});

parser.add_argument('--url', {help: 'url of report'});
parser.add_argument('--format', {help: 'File format of the report'});
parser.add_argument('--width', { help: 'Window width of the report'});
parser.add_argument('--height', {help: 'Window height of the report'});
parser.add_argument('--filename', {help: 'File name of the report'});
parser.add_argument('--auth', {help: 'authentication type of the report. options are basic, SAML, cognito and none.'});
parser.add_argument('--credentials', {help: 'login credentials in the format of username:password'});
parser.add_argument('--from', {help: 'email address of the sender'});
parser.add_argument('--to', {help: 'email address of the recipient'});
parser.add_argument('--transport', {help: 'transport for sending the email. options are SES and SMTP.'});

const parsed_args = parser.parse_args();

// Get URL for the report.
if (parsed_args.url === undefined) {
  console.log('Please specify a URL');
  exit(1);
}

// Get authentication type.
let authType;
if (parsed_args.auth !== undefined) {
  authType = parsed_args.auth;
}

// Get credentials from .env file.
let username = process.env.USERNAME;
let password = process.env.PASSWORD;

// If credentials are not set in .env file, get credentials from command line arguments.
if((username === undefined || username.length <=0) && parsed_args.credentials !== undefined) {
  username = parsed_args.credentials.split(":")[0];
}
if((password === undefined || password.length <=0) && parsed_args.credentials !== undefined) {
  password = parsed_args.credentials.split(":")[1];
}

// If auth type is not none & credentials are missing, exit with error.
if(authType != undefined && authType != 'none' && username == undefined && password == undefined){
  console.log('Please specify a valid username or password');
  exit(1);
}

// Validate report format.
if (parsed_args.format !== undefined && 
    parsed_args.format.toUpperCase() !== 'PDF' && 
    parsed_args.format.toUpperCase() !== 'PNG') {
  console.log('Please specify a valid file format: one of PDF or PNG');
  exit(1);
}

// Set default format to PDF if not specified.
let format;
if (parsed_args.format === undefined) {
  format = 'pdf';
} else {
  format = parsed_args.format;
}

// Set default width and height if not specified.
let width, height;
if (parsed_args.width === undefined) {
  width = 1680;
}
else {
  width = Number(parsed_args.width)
}
if (parsed_args.height === undefined) {
  height = 600;
}
else {
  height = Number(parsed_args.height)
}

// Set default filename is not specified.
let filename = process.env.FILENAME;
if ((filename === undefined || filename.length <=0) && parsed_args.filename !== undefined) {
  filename = parsed_args.filename;
}
else {
  filename = 'reporting_anything';
}

// Set transport for the email.
let transport = process.env.TRANSPORT || parsed_args.transport;

// Set email addresse if specified
let sender = process.env.FROM || parsed_args.from;
let recipient = process.env.TO || parsed_args.to;

await downloadVisualReport(parsed_args.url, format, width, height, filename, authType, username, password, sender, recipient, transport);
