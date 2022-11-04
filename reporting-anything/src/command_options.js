#!/usr/bin/env node
/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

import { program, Option} from 'commander';
import { exit } from 'process';
import ora from 'ora';
import dotenv from "dotenv";
dotenv.config();

const spinner = ora();

export var downloadOptions = {
    url: null,
    auth: null,
    username: null,
    password: null,
    format: null,
    width: null,
    height: null,
    filename: null,
  }

export var emailOptions = {
    transport: null,
    sender: null,
    recipient: null,
    smtphost: null,
    smtpport: null,
    smtpsecure: null,
    smtpusername: null,
    smtppassword: null
}

export async function getCommandArguments() {
    spinner.start('Fetching the arguments values');

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
    const options = program.opts();
    getDownloadOptions(options);
    getEmailOptions(options);
    spinner.succeed('Fetched argument values')
}

function getDownloadOptions(options) {
    // Get credentials from .env file.
    downloadOptions.username = process.env.USERNAME;
    downloadOptions.password = process.env.PASSWORD;

    // If credentials are not set in .env file, get credentials from command line arguments.
    if((downloadOptions.username === undefined || downloadOptions.username.length <=0) && options.credentials !== undefined) {
        downloadOptions.username = options.credentials.split(":")[0];
    }
    if((downloadOptions.password === undefined || downloadOptions.password.length <=0) && options.credentials !== undefined) {
        downloadOptions.password = options.credentials.split(":")[1];
    }

    // If auth type is not none & credentials are missing, exit with error.
    downloadOptions.auth = options.auth;
    if(downloadOptions.auth != undefined && downloadOptions.auth != 'none' && downloadOptions.username == undefined && downloadOptions.password == undefined){
    spinner.fail('Please specify a valid username or password');
    exit(1);
    }

    // Validate report format.
    downloadOptions.format = options.format;
    if (downloadOptions.format.toUpperCase() !== 'PDF' && 
        downloadOptions.format.toUpperCase() !== 'PNG') {
    spinner.fail('Please specify a valid file format: one of PDF or PNG');
    exit(1);
    }

    // Set default filename is not specified.
    downloadOptions.filename = process.env.FILENAME || options.filename;

    // Set url.
    downloadOptions.url = options.url;

    // Set width and height of the window
    downloadOptions.width = Number(options.width);
    downloadOptions.height = Number(options.height);
}

function getEmailOptions(options) {
    // Set transport for the email.
    emailOptions.transport = process.env.TRANSPORT || options.transport;
            
    // Set email addresse if specified.
    emailOptions.sender = process.env.FROM || options.from;
    emailOptions.recipient = process.env.TO || options.to;

    // Set SMTP options.
    emailOptions.smtphost = process.env.SMTP_HOST || options.smtphost;
    emailOptions.smtpport = process.env.SMTP_PORT || options.smtpport;
    emailOptions.smtpsecure = process.env.SMTP_SECURE || options.smtpsecure;
    emailOptions.smtpusername = process.env.SMTP_USERNAME || options.smtpusername;
    emailOptions.smtppassword = process.env.SMTP_PASSWORD || options.smtppassword;
}
