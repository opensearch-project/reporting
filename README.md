<img src="https://opensearch.org/assets/img/opensearch-logo-themed.svg" height="64px">

- [OpenSearch Dashboards Reports](#opensearch-dashboards-reports)
- [Code Summary](#code-summary)
- [Documentation](#documentation)
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

## Code Summary

### Reports-Scheduler

|                              |                                                                                                                                                                          |
| ---------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------ |
| Test and build               | [![Observability OpenSearch Build CI][reports-scheduler-build-badge]][reports-scheduler-build-link]                                                                      |
| Code coverage                | [![codecov][reports-scheduler-codecov-badge]][codecov-link]                                                                                                              |
| Distribution build tests     | [![OpenSearch IT tests][reports-scheduler-it-badge]][reports-scheduler-it-link] [![OpenSearch IT code][reports-scheduler-it-code-badge]][reports-scheduler-it-code-link] |
| Backward compatibility tests | [![BWC tests][bwc-tests-badge]][bwc-tests-link]                                                                                                                          |

### Dashboard-Reports

|                          |                                                                                                                    |
| ------------------------ | ------------------------------------------------------------------------------------------------------------------ |
| Test and build           | [![Observability Dashboards CI][dashboard-reports-build-badge]][dashboard-reports-build-link]                      |
| Code coverage            | [![codecov][dashboard-reports-codecov-badge]][codecov-link]                                                        |
| Distribution build tests | [![cypress tests][cypress-test-badge]][cypress-test-link] [![cypress code][cypress-code-badge]][cypress-code-link] |

### Repository Checks

|              |                                                                 |
| ------------ | --------------------------------------------------------------- |
| DCO Checker  | [![Developer certificate of origin][dco-badge]][dco-badge-link] |
| Link Checker | [![Link Checker][link-check-badge]][link-check-link]            |

### Issues

|                                                                |
| -------------------------------------------------------------- |
| [![good first issues open][good-first-badge]][good-first-link] |
| [![features open][feature-badge]][feature-link]                |
| [![enhancements open][enhancement-badge]][enhancement-link]    |
| [![bugs open][bug-badge]][bug-link]                            |
| [![untriaged open][untriaged-badge]][untriaged-link]           |
| [![nolabel open][nolabel-badge]][nolabel-link]                 |

[dco-badge]: https://github.com/opensearch-project/dashboards-reports/actions/workflows/dco.yml/badge.svg
[dco-badge-link]: https://github.com/opensearch-project/dashboards-reports/actions/workflows/dco.yml
[link-check-badge]: https://github.com/opensearch-project/dashboards-reports/actions/workflows/link-checker.yml/badge.svg
[link-check-link]: https://github.com/opensearch-project/dashboards-reports/actions/workflows/link-checker.yml
[dashboard-reports-build-badge]: https://github.com/opensearch-project/dashboards-reports/actions/workflows/dashboards-reports-test-and-build-workflow.yml/badge.svg
[dashboard-reports-build-link]: https://github.com/opensearch-project/dashboards-reports/actions/workflows/dashboards-reports-test-and-build-workflow.yml
[reports-scheduler-build-badge]: https://github.com/opensearch-project/dashboards-reports/actions/workflows/reports-scheduler-test-and-build-workflow.yml/badge.svg
[reports-scheduler-build-link]: https://github.com/opensearch-project/dashboards-reports/actions/workflows/reports-scheduler-test-and-build-workflow.yml
[dashboard-reports-codecov-badge]: https://codecov.io/gh/opensearch-project/dashboards-reports/branch/main/graphs/badge.svg?flag=dashboards-reports
[reports-scheduler-codecov-badge]: https://codecov.io/gh/opensearch-project/dashboards-reports/branch/main/graphs/badge.svg?flag=reports-scheduler
[codecov-link]: https://codecov.io/gh/opensearch-project/dashboards-reports
[cypress-test-badge]: https://img.shields.io/badge/Cypress%20tests-in%20progress-yellow
[cypress-test-link]: https://github.com/opensearch-project/opensearch-build/issues/1124
[cypress-code-badge]: https://img.shields.io/badge/Cypress%20code-blue
[cypress-code-link]: https://github.com/opensearch-project/dashboards-reports/tree/main/dashboards-reports/.cypress/integration
[reports-scheduler-it-badge]: https://img.shields.io/badge/Reports%20Scheduler%20IT%20tests-in%20progress-yellow
[reports-scheduler-it-link]: https://github.com/opensearch-project/opensearch-build/issues/1124
[reports-scheduler-it-code-badge]: https://img.shields.io/badge/Reports%20Scheduler%20code-blue
[reports-scheduler-it-code-link]: https://github.com/opensearch-project/dashboards-reports/blob/main/reports-scheduler/src/test/kotlin/org/opensearch/reportsscheduler/ReportsSchedulerPluginIT.kt
[bwc-tests-badge]: https://img.shields.io/badge/BWC%20tests-in%20progress-yellow
[bwc-tests-link]: https://github.com/opensearch-project/dashboards-reports/pull/244/files
[good-first-badge]: https://img.shields.io/github/issues/opensearch-project/dashboards-reports/good%20first%20issue.svg
[good-first-link]: https://github.com/opensearch-project/dashboards-reports/issues?q=is%3Aopen+is%3Aissue+label%3A%22good+first+issue%22+
[feature-badge]: https://img.shields.io/github/issues/opensearch-project/dashboards-reports/feature%20request.svg
[feature-link]: https://github.com/opensearch-project/dashboards-reports/issues?q=is%3Aopen+is%3Aissue+label%3A%22feature+request%22+
[bug-badge]: https://img.shields.io/github/issues/opensearch-project/dashboards-reports/bug.svg
[bug-link]: https://github.com/opensearch-project/dashboards-reports/issues?q=is%3Aopen+is%3Aissue+label%3Abug+
[enhancement-badge]: https://img.shields.io/github/issues/opensearch-project/dashboards-reports/enhancement.svg
[enhancement-link]: https://github.com/opensearch-project/dashboards-reports/issues?q=is%3Aopen+is%3Aissue+label%3Aenhancement+
[untriaged-badge]: https://img.shields.io/github/issues/opensearch-project/dashboards-reports/untriaged.svg
[untriaged-link]: https://github.com/opensearch-project/dashboards-reports/issues?q=is%3Aopen+is%3Aissue+label%3Auntriaged+
[nolabel-badge]: https://img.shields.io/github/issues-search/opensearch-project/dashboards-reports?color=yellow&label=no%20label%20issues&query=is%3Aopen%20is%3Aissue%20no%3Alabel
[nolabel-link]: https://github.com/opensearch-project/dashboards-reports/issues?q=is%3Aopen+is%3Aissue+no%3Alabel+

## Documentation

Please see our technical [documentation](https://opensearch.org/docs/dashboards/reporting/) to learn more about its features.

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

- [`ipa-gothic-fonts`](https://centos.pkgs.org/7/centos-x86_64/ipa-gothic-fonts-003.03-5.el7.noarch.rpm.html)
- [`xorg-x11-fonts-100dpi`](https://centos.pkgs.org/7/centos-x86_64/xorg-x11-fonts-100dpi-7.5-9.el7.noarch.rpm.html)
- [`xorg-x11-fonts-75dpi`](https://centos.pkgs.org/7/centos-x86_64/xorg-x11-fonts-75dpi-7.5-9.el7.noarch.rpm.html)
- [`xorg-x11-utils`](https://centos.pkgs.org/7/centos-x86_64/xorg-x11-utils-7.5-23.el7.x86_64.rpm.html)
- [`xorg-x11-fonts-cyrillic`](https://centos.pkgs.org/7/centos-x86_64/xorg-x11-fonts-cyrillic-7.5-9.el7.noarch.rpm.html)
- [`xorg-x11-fonts-Type1`](https://centos.pkgs.org/7/centos-x86_64/xorg-x11-fonts-Type1-7.5-9.el7.noarch.rpm.html)
- [`xorg-x11-fonts-misc`](https://centos.pkgs.org/7/centos-x86_64/xorg-x11-fonts-misc-7.5-9.el7.noarch.rpm.html)
- [`fontconfig`](https://www.freedesktop.org/wiki/Software/fontconfig/)
- [`freetype`](https://freetype.org/)

If you are using a Ubuntu/Debian system, install the following packages:

- [`fonts-liberation`](https://packages.debian.org/search?keywords=fonts-liberation)
- [`libfontconfig1`](https://packages.debian.org/sid/libfontconfig1)

The installation command for both systems can be found [here](./dashboards-reports/rendering-engine/headless-chrome/README.md).

## Code of Conduct

This project has adopted the [Amazon Open Source Code of Conduct](CODE_OF_CONDUCT.md). For more information see the [Code of Conduct FAQ](https://aws.github.io/code-of-conduct-faq), or contact [opensource-codeofconduct@amazon.com](mailto:opensource-codeofconduct@amazon.com) with any additional questions or comments.

## Security

If you discover a potential security issue in this project we ask that you notify AWS/Amazon Security via our [vulnerability reporting page](http://aws.amazon.com/security/vulnerability-reporting/). Please do **not** create a public GitHub issue.

## License

See the [LICENSE](./LICENSE) file for our project's licensing. We will ask you to confirm the licensing of your contribution.

## Copyright

Copyright OpenSearch Contributors. See [NOTICE](NOTICE.txt) for details.
