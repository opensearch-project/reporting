## Version 2.19.6 Release Notes

Compatible with OpenSearch and OpenSearch Dashboards version 2.19.6

### Enhancements

* Update maven2 mirror repository URL order ([#1202](https://github.com/opensearch-project/reporting/pull/1202))

### Bug Fixes

* Bump assertj-core to 3.27.7 to fix CVE-2026-24400 (HIGH) XXE vulnerability ([#1195](https://github.com/opensearch-project/reporting/pull/1195))

### Infrastructure

* Add CI mirror to plugin and dependency repositories to avoid Maven Central throttling ([#1199](https://github.com/opensearch-project/reporting/pull/1199))
* Fix maven snapshot publication on 2.19 branch ([#1161](https://github.com/opensearch-project/reporting/pull/1161))
