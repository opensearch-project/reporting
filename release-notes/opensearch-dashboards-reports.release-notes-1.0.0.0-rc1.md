## 2021-05-25 Version 1.0.0.0-rc1
Compatible with OpenSearch 1.0.0-rc1

### Enhancements
* Use output_only parameter for notebook reports ([#32](https://github.com/opensearch-project/dashboards-reports/pull/32))
* Add Logic to Auto-populate Notebooks from Context menu ([#30](https://github.com/opensearch-project/dashboards-reports/pull/30))
* [Query Builder] Correctly handle matched phrases when a single value is specified or when the match phrases is negated ([#33](https://github.com/opensearch-project/dashboards-reports/pull/33))
* Replace osd header in context menu ([#37](https://github.com/opensearch-project/dashboards-reports/pull/37))
* Remove visualization editor in visualization reports ([#50](https://github.com/opensearch-project/dashboards-reports/pull/50))
* Increase chromium timeout to 100s ([#58](https://github.com/opensearch-project/dashboards-reports/pull/58))

### Bug Fixes
* Configure index to have default one replica and upper bound 2 ([#62](https://github.com/opensearch-project/dashboards-reports/pull/62))
* Fix case-sensitive directory name for chromium zip ([#35](https://github.com/opensearch-project/dashboards-reports/pull/35))
* Add condition to fix negative value display ([#51](https://github.com/opensearch-project/dashboards-reports/pull/51))
* Fix csv parsing function ([#53](https://github.com/opensearch-project/dashboards-reports/pull/53))

### Infrastructure
* Update issue template with multiple labels ([#36](https://github.com/opensearch-project/dashboards-reports/pull/36))
* Bump path-parse version to 1.0.7 to address CVE ([#59](https://github.com/opensearch-project/dashboards-reports/pull/59))
* Update README CoC Link ([#56](https://github.com/opensearch-project/dashboards-reports/pull/56))

### OpenSearch Migration
* Change opendistro to opensearch in email template ([#31](https://github.com/opensearch-project/dashboards-reports/pull/31))
* Update Namespaces/API/Documentation for OpenSearch ([#55](https://github.com/opensearch-project/dashboards-reports/pull/55))
* Migrate multiple commits from OpenDistro Kibana Reports ([#57](https://github.com/opensearch-project/dashboards-reports/pull/57))