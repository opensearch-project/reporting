/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

import nodemailer from "nodemailer";
import hbs from "nodemailer-express-handlebars";
import { FORMAT } from './constants.js';
import AWS from "aws-sdk";
AWS.config.update({region: 'REGION'});
AWS.config = new AWS.Config();
const ses = new AWS.SES({region: "us-west-2"});

export async function sendEmail(filename, format, sender, recipient, transport, smtphost, smtpport, smtpsecure, smtpusername, smtppassword) {
  if(transport !== undefined && sender !== undefined && recipient !== undefined) {
    console.log('Sending email...'); 
  } else {
    if(transport === undefined) {
      console.log('Transport value is missing');
    } else if(sender === undefined || recipient === undefined) {
      console.log('Sender/Recipient value is missing');
    }
    console.log('Skipped sending email');
    return;
  }

  let mailOptions = getmailOptions(format, sender, recipient, `${filename}.${format}`);

  let transporter = getTransporter(transport, smtphost, smtpport, smtpsecure, smtpusername, smtppassword);
    
  transporter.use("compile",hbs({
    viewEngine:{
      partialsDir:"./views/",
      defaultLayout:""
    },
      viewPath:"./views/",
      extName:".hbs"
  }));
    
  // send email
  await transporter.sendMail(mailOptions, function (err, info) {
      if (err) {
        console.log('Error sending email' + err);
      } else {
        console.log('Email sent successfully');
      }
    });
  }
  
const getTransporter = (transport, smtphost, smtpport, smtpsecure, smtpusername, smtppassword, transporter) => {
  if(transport === 'SES') {
    transporter = nodemailer.createTransport({
      SES: ses
    });
  } else if (transport === 'SMTP') {
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

const getmailOptions = (format, sender, recipient, fileName, mailOptions = {}) => {
    if(format === FORMAT.PNG) {
      mailOptions = {
        from: sender,
        subject: 'This is an email containing your dashboard report',
        to: recipient,
        attachments: [
          {
            filename: fileName,
            path: fileName,
            cid: 'report'
          }],
        template : 'index'
      };
    } else {
      mailOptions = {
        from: sender,
        subject: 'This is an email containing your dashboard report',
        to: recipient,
        attachments: [
          {
            filename: fileName,
            path: fileName,
            contentType: 'application/pdf'
          }],
        template : 'index'
      };
    }
    return mailOptions;
  }