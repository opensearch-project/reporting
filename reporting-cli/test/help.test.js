/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

const cli = require('./cli.js');

describe('help option', () => {

    test('help', async () => {
        const expectedHelpInformation =
            `Usage: reporting [options]

Reporting CLI to download and email reports

Options:
  -u, --url <url>                        url of report (env: URL)
  -a, --auth <type>                      authentication type of the report (choices: "basic", "cognito", "saml", default: "none")
  -c, --credentials <username:password>  login credentials (env: USERNAME and PASSWORD)
  -t, --tenant <tenant>                  Tenants in OpenSearch dashboards (default: "private")
  -f, --format <type>                    file format of the report (choices: "pdf", "png", "csv", default: "pdf")
  -w, --width <psize>                    window width of the report (default: "1680")
  -l, --height <size>                    window height of the report (default: "600")
  -n, --filename <name>                  file name of the report (default: "reporting", env: FILENAME)
  -e, --transport <method>               transport for sending the email (choices: "ses", "smtp", env: TRANSPORT)
  -s, --from <sender>                    email address of the sender (env: FROM)
  -r, --to <recipient>                   email address of the recipient (env: TO)
  --smtphost <host>                      the hostname of the SMTP server (env: SMTP_HOST)
  --smtpport <port>                      the port for connection (env: SMTP_PORT)
  --smtpsecure <flag>                    use TLS when connecting to server (env: SMTP_SECURE)
  --smtpusername <username>              SMTP username (env: SMTP_USERNAME)
  --smtppassword <password>              SMTP password (env: SMTP_PASSWORD)
  --subject <subject>                    Email Subject (default: "This is an email containing your dashboard report", env: SUBJECT)
  -h, --help                             display help for command
`;
        let result = await cli(['-h'], '.');
        expect(result.code).toBe(0);
        expect(result.stdout).toBe(expectedHelpInformation);
    });
});
