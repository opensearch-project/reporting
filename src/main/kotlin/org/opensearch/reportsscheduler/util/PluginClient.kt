/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.reportsscheduler.util

import org.opensearch.transport.client.Client

/**
 * The reports scheduler's client that runs actions as its system subject.
 *
 * @param delegate the client whose actions this wraps
 */
class PluginClient(delegate: Client) : org.opensearch.commons.client.PluginClient(delegate)
