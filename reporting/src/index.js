#!/usr/bin/env node
/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

import { sendEmail } from './email-helpers.js';
import { downloadVisualReport } from './download-helpers.js';
import { getCommandArguments } from './arguments.js';

"use strict";

let options = await getCommandArguments();

await downloadVisualReport(
    options.url,
    options.format, 
    options.width, 
    options.height, 
    options.filename, 
    options.auth, 
    options.username, 
    options.password,
    options.tenant
    );

await sendEmail(
    options.filename, 
    options.format, 
    options.sender, 
    options.recipient, 
    options.transport, 
    options.smtphost, 
    options.smtpport, 
    options.smtpsecure, 
    options.smtpusername, 
    options.smtppassword
    );
