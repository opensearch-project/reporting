## Version 1.0.0.0 Release Notes

Compatible with OpenSearch 1.0.0

### Enhancements
* Use output_only parameter for notebook reports ([#32](https://github.com/opensearch-project/dashboards-reports/pull/32))
* Add Logic to Auto-populate Notebooks from Context menu ([#30](https://github.com/opensearch-project/dashboards-reports/pull/30))
* [Query Builder] Correctly handle matched phrases when a single value is specified or when the match phrases is negated ([#33](https://github.com/opensearch-project/dashboards-reports/pull/33))
* Replace osd header in context menu ([#37](https://github.com/opensearch-project/dashboards-reports/pull/37))
* Remove visualization editor in visualization reports ([#50](https://github.com/opensearch-project/dashboards-reports/pull/50))
* Increase chromium timeout to 100s ([#58](https://github.com/opensearch-project/dashboards-reports/pull/58))
* configure index setting to have default 1 replica and upper bound 2 ([#52](https://github.com/opensearch-project/dashboards-reports/pull/52))
* Add i18n translation support ([#82](https://github.com/opensearch-project/dashboards-reports/pull/82))
* PDF report is no more a screenshot, increasing the overall quality ([#82](https://github.com/opensearch-project/dashboards-reports/pull/82))
* Change Delivery Request Body for Notifications ([#85](https://github.com/opensearch-project/dashboards-reports/pull/85))
* Remove legacy notifications/delivery related code ([#94](https://github.com/opensearch-project/dashboards-reports/pull/94))


### Bug Fixes
* Configure index to have default one replica and upper bound 2 ([#62](https://github.com/opensearch-project/dashboards-reports/pull/62))
* Fix case-sensitive directory name for chromium zip ([#35](https://github.com/opensearch-project/dashboards-reports/pull/35))
* Add condition to fix negative value display ([#51](https://github.com/opensearch-project/dashboards-reports/pull/51))
* Fix csv parsing function ([#53](https://github.com/opensearch-project/dashboards-reports/pull/53))
* Revert .opensearch_dashboards index references to .kibana ([#67](https://github.com/opensearch-project/dashboards-reports/pull/67))
* Bump ws from 7.3.1 to 7.4.6 in /dashboards-reports ([#68](https://github.com/opensearch-project/dashboards-reports/pull/68))
* Better support sorting for csv report based on saved search ([#86](https://github.com/opensearch-project/dashboards-reports/pull/86))
* bump dependency version ([#101](https://github.com/opensearch-project/dashboards-reports/pull/101))
* fix failed cypress integ-testing ([#106](https://github.com/opensearch-project/dashboards-reports/pull/106))
* Fix notebooks context menu ([#109](https://github.com/opensearch-project/dashboards-reports/pull/109))
* Update context menu download request body after schema change ([#115](https://github.com/opensearch-project/dashboards-reports/pull/115))
* Exclude time range from report details for Notebooks ([#117](https://github.com/opensearch-project/dashboards-reports/pull/117))


### Infrastructure
* Update issue template with multiple labels ([#36](https://github.com/opensearch-project/dashboards-reports/pull/36))
* Bump path-parse version to 1.0.7 to address CVE ([#59](https://github.com/opensearch-project/dashboards-reports/pull/59))
* Update README CoC Link ([#56](https://github.com/opensearch-project/dashboards-reports/pull/56))
* Remove dependency on demo.elastic and use local mock html for testing ([#100](https://github.com/opensearch-project/dashboards-reports/pull/100))
* Add code cov back ([#98](https://github.com/opensearch-project/dashboards-reports/pull/98))
* update workflow to rename artifact in kebab case ([#102](https://github.com/opensearch-project/dashboards-reports/pull/102))
* Bump test resource(job-scheduler) to 1.0.0.0 ([#105](https://github.com/opensearch-project/dashboards-reports/pull/105))
* Bump node version, fix workflow and gradle build ([#108](https://github.com/opensearch-project/dashboards-reports/pull/108))


### Documentation
* Add diagrams for integration with Notifications plugin ([#75](https://github.com/opensearch-project/dashboards-reports/pull/75))
* Add Notifications to docs ([#87](https://github.com/opensearch-project/dashboards-reports/pull/87))
* level up markdowns and readme ([#97](https://github.com/opensearch-project/dashboards-reports/pull/97))


### Maintenance
* Bump OpenSearch Dashboards version to 1.0 ([#64](https://github.com/opensearch-project/dashboards-reports/pull/64))
* Bump to version 1.0.0.0 ([#103](https://github.com/opensearch-project/dashboards-reports/pull/103))

### OpenSearch Migration
* Change opendistro to opensearch in email template ([#31](https://github.com/opensearch-project/dashboards-reports/pull/31))
* Update Namespaces/API/Documentation for OpenSearch ([#55](https://github.com/opensearch-project/dashboards-reports/pull/55))
* Migrate multiple commits from OpenDistro Kibana Reports ([#57](https://github.com/opensearch-project/dashboards-reports/pull/57))