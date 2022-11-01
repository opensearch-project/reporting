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

export async function sendEmail(fileName, sender, recipient, format, transport) {

    let mailOptions = getmailOptions(format, sender, recipient, fileName);

    let transporter = getTransporter(transport);
    
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
  
const getTransporter = (transport, transporter) => {
  if(transport === 'SES') {
    transporter = nodemailer.createTransport({
      SES: ses
    });
  } else if (transport === 'SMTP') {
    transporter = nodemailer.createTransport({
      host: process.env.SMTP_HOST,
      port: process.env.SMTP_PORT,
      secure: process.env.SMTP_SECURE || true,
      auth: {
        user: process.env.SMTP_USER, 
        pass: process.env.SMTP_PASSWORD,
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