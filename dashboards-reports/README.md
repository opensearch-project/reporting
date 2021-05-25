# OpenSearch Dashboards Reports

OpenSearch Dashboards Reports for Open Distro allows ‘Report Owner’ (engineers, including but not limited to developers, DevOps, IT Engineer, and IT admin) export and share reports from OpenSearch Dashboards dashboards, saved search, alerts and visualizations. It helps automate the process of scheduling reports on an on-demand or a periodical basis (on cron schedules as well). Further, it also automates the process of exporting and sharing reports triggered for various alerts. The feature is present in the Dashboard, Discover, and Visualization tabs. Scheduled reports can be sent to (shared with) self or various stakeholders within the organization such as, including but not limited to, executives, managers, engineers (developers, DevOps, IT Engineer) in the form of pdf, hyperlinks, csv, excel via various channels such as email, slack, Amazon Chime. However, in order to export, schedule and share reports, report owners should have the necessary permissions as defined under Roles and Privileges.

# Request for Comments ( RFC )

Please add your feature requests here [ New Requests ](https://github.com/opensearch-project/dashboards-reports/issues) and view project progress here [RFCs](https://github.com/opensearch-project/dashboards-reports/projects/1).

## Setup

1. Download OpenSearch for the version that matches the [OpenSearch Dashboards version specified in package.json](./package.json#L7).
1. Download the OpenSearch Dashboards source code for the [version specified in package.json](./package.json#L7) you want to set up.

1. Change your node version to the version specified in `.node-version` inside the OpenSearch Dashboards root directory.
1. Create a `plugins` directory inside the OpenSearch Dashboards source code directory, if `plugins` directory doesn't exist.
1. Check out this package from version control into the `plugins` directory.
   ```
   git clone git@github.com:opensearch-project/dashboards-reports.git plugins --no-checkout
   cd plugins
   echo 'dashboards-reports/*' >> .git/info/sparse-checkout
   git config core.sparseCheckout true
   git checkout dev
   ```
1. Run `yarn osd bootstrap` inside `OpenSearch-Dashboards/plugins/dashboards-reports`.

Ultimately, your directory structure should look like this:

<!-- prettier-ignore -->
```md
.
├── OpenSearch-Dashboards
│   └──plugins
│      └── dashboards-reports
```

## Build

To build the plugin's distributable zip simply run `yarn build`.

Example output: `./build/reports-dashboards-0.0.1.zip`

## Run

- `yarn start`

  Starts OpenSearch Dashboards and includes this plugin. OpenSearch Dashboards will be available on `localhost:5601`.

- `yarn test:jest`

  Runs the plugin tests.

## Contributing to OpenSearch Dashboards reports for Open Distro

We welcome you to get involved in development, documentation, testing the OpenSearch Dashboards reports plugin. See our [CONTRIBUTING.md](./CONTRIBUTING.md) and join in.

## Code of Conduct

This project has adopted an [Open Source Code of Conduct](https://opensearch.org/codeofconduct.html).

## Security issue notifications

If you discover a potential security issue in this project we ask that you notify AWS/Amazon Security via our [vulnerability reporting page](http://aws.amazon.com/security/vulnerability-reporting/). Please do **not** create a public GitHub issue.

## License

See the [LICENSE](./LICENSE.txt) file for our project's licensing. We will ask you to confirm the licensing of your contribution.

## Copyright

Copyright 2021 Amazon.com, Inc. or its affiliates. All Rights Reserved.
