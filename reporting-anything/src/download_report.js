#!/usr/bin/env node
/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

import { ArgumentParser } from 'argparse';
import { exit } from 'process';
import { downloadVisualReport } from './download_helpers.js';
import dotenv from "dotenv";
dotenv.config();

"use strict";

// url OR report source type and ID
const parser = new ArgumentParser({
  description: 'Argument parser for Reporting Anything'
});

parser.add_argument('-u', '--url', {help: 'url of report'});
parser.add_argument('-f', '--format', {help: 'File format of the report'});
parser.add_argument('-w', '--width', { help: 'Window width of the report'});
parser.add_argument('-t', '--height', {help: 'Window height of the report'});
parser.add_argument('-n', '--filename', {help: 'File name of the report'});
parser.add_argument('-a', '--auth', {help: 'authentication type of the report; options are bas'});
parser.add_argument('-c', '--credentials', {help: 'login credentials in the format of username:password'});
parser.add_argument('-s', '--sender', {help: 'email address of sender'});
parser.add_argument('-r', '--recipient', {help: 'email address of recipient'});

const parsed_args = parser.parse_args();

if (parsed_args.url === undefined) {
  console.log('Please specify a URL');
  exit(1);
}

if (parsed_args.format !== undefined && 
    parsed_args.format.toUpperCase() !== 'PDF' && 
    parsed_args.format.toUpperCase() !== 'PNG') {
  console.log('Please specify a valid file format: one of PDF or PNG');
  exit(1);
}

let format;

// Set default format to PDF if not specified.
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
let filename;
if (parsed_args.filename !== undefined) {
  filename = parsed_args.filename;
}
else {
  filename = 'reporting_anything';
}

let authType;
if (parsed_args.auth !== undefined) {
  authType = parsed_args.auth;
}

// Get credentials from .env file.
let username = process.env.USERNAME
let password = process.env.PASSWORD

// If credentials are not set in .env file, get credentials from command line arguments.
if(username === undefined || username.length <=0 && parsed_args.credentials !== undefined) {
  username = parsed_args.credentials.split(":")[0];
}
if(password === undefined || password.length <=0 && parsed_args.credentials !== undefined) {
  password = parsed_args.credentials.split(":")[1];
}

// If auth type is not none & credentials are missing, exit with error.
if(authType != undefined && authType != 'none' && username == undefined && password == undefined){
  console.log('Please specify a valid username or password');
  exit(1);
}

await downloadVisualReport(parsed_args.url, format, width, height, filename, authType, username, password, parsed_args.sender, parsed_args.recipient);
