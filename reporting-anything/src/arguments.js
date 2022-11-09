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

export async function getCommandArguments() {
    spinner.start('Fetching the arguments values');

    program
        .name('reporting-anything')
        .description('Reporting CLI to download and email reports');

    program
        .addOption(new Option('-u, --url <url>', 'url of report')
            .env('URL'))
        .addOption(new Option('-a, --auth <type>', 'authentication type of the report')
            .default('none')
            .choices(['basic', 'cognito', 'SAML']))
        .addOption(new Option('-c, --credentials <username:password>', 'login credentials')
            .env('USERNAME and PASSWORD'))
        .addOption(new Option('--tenant <tenant>', 'Tenants in OpenSearch dashboards')
            .default('global'))
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
            .choices(['ses', 'smtp'])
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
    return getOptions(options);
}

function getOptions(options) {
    var commandOptions = {
        url: null,
        auth: null,
        username: null,
        password: null,
        tenant: null,
        format: null,
        width: null,
        height: null,
        filename: null,
        transport: null,
        sender: null,
        recipient: null,
        smtphost: null,
        smtpport: null,
        smtpsecure: null,
        smtpusername: null,
        smtppassword: null
      }

    // Set url.
    commandOptions.url = process.env.URL || options.url;
    if (commandOptions.url === undefined || commandOptions.url.length <=0) {
        spinner.fail('Please specify URL');
        exit(1);
    }
    // Remove double quotes if present.
    if (commandOptions.url.length >= 2 && commandOptions.url.charAt(0) == '"' && commandOptions.url.charAt(commandOptions.url.length - 1) == '"')
    {
        commandOptions.url = commandOptions.url.substring(1, commandOptions.url.length - 1)
    }

    // Get credentials from .env file.
    commandOptions.username = process.env.USERNAME;
    commandOptions.password = process.env.PASSWORD;

    // Set tenant
    commandOptions.tenant = options.tenant;

    // If credentials are not set in .env file, get credentials from command line arguments.
    if((commandOptions.username === undefined || commandOptions.username.length <=0) && options.credentials !== undefined) {
        commandOptions.username = options.credentials.split(":")[0];
    }
    if((commandOptions.password === undefined || commandOptions.password.length <=0) && options.credentials !== undefined) {
        commandOptions.password = options.credentials.split(":")[1];
    }

    // If auth type is not none & credentials are missing, exit with error.
    commandOptions.auth = options.auth;
    if(commandOptions.auth != undefined && commandOptions.auth != 'none' && commandOptions.username == undefined && commandOptions.password == undefined){
    spinner.fail('Please specify a valid username or password');
    exit(1);
    }

    // Set report format.
    commandOptions.format = options.format;
    if (commandOptions.format.toUpperCase() !== 'PDF' && 
    commandOptions.format.toUpperCase() !== 'PNG') {
    spinner.fail('Please specify a valid file format: one of PDF or PNG');
    exit(1);
    }

    // Set default filename is not specified.
    commandOptions.filename = process.env.FILENAME || options.filename;

    // Set width and height of the window
    commandOptions.width = Number(options.width);
    commandOptions.height = Number(options.height);

    // Set transport for the email.
    commandOptions.transport = process.env.TRANSPORT || options.transport;
            
    // Set email addresse if specified.
    commandOptions.sender = process.env.FROM || options.from;
    commandOptions.recipient = process.env.TO || options.to;

    // Set SMTP options.
    commandOptions.smtphost = process.env.SMTP_HOST || options.smtphost;
    commandOptions.smtpport = process.env.SMTP_PORT || options.smtpport;
    commandOptions.smtpsecure = process.env.SMTP_SECURE || options.smtpsecure;
    commandOptions.smtpusername = process.env.SMTP_USERNAME || options.smtpusername;
    commandOptions.smtppassword = process.env.SMTP_PASSWORD || options.smtppassword;

    spinner.succeed('Fetched argument values')
    return commandOptions;
}