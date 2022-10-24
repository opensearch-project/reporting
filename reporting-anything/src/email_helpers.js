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

export async function sendEmail(fileName, sender, recipient, format) {

    let mailOptions = getmailOptions(format, sender, recipient, fileName);
  
    console.log('Creating SES transporter');
    let transporter = nodemailer.createTransport({
      SES: ses
    });
    
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
        console.log(err);
        console.log('Error sending email');
  
      } else {
        console.log('Email sent successfully');
      }
    });
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
        //specify the template here
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
        //specify the template here
        template : 'index'
      };
    }
    return mailOptions;
  }