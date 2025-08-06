## Version 3.2.0 Release Notes

Compatible with OpenSearch and OpenSearch Dashboards version 3.2.0

### Bug Fixes
* Create the report definitions and instances indices in the system context to avoid permissions issues ([#1108](https://github.com/opensearch-project/opensearch-reports/pull/1108))

### Infrastructure
* Add a PR check to run integ tests with security ([#1110](https://github.com/opensearch-project/opensearch-reports/pull/1110))
* Add integtest.sh to specifically run integTestRemote task ([#1112](https://github.com/opensearch-project/opensearch-reports/pull/1112))

### Maintenance
* Upgrade gradle to 8.14.3 and run CI checks with JDK24 ([#1109](https://github.com/opensearch-project/opensearch-reports/pull/1109))