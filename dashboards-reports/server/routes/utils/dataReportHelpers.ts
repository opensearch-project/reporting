/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

import esb, { Sort } from 'elastic-builder';
import converter from 'json-2-csv';
import _ from 'lodash';
import moment from 'moment';
import { DATA_REPORT_CONFIG } from './constants';
import {
  buildOpenSearchQuery,
  Filter,
  Query,
  OpenSearchQueryConfig,
} from '../../../../../src/plugins/data/common';

export var metaData = {
  saved_search_id: <string>null,
  report_format: <string>null,
  start: <string>null,
  end: <string>null,
  fields: <string>null,
  type: <string>null,
  timeFieldName: <string>null,
  sorting: <string>null,
  fields_exist: <boolean>false,
  selectedFields: <any>[],
  paternName: <string>null,
  searchSourceJSON: <any>[],
  dateFields: <any>[],
};

// Get the selected columns by the user.
export const getSelectedFields = async (columns) => {
  const selectedFields = [];
  let fields_exist = false;
  for (let column of columns) {
    if (column !== '_source') {
      fields_exist = true;
      selectedFields.push(column);
    } else {
      fields_exist = false;
      selectedFields.push('_source');
    }
  }
  metaData.fields_exist = fields_exist;
  metaData.selectedFields = selectedFields;
};

// Build the OpenSearch query from the meta data
// is_count is set to 1 if we building the count query but 0 if we building the fetch data query
export const buildRequestBody = (report: any, allowLeadingWildcards: boolean, is_count: number) => {
  let esbBoolQuery = esb.boolQuery();
  const searchSourceJSON = report._source.searchSourceJSON;
  const savedObjectQuery: Query = JSON.parse(searchSourceJSON).query;
  const savedObjectFilter: Filter = JSON.parse(searchSourceJSON).filter;
  const savedObjectConfig: OpenSearchQueryConfig = {
    allowLeadingWildcards: allowLeadingWildcards,
    queryStringOptions: {},
    ignoreFilterIfFieldNotInIndex: false,
  }
  const QueryFromSavedObject = buildOpenSearchQuery(
    undefined,
    savedObjectQuery,
    savedObjectFilter,
    savedObjectConfig,
  );
  // Add time range
  if (report._source.timeFieldName && report._source.timeFieldName.length > 0) {
    esbBoolQuery.must(
      esb
        .rangeQuery(report._source.timeFieldName)
        .format('epoch_millis')
        .gte(report._source.start - 1)
        .lte(report._source.end + 1)
    );
  }
  if (is_count) {
    return esb.requestBodySearch().query(esbBoolQuery);
  }

  // Add sorting to the query
  let esbSearchQuery = esb
    .requestBodySearch()
    .query(esbBoolQuery)
    .version(true);

  if (report._source.sorting.length > 0) {
    const sortings: Sort[] = report._source.sorting.map((element: string[]) => {
      return esb.sort(element[0], element[1]);
    });
    esbSearchQuery.sorts(sortings);
  }

  // add selected fields to query
  if (report._source.fields_exist) {
    esbSearchQuery.source({ includes: report._source.selectedFields });
  }
  // Add a customizer to merge queries to generate request body
  let requestBody = _.mergeWith(
    { query: QueryFromSavedObject },
    esbSearchQuery.toJSON(),
    (objValue, srcValue) => {
      if (_.isArray(objValue)) {
        return objValue.concat(srcValue);
      }
    }
  );

  requestBody = addDocValueFields(report, requestBody);
  return requestBody;
};

// Fetch the data from OpenSearch
export const getOpenSearchData = (
  arrayHits,
  report,
  params,
  dateFormat: string
) => {
  let hits: any = [];
  for (let valueRes of arrayHits) {
    for (let data of valueRes.hits) {
      const fields = data.fields;
      // get all the fields of type date and format them to excel format
      for (let dateField of report._source.dateFields) {
        const dateValue = data._source[dateField];
        if (dateValue && dateValue.length !== 0) {
          if (dateValue instanceof Array) {
            // loop through array
            dateValue.forEach((element, index) => {
              data._source[dateField][index] = moment(
                fields[dateField][index]
              ).format(dateFormat);
            });
          } else {
            // The fields response always returns an array of values for each field
            // https://www.elastic.co/guide/en/elasticsearch/reference/master/search-fields.html#search-fields-response
            data._source[dateField] = moment(fields[dateField][0]).format(
              dateFormat
            );
          }
        }
      }
      delete data['fields'];
      if (report._source.fields_exist === true) {
        let result = traverse(data, report._source.selectedFields);
        hits.push(params.excel ? sanitize(result) : result);
      } else {
        hits.push(params.excel ? sanitize(data) : data);
      }
      // Truncate to expected limit size
      if (hits.length >= params.limit) {
        return hits;
      }
    }
  }
  return hits;
};

//Convert the data to Csv format
export const convertToCSV = async (dataset, csvSeparator) => {
  let convertedData: any = [];
  const options = {
    delimiter: { field: csvSeparator, eol: '\n' },
    emptyFieldValue: ' ',
  };
  await converter.json2csvAsync(dataset[0], options).then((csv) => {
    convertedData = csv;
  });
  return convertedData;
};

function flattenHits(hits, result = {}, prefix = '') {
  for (const [key, value] of Object.entries(hits)) {
    if (!hits.hasOwnProperty(key)) continue;
    if (
      value != null &&
      typeof value === 'object' &&
      !Array.isArray(value) &&
      Object.keys(value).length > 0
    ) {
      flattenHits(value, result, prefix + key + '.');
    } else {
      result[prefix.replace(/^_source\./, '') + key] = value;
    }
  }
  return result;
}

//Return only the selected fields
function traverse(data, keys, result = {}) {
  data = flattenHits(data);
  const sourceKeys = Object.keys(data);
  keys.forEach((key) => {
    const value = _.get(data, key, undefined);
    if (value !== undefined) result[key] = value;
    else {
      Object.keys(data)
        .filter((sourceKey) => sourceKey.startsWith(key + '.'))
        .forEach((sourceKey) => (result[sourceKey] = data[sourceKey]));
    }
  });
  return result;
}

/**
 * Escape special characters if field value prefixed with.
 * This is intend to avoid CSV injection in Microsoft Excel.
 * @param doc   document
 */
function sanitize(doc: any) {
  for (const field in doc) {
    if (doc[field] == null) continue;
    if (
      doc[field].toString().startsWith('+') ||
      (doc[field].toString().startsWith('-') &&
        typeof doc[field] !== 'number') ||
      doc[field].toString().startsWith('=') ||
      doc[field].toString().startsWith('@')
    ) {
      doc[field] = "'" + doc[field];
    }
  }
  return doc;
}

const addDocValueFields = (report: any, requestBody: any) => {
  const docValues = [];
  for (const dateType of report._source.dateFields) {
    docValues.push({
      field: dateType,
      format: 'date_hour_minute_second_fraction',
    });
  }
  // elastic-builder doesn't provide function to build docvalue_fields with format,
  // this is a workaround which appends docvalues field to the request body.
  requestBody = {
    ...requestBody,
    docvalue_fields: docValues,
  };
  return requestBody;
};
