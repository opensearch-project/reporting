#!/usr/bin/env node
/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

import { sendEmail } from './email_helpers.js';
import { program, Option} from 'commander';
import { exit } from 'process';
import { downloadVisualReport } from './report_helpers.js';
import dotenv from "dotenv";
dotenv.config();

"use strict";

program
  .name('reporting-anything')
  .description('Reporting CLI to download and email reports');

program
  .requiredOption('-u, --url <url>', 'url of report')
  .addOption(new Option('-a, --auth <type>', 'authentication type of the report')
    .default('none')
    .choices(['basic', 'cognito', 'SAML']))
  .addOption(new Option('-c, --credentials <username:password>', 'login credentials')
    .env('USERNAME and PASSWORD'))
  .addOption(new Option('-f, --format <type>', 'file format of the report')
    .default('pdf')
    .choices(['pdf', 'png']))
  .addOption(new Option('-w, --width <psize>', 'window width of the report')
    .default('1680'))
  .addOption(new Option('-l, --height <size>', 'window height of the report')
    .default('600'))
  .addOption(new Option('-n, --filename <name>', 'file name of the report')
    .default('reporting_anything')
    .env('FILENAME'))
  .addOption(new Option('-t, --transport <method>', 'transport for sending the email')
    .choices(['SES', 'SMTP'])
    .env('TRANSPORT'))
  .addOption(new Option('-s, --from <sender>', 'email address of the sender')
    .env('FROM'))
  .addOption(new Option('-r, --to <recipient>', 'email address of the recipient')
    .env('TO'))
  .addOption(new Option('--smtphost <host>', 'the hostname of the SMTP server')
    .env('SMTP_HOST'))
  .addOption(new Option('--smtpport <port>', 'the port for connection')
    .env('SMTP_PORT'))
  .addOption(new Option('--smtpsecure <flag>', 'use TLS when connecting to server')
    .env('SMTP_SECURE'))
  .addOption(new Option('--smtpusername <username>', 'SMTP username')
    .env('SMTP_USERNAME'))
  .addOption(new Option('--smtppassword <password>', 'SMTP password')
    .env('SMTP_PASSWORD'))

program.parse(process.argv);
const parsed_args = program.opts();

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
if(parsed_args.auth != undefined && parsed_args.auth != 'none' && username == undefined && password == undefined){
  console.log('Please specify a valid username or password');
  exit(1);
}

// Validate report format.
if (parsed_args.format.toUpperCase() !== 'PDF' && 
    parsed_args.format.toUpperCase() !== 'PNG') {
  console.log('Please specify a valid file format: one of PDF or PNG');
  exit(1);
}

// Set default filename is not specified.
let filename = process.env.FILENAME || parsed_args.filename;

// Set transport for the email.
let transport = process.env.TRANSPORT || parsed_args.transport;

// Set email addresse if specified
let sender = process.env.FROM || parsed_args.from;
let recipient = process.env.TO || parsed_args.to;

// Set width and height of the window
let width = Number(parsed_args.width);
let height = Number(parsed_args.height);

await downloadVisualReport(parsed_args.url, parsed_args.format, width, height, filename, parsed_args.auth, username, password, sender, recipient, transport);

await sendEmail(`${filename}.${parsed_args.format}`, sender, recipient, parsed_args.format, transport, parsed_args.smtphost, parsed_args.smtpport, parsed_args.smtpsecure, parsed_args.smtpusername, parsed_args.smtppassword);