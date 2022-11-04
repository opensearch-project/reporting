#!/usr/bin/env node
/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

import { sendEmail } from './email_helpers.js';
import { downloadVisualReport } from './report_helpers.js';
import { getCommandArguments, downloadOptions, emailOptions } from './command_options.js';

"use strict";

await getCommandArguments();

await downloadVisualReport(
    downloadOptions.url,
    downloadOptions.format, 
    downloadOptions.width, 
    downloadOptions.height, 
    downloadOptions.filename, 
    downloadOptions.auth, 
    downloadOptions.username, 
    downloadOptions.password
    );

if(emailOptions.transport !== null)    
    await sendEmail(
        downloadOptions.filename, 
        downloadOptions.format, 
        emailOptions.sender, 
        emailOptions.recipient, 
        emailOptions.transport, 
        emailOptions.smtphost, 
        emailOptions.smtpport, 
        emailOptions.smtpsecure, 
        emailOptions.smtpusername, 
        emailOptions.smtppassword
        );

