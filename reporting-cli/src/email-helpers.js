/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

import nodemailer from "nodemailer";
import hbs from "nodemailer-express-handlebars";
import ora from 'ora';
import { FORMAT } from './constants.js';
import AWS from "aws-sdk";
const spinner = ora();
let ses;

try {
  process.env.AWS_SDK_LOAD_CONFIG = true;
  ses = new AWS.SES();
} catch (err) {
  // Do not set AWS_SDK_LOAD_CONFIG if aws config file is missing.
}

export async function sendEmail(filename, format, sender, recipient, transport, smtphost, smtpport, smtpsecure, smtpusername, smtppassword, subject) {
  if (transport !== undefined && (transport === 'smtp' || ses !== undefined) && sender !== undefined && recipient !== undefined) {
    spinner.start('Sending email...');
  } else {
    if (transport === undefined && sender === undefined && recipient === undefined) {
      return;
    } else if (transport === undefined) {
      spinner.warn('Transport value is missing');
    } else if (transport === 'ses' && ses === undefined) {
      spinner.warn('aws config not found');
    } else if (sender === undefined || recipient === undefined) {
      spinner.warn('Sender/Recipient value is missing');
    }
    spinner.fail('Skipped sending email');
    return;
  }

  let mailOptions = getmailOptions(format, sender, recipient, `${filename}.${format}`, subject);

  let transporter = getTransporter(transport, smtphost, smtpport, smtpsecure, smtpusername, smtppassword);

  transporter.use("compile", hbs({
    viewEngine: {
      partialsDir: "./views/",
      defaultLayout: ""
    },
    viewPath: "./views/",
    extName: ".hbs"
  }));

  // send email
  await transporter.sendMail(mailOptions, function (err, info) {
    if (err) {
      spinner.fail('Error sending email' + err);
    } else {
      spinner.succeed('Email sent successfully');
    }
  });
}

const getTransporter = (transport, smtphost, smtpport, smtpsecure, smtpusername, smtppassword, transporter) => {
  if (transport === 'ses') {
    transporter = nodemailer.createTransport({
      SES: ses
    });
  } else if (transport === 'smtp') {
    transporter = nodemailer.createTransport({
      host: smtphost,
      port: smtpport,
      secure: smtpsecure,
      auth: {
        user: smtpusername,
        pass: smtppassword,
      }
    });
  }
  return transporter;
}

const getmailOptions = (format, sender, recipient, fileName, emailSubject, mailOptions = {}) => {
  if (format === FORMAT.PNG) {
    mailOptions = {
      from: sender,
      subject: emailSubject,
      to: recipient,
      attachments: [
        {
          filename: fileName,
          path: fileName,
          cid: 'report'
        }],
      template: 'index'
    };
  } else {
    mailOptions = {
      from: sender,
      subject: emailSubject,
      to: recipient,
      attachments: [
        {
          filename: fileName,
          path: fileName,
          contentType: 'application/pdf'
        }],
      template: 'index'
    };
  }
  return mailOptions;
}