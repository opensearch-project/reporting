## Reporting Anything

Reporting CLI is a quick out-of-box options to be able to download reports without requiring the use of Dashboards and the Dashboards plugin. 

### Features

- Reporting Anything supports following authentication types for opensearch connection.
    - Basic authentication
    - Cognito authentication
    - SAML authentication
    - No auth
- Reporting Anything supports downloading reports in below file formats.
    - PDF
    - PNG
- Reporting Anything can be used to send email with report as an attachment.   

### Command-line Options

Reporting Anything supports following configurable options.

Option | Default Value | Valid Options | Description
-- | --- | --- | --- |
url | - | - | url of the report
format | pdf | PDF, PNG | file formart of the report
width | 1680 | - | page width in pixels
height | 600 | - | page height in pixels
filename | reporting_anything | - | file name of the report
auth | No auth | BASIC, SAML, COGNITO, NONE | authentication type
credentials | - | - | login credentials in the format of username:password for connecting to url
from | - | - | the email address of the sender
to | - | - | the recipient of the email
transport | - | SES, SMTP | transport for sending the email
smtphost | - | - | the hostname of the SMTP server
smtpport | - | - | the port for connection
smtpusername | - | - | SMTP username
smtppassword | - | - | SMTP password
smtpsecure | - | - | if true the connection will use TLS when connecting to server.

You can also find this information using help command.
```
reporting anything --help
```

### Environment Variable File

Reporting Anything also reads environment variables from .env file inside the project. 

```md
.
Reporting Anything
├── src
│   └── .env
```

- Each line should have format NAME=VALUE
- Lines starting with # are considered as comments.
- There is no special handling of quotation marks.
- Currently supported variables are *CHROMIUM_PATH*, *USERNAME*, *PASSWORD*,  *FILENAME*, *FROM*, *TO*, *TRANSPORT*, *SMTP_HOST*, *SMTP_PORT*, *SMTP_SECURE*, *SMTP_USER*, *SMTP_PASSWORD*.

NOTE: Values from the environment file has higher priority than command line argument. For example, if you add filename as *test* in *.env* file and also add `--filename report` command option, the downloded report's name will be *test*.

### Example

Sample command for downloading a dashboard report with basic authentication in png format
```
reporting-anything --url https://localhost:5601/app/dashboards#/view/7adfa750-4c81-11e8-b3d7-01146121b73d --format png --auth basic --credentials admin:admin
```
Report will be downloaded in the current directory.

### Sending an Email with report attachment using Amazon SES

Prerequisites:
- The sender's email address must be verified on [Amazon SES](https://aws.amazon.com/ses/).
- [AWS CLI](https://docs.aws.amazon.com/cli/latest/userguide/cli-chap-welcome.html) is required to interact with Amazon SES. 
- [Configure](https://docs.aws.amazon.com/cli/latest/userguide/cli-configure-quickstart.html#cli-configure-quickstart-config) basic settings used by AWS CLI.
-  SES transport requires ses:SendRawEmail role.
```
{
  "Statement": [
    {
      "Effect": "Allow",
      "Action": "ses:SendRawEmail",
      "Resource": "*"
    }
  ]
}
```

Sample command to send email with report as an attachment:
```
reporting-anything --url https://localhost:5601/app/dashboards#/view/7adfa750-4c81-11e8-b3d7-01146121b73d --transport SES --from <sender_email_id> --to <recipient_email_id>
```
This example uses default values for all other options.

You can also set *FROM*, *TO*, *TRANSPORT* in .env file and use following command. 
```
reporting-anything --url https://localhost:5601/app/dashboards#/view/7adfa750-4c81-11e8-b3d7-01146121b73d
```

To modify the body of your email, you can simply edit *index.hbs* file.


### Sending an Email with report attachment using SMTP

For sending email using SMTP transport, the options **SMTP_HOST**, **SMTP_PORT**, **SMTP_USER**, **SMTP_PASSWORD**, **SMTP_SECURE** need to be set in .env file.

Once above options are set in .env file, you can send the email using below sample command.
```
reporting-anything --url https://localhost:5601/app/dashboards#/view/7adfa750-4c81-11e8-b3d7-01146121b73d --transport SMTP --from <sender_email_id> --to <recipient_email_id>
```

You can choose to set options using *.env* file or the command line argument values in any combination. Make sure to specify all required values to avoid getting errors.

To modify the body of your email, you can simply edit *index.hbs* file.

## Troubleshooting

- To resolve **MessageRejected: Email address is not verified**, [check](https://aws.amazon.com/premiumsupport/knowledge-center/ses-554-400-message-rejected-error/) this arcticle.