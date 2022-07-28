## Version 1.2.0.0 Release Notes
Compatible with OpenSearch and OpenSearch Dashboards Version 1.2.0

### Bug Fixes
* Fix quoting and url-encoding ([#153](https://github.com/opensearch-project/dashboards-reports/pull/153))
* Remove hard coded localhost when calling API ([#172](https://github.com/opensearch-project/dashboards-reports/pull/172))
* Fix Report Creation after Report Definition Creation ([#196](https://github.com/opensearch-project/dashboards-reports/pull/196))
* fix csv missing fields issue and empty csv on _source fields ([#206](https://github.com/opensearch-project/dashboards-reports/pull/206))
* Revert backend paths to opendistro  ([#218](https://github.com/opensearch-project/dashboards-reports/pull/218))


### Enhancements
* Add metrics for notifications ([#173](https://github.com/opensearch-project/dashboards-reports/pull/173))
* Add logic to build report detail page link and send as part of message for non-email channels ([#182](https://github.com/opensearch-project/dashboards-reports/pull/182))
* Forward all cookies while using headless Chromium ([#194](https://github.com/opensearch-project/dashboards-reports/pull/194))
* Catch Notifications Errors on Details Pages ([#197](https://github.com/opensearch-project/dashboards-reports/pull/197))
* Remove notifications integration from Details pages ([#210](https://github.com/opensearch-project/dashboards-reports/pull/210))
* refactor logic for creating DSL from saved object using `buildOpensearchQuery()` ([#213](https://github.com/opensearch-project/dashboards-reports/pull/213))
* Use advanced settings for csv separator and visual report timezone ([#209](https://github.com/opensearch-project/dashboards-reports/pull/209))
* Build email message from template with reports links ([#184](https://github.com/opensearch-project/dashboards-reports/pull/184))
* Use advanced settings for date format in csv reports ([#186](https://github.com/opensearch-project/dashboards-reports/pull/186))
* Remove Notifications from Create/Edit ([#212](https://github.com/opensearch-project/dashboards-reports/pull/212))
* Remove calling notifications in reports scheduler ([#211](https://github.com/opensearch-project/dashboards-reports/pull/211))
* Taking RBAC settings from Alerting plugin default to false ([#165](https://github.com/opensearch-project/dashboards-reports/pull/165))
* Support range filters for csv reports ([#185](https://github.com/opensearch-project/dashboards-reports/pull/185))
* Integrate notifications backend ([#129](https://github.com/opensearch-project/dashboards-reports/pull/129))


### Documentation
* Update validation for observability notebooks integration ([#174](https://github.com/opensearch-project/dashboards-reports/pull/174))
* Add dco and release drafter workflows ([#217](https://github.com/opensearch-project/dashboards-reports/pull/217))
* update README notification section ([#216](https://github.com/opensearch-project/dashboards-reports/pull/216))


### Maintenance
* Bump to version 1.2 ([#203](https://github.com/opensearch-project/dashboards-reports/pull/203))
* Bump tmpl from 1.0.4 to 1.0.5 in /dashboards-reports ([#164](https://github.com/opensearch-project/dashboards-reports/pull/164))
* rename plugin helper config file name to consistent with OSD ([#180](https://github.com/opensearch-project/dashboards-reports/pull/180))

