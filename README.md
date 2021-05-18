[![OpenSearch Dashboards Reports CI](https://github.com/opensearch-project/dashboards-reports/workflows/Test%20and%20Build%20OpenSearch%20Dashboards%20Reports/badge.svg)](https://github.com/opensearch-project/dashboards-reports/actions?query=workflow%3A%22Test+and+Build+OpenSearch+Dashboards+Reports%22)
[![Reports Scheduler CI](https://github.com/opensearch-project/dashboards-reports/workflows/Test%20and%20Build%20Reports%20Scheduler/badge.svg)](https://github.com/opensearch-project/dashboards-reports/actions?query=workflow%3A%22Test+and+Build+Reports+Scheduler%22)
[![codecov](https://codecov.io/gh/opensearch-project/dashboards-reports/branch/dev/graph/badge.svg?token=FBVYQSZD3B)](https://codecov.io/gh/opensearch-project/dashboards-reports)
[![Documentation](https://img.shields.io/badge/documentation-blue.svg)](https://docs-beta.opensearch.org/docs/opensearch-dashboards/reporting/)
![PRs welcome!](https://img.shields.io/badge/PRs-welcome!-success)

# OpenSearch Dashboards Reports for Open Distro

OpenSearch Dashboards Reports for Open Distro allows ‘Report Owner’ (engineers, including but not limited to developers, DevOps, IT Engineer, and IT admin) export and share reports from OpenSearch Dashboards dashboards, saved search, alerts and visualizations. It helps automate the process of scheduling reports on an on-demand or a periodical basis (on cron schedules as well). Further, it also automates the process of exporting and sharing reports triggered for various alerts. The feature is present in the Dashboard, Discover, and Visualization tabs. Scheduled reports can be sent to (shared with) self or various stakeholders within the organization such as, including but not limited to, executives, managers, engineers (developers, DevOps, IT Engineer) in the form of pdf, hyperlinks, csv, excel via various channels such as email, slack, Amazon Chime. However, in order to export, schedule and share reports, report owners should have the necessary permissions as defined under Roles and Privileges.

# Request for Comments ( RFC )

Please add your feature requests here [ New Requests ](https://github.com/opensearch-project/dashboards-reports/issues) and view project progress here [RFCs](https://github.com/opensearch-project/dashboards-reports/projects/1).

## Setup & Build

Complete OpenSearch Dashboards Report feature is composed of 2 plugins. Refer to README in each plugin folder for more details.

- [OpenSearch Dashboards reports plugin](./dashboards-reports/README.md)
- [Reports scheduler ES plugin](./reports-scheduler/README.md)（TODO）

## Troubleshooting

#### Fail to launch Chromium

There could be two reasons for this problem

1. You are not having the correct version of headless-chrome matching to the OS that your OpenSearch Dashboards is running. Different versions of headless-chrome can be found [here](https://github.com/opensearch-project/dashboards-reports/releases/tag/chromium-1.12.0.0)

2. Missing additional dependencies. Please refer to [additional dependencies section](./dashboards-reports/rendering-engine/headless-chrome/README.md#additional-libaries) to install required dependencies according to your operating system.

## Contributing to OpenSearch Dashboards reports for Open Distro

We welcome you to get involved in development, documentation, testing the OpenSearch Dashboards reports plugin. See our [CONTRIBUTING.md](./CONTRIBUTING.md) and join in.

## Code of Conduct

This project has adopted an [Open Source Code of Conduct](https://github.com/opensearch-project/project-website/blob/main/CONTRIBUTING.md#code-of-conduct).

## Security issue notifications

If you discover a potential security issue in this project we ask that you notify AWS/Amazon Security via our [vulnerability reporting page](http://aws.amazon.com/security/vulnerability-reporting/). Please do **not** create a public GitHub issue.

## License

See the [LICENSE](./LICENSE.txt) file for our project's licensing. We will ask you to confirm the licensing of your contribution.

## Copyright

Copyright 2020 Amazon.com, Inc. or its affiliates. All Rights Reserved.
