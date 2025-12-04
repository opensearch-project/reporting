/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.reportsscheduler.model

import org.opensearch.common.xcontent.XContentFactory
import org.opensearch.core.action.ActionResponse
import org.opensearch.core.rest.RestStatus
import org.opensearch.core.xcontent.ToXContent
import org.opensearch.core.xcontent.ToXContentObject
import org.opensearch.core.xcontent.XContentBuilder

/**
 * Base response which give REST status.
 */
internal abstract class BaseResponse :
    ActionResponse(),
    ToXContentObject {
    /**
     * {@inheritDoc}
     */
    fun toXContent(params: ToXContent.Params = ToXContent.EMPTY_PARAMS): XContentBuilder = toXContent(XContentFactory.jsonBuilder(), params)

    /**
     * get rest status for the response. Useful override for multi-status response.
     * @return RestStatus for the response
     */
    open fun getStatus(): RestStatus = RestStatus.OK
}
