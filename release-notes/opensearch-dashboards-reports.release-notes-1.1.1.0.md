## Version 1.1.1.0 Release Notes

Compatible with OpenSearch 1.1.0

### Bug Fixes
* Fix quoting and url-encoding ([#153](https://github.com/opensearch-project/dashboards-reports/pull/153))
* fix csv missing fields issue and empty csv on _source fields ([#206](https://github.com/opensearch-project/dashboards-reports/pull/206))
* Revert backend paths to opendistro  ([#218](https://github.com/opensearch-project/dashboards-reports/pull/218))

### Infrastructure
* Fix apt source and link checker for CI ([#245](https://github.com/opensearch-project/dashboards-reports/pull/245))
* Hard-code OpenSearch dependency to 1.1.0 ([#248](https://github.com/opensearch-project/dashboards-reports/pull/245))

### Enhancements
* Support range filters for csv reports ([#185](https://github.com/opensearch-project/dashboards-reports/pull/185))
* Forward all cookies while using headless Chromium ([#194](https://github.com/opensearch-project/dashboards-reports/pull/194))
* Use advanced settings for csv separator and visual report timezone ([#209](https://github.com/opensearch-project/dashboards-reports/pull/209))
* refactor logic for creating DSL from saved object using `buildOpensearchQuery()` ([#213](https://github.com/opensearch-project/dashboards-reports/pull/213))
* Use advanced settings for csv separator and visual report timezone ([#209](https://github.com/opensearch-project/dashboards-reports/pull/209))

### Maintenance
* Bump to version 1.1.1 ([#236](https://github.com/opensearch-project/dashboards-reports/pull/236))
