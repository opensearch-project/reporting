/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.reportsscheduler.metrics;

/**
 * Defines a generic counter.
 */
public interface Counter<T> {

    /** Increments the count value by 1 unit*/
    void increment();

    /** Increments the count value by n unit*/
    void add(long n);

    /** Retrieves the count value accumulated upto this call*/
    T getValue();

    /** Resets the count value to initial value when Counter is created*/
    void reset();
}
