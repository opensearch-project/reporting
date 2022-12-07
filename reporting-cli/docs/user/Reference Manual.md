## Reporting CLI

Reporting CLI is a quick out-of-box options to be able to download reports without requiring the use of Dashboards.

### Features

- Reporting CLI supports following authentication types for opensearch connection.
    - Basic authentication
    - Cognito authentication
    - SAML authentication
    - No auth
- Reporting CLI supports downloading reports in below file formats.
    - PDF
    - PNG
    - CSV
- Reporting CLI can be used to send email with report as an attachment.   

### Command-line Options

Reporting CLI supports following configurable options.

Option | Default Value | Valid Options |  Environment Variable | Description
-- | --- | --- | --- | --- |
url | - | - | URL | url of the report
format | pdf | pdf, png, csv | - | file formart of the report
width | 1680 | - | - | page width in pixels
height | 600 | - | - | page height in pixels
filename | reporting | - | FILENAME | file name of the report
auth | none | basic, saml, cognito, none | - | authentication type
credentials | - | - | USERNAME and PASSWORD | login credentials in the format of username:password for connecting to url
from | - | - | FROM | the email address of the sender
to | - | - | TO | the recipient of the email
transport | - | ses, smtp | TRANSPORT | transport for sending the email
smtphost | - | - | SMTP_HOST | the hostname of the SMTP server
smtpport | - | - | SMTP_PORT | the port for connection
smtpusername | - | - | SMTP_USERNAME | SMTP username
smtppassword | - | - | SMTP_PASSWORD | SMTP password
smtpsecure | - | - | SMTP_SECURE | if true the connection will use TLS when connecting to server.
subject | 'This is an email containing your dashboard report' | - | SUBJECT |Subject for the email
| - | - | - | CHROMIUM_PATH | path to chromium directory

You can also find this information using help command.
```
reporting --help
```

### Environment Variable File

Reporting CLI also reads environment variables from .env file inside the project. 

```md
.
Reporting
├── src
│   └── .env
```

- Each line should have format NAME=VALUE
- Lines starting with # are considered as comments.
- There is no special handling of quotation marks.

NOTE: Values from the command line argument has higher priority than environment file. For example, if you add filename as *test* in *.env* file and also add `--filename report` command option, the downloded report's name will be *report*.

### Example

Sample command for downloading a dashboard report with basic authentication in png format
```
reporting --url https://localhost:5601/app/dashboards#/view/7adfa750-4c81-11e8-b3d7-01146121b73d --format png --auth basic --credentials admin:admin
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
reporting --url https://localhost:5601/app/dashboards#/view/7adfa750-4c81-11e8-b3d7-01146121b73d --transport ses --from <sender_email_id> --to <recipient_email_id>
```
This example uses default values for all other options.

You can also set *FROM*, *TO*, *TRANSPORT* in .env file and use following command. 
```
reporting --url https://localhost:5601/app/dashboards#/view/7adfa750-4c81-11e8-b3d7-01146121b73d
```

To modify the body of your email, you can simply edit *index.hbs* file.


### Sending an Email with report attachment using SMTP

For sending email using SMTP transport, the options **SMTP_HOST**, **SMTP_PORT**, **SMTP_USER**, **SMTP_PASSWORD**, **SMTP_SECURE** need to be set in .env file.

Once above options are set in .env file, you can send the email using below sample command.
```
reporting --url https://localhost:5601/app/dashboards#/view/7adfa750-4c81-11e8-b3d7-01146121b73d --transport smtp --from <sender_email_id> --to <recipient_email_id>
```

You can choose to set options using *.env* file or the command line argument values in any combination. Make sure to specify all required values to avoid getting errors.

To modify the body of your email, you can simply edit *index.hbs* file.

## Limitations
- Supported platforms are Windows x86, Windows x64, Mac Intel, Mac ARM, Linux x86, Linux x64.
  
  For any other platform, users can take advantage of *CHROMIUM_PATH* environment variable to use custom chromium.

- If a URL contains `!`, history expansion needs to be disable temporarity. 

  bash: `set +H`

  zsh: `setopt nobanghist`


  Alternate option would be adding URL value in envirnoment variable as URL="<url-with-!>" 

- All command option currently accept only lower-case letters.

## Troubleshooting

- To resolve **MessageRejected: Email address is not verified**, [check](https://aws.amazon.com/premiumsupport/knowledge-center/ses-554-400-message-rejected-error/) this arcticle.