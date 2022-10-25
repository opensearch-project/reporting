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

Option | Default Value | Description
--- | --- | --- |
url | - | url of the report
format | pdf | file formart of the report
width | 1680 | page width in pixels
height | 600 | page height in pixels
filename | reporting_anything | file name of the report
auth | none | authentication type
credentials | - | login credentials in the format of username:password
from | - | the email address of the sender
to | - | the recipient of the email


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
- Currently supported variables are *CHROMIUM_PATH*, *USERNAME*, *PASSWORD*, *FROM*, *TO* and *FILENAME*.

### Example

Sample command for downloading a dashboard report with basic authentication in png format is
```
reporting-anything --url https://localhost:5601/app/dashboards#/view/7adfa750-4c81-11e8-b3d7-01146121b73d --format png --auth basic --credentials admin:admin
```

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



