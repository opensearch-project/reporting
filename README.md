[![OpenSearch Dashboards Reports CI](https://github.com/opensearch-project/dashboards-reports/workflows/Test%20and%20Build%20OpenSearch%20Dashboards%20Reports/badge.svg)](https://github.com/opensearch-project/dashboards-reports/actions?query=workflow%3A%22Test+and+Build+OpenSearch+Dashboards+Reports%22)
[![Reports Scheduler CI](https://github.com/opensearch-project/dashboards-reports/workflows/Test%20and%20Build%20Reports%20Scheduler/badge.svg)](https://github.com/opensearch-project/dashboards-reports/actions?query=workflow%3A%22Test+and+Build+Reports+Scheduler%22)
[![codecov](https://codecov.io/gh/opensearch-project/dashboards-reports/branch/main/graph/badge.svg?token=YOX0XBW2NA)](https://codecov.io/gh/opensearch-project/dashboards-reports)
[![Documentation](https://img.shields.io/badge/documentation-blue.svg)](https://opensearch.org/docs/dashboards/reporting/)
![PRs welcome!](https://img.shields.io/badge/PRs-welcome!-success)

<img src="https://opensearch.org/assets/img/opensearch-logo-themed.svg" height="64px">

- [OpenSearch Dashboards Reports](#opensearch-dashboards-reports)
- [Contributing](#contributing)
- [Setup](#setup-&-build)
- [Notifications Integration](#notifications-integration)
- [Troubleshooting](#troubleshooting)
- [Code of Conduct](#code-of-conduct)
- [Security](#security)
- [License](#license)
- [Copyright](#copyright)

# OpenSearch Dashboards Reports

OpenSearch Dashboards Reports allows ‘Report Owner’ (engineers, including but not limited to developers, DevOps, IT Engineer, and IT admin) export and share reports from OpenSearch Dashboards dashboards, saved search, alerts and visualizations. It helps automate the process of scheduling reports on an on-demand or a periodical basis (on cron schedules as well). Further, it also automates the process of exporting and sharing reports triggered for various alerts. The feature is present in the Dashboard, Discover, and Visualization tabs. We are currently working on integrating Dashboards Reports with Notifications to enable sharing functionality. After the support is introduced, scheduled reports can be sent to (shared with) self or various stakeholders within the organization. These stakeholders include but are not limited to, executives, managers, engineers (developers, DevOps, IT Engineer) in the form of pdf, hyperlinks, csv, excel via various channels such as email, Slack, and Amazon Chime. However, in order to export, schedule and share reports, report owners should have the necessary permissions as defined under Roles and Privileges.

## Contributing

We welcome you to get involved in development, documentation, testing the OpenSearch Dashboards reports plugin. See our [CONTRIBUTING.md](./CONTRIBUTING.md) and join in.

## Setup & Build

Complete OpenSearch Dashboards Report feature is composed of 2 plugins.

- [OpenSearch Dashboards reports plugin](./dashboards-reports/README.md)
- OpenSearch Reports scheduler plugin

## Notifications Integration

OpenSearch Dashboards Reports integration with [Notifications](https://github.com/opensearch-project/notifications) is currently in progress. Tracking [here](https://github.com/opensearch-project/dashboards-reports/issues/72)

## Troubleshooting

### Fail to launch Chromium

There could be two reasons for this problem

1. You are not having the correct version of headless-chrome matching to the OS that your OpenSearch Dashboards is running. Different versions of headless-chrome can be found [here](https://github.com/opensearch-project/dashboards-reports/releases/tag/chromium-1.12.0.0)

2. Missing additional dependencies. Please refer to [additional dependencies section](./dashboards-reports/rendering-engine/headless-chrome/README.md#additional-libaries) to install required dependencies according to your operating system.

### Missing Font Dependencies
Chromium may not have all of the dependencies you may require to be able to view all of the content of your reports.

If you are using a CentOS/RHEL system, install the following packages:
* [`ipa-gothic-fonts`](https://centos.pkgs.org/7/centos-x86_64/ipa-gothic-fonts-003.03-5.el7.noarch.rpm.html)
* [`xorg-x11-fonts-100dpi`](https://centos.pkgs.org/7/centos-x86_64/xorg-x11-fonts-100dpi-7.5-9.el7.noarch.rpm.html)
* [`xorg-x11-fonts-75dpi`](https://centos.pkgs.org/7/centos-x86_64/xorg-x11-fonts-75dpi-7.5-9.el7.noarch.rpm.html)
* [`xorg-x11-utils`](https://centos.pkgs.org/7/centos-x86_64/xorg-x11-utils-7.5-23.el7.x86_64.rpm.html)
* [`xorg-x11-fonts-cyrillic`](https://centos.pkgs.org/7/centos-x86_64/xorg-x11-fonts-cyrillic-7.5-9.el7.noarch.rpm.html)
* [`xorg-x11-fonts-Type1`](https://centos.pkgs.org/7/centos-x86_64/xorg-x11-fonts-Type1-7.5-9.el7.noarch.rpm.html)
* [`xorg-x11-fonts-misc`](https://centos.pkgs.org/7/centos-x86_64/xorg-x11-fonts-misc-7.5-9.el7.noarch.rpm.html)
* [`fontconfig`](https://www.freedesktop.org/wiki/Software/fontconfig/)
* [`freetype`](https://freetype.org/)


If you are using a Ubuntu/Debian system, install the following packages:
* [`fonts-liberation`](https://packages.debian.org/search?keywords=fonts-liberation)
* [`libfontconfig1`](https://packages.debian.org/sid/libfontconfig1)

The installation command for both systems can be found [here](./dashboards-reports/rendering-engine/headless-chrome/README.md).

## Code of Conduct

This project has adopted the [Amazon Open Source Code of Conduct](CODE_OF_CONDUCT.md). For more information see the [Code of Conduct FAQ](https://aws.github.io/code-of-conduct-faq), or contact [opensource-codeofconduct@amazon.com](mailto:opensource-codeofconduct@amazon.com) with any additional questions or comments.

## Security

If you discover a potential security issue in this project we ask that you notify AWS/Amazon Security via our [vulnerability reporting page](http://aws.amazon.com/security/vulnerability-reporting/). Please do **not** create a public GitHub issue.

## License

See the [LICENSE](./LICENSE) file for our project's licensing. We will ask you to confirm the licensing of your contribution.

## Copyright

Copyright OpenSearch Contributors. See [NOTICE](NOTICE.txt) for details.
