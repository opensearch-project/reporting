/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.reportsscheduler.model

<<<<<<< HEAD
import org.opensearch.action.ActionResponse
import org.opensearch.common.xcontent.ToXContent
import org.opensearch.common.xcontent.ToXContentObject
import org.opensearch.common.xcontent.XContentBuilder
import org.opensearch.common.xcontent.XContentFactory
import org.opensearch.rest.RestStatus
=======
import org.opensearch.common.xcontent.XContentFactory
import org.opensearch.core.action.ActionResponse
import org.opensearch.core.rest.RestStatus
import org.opensearch.core.xcontent.ToXContent
import org.opensearch.core.xcontent.ToXContentObject
import org.opensearch.core.xcontent.XContentBuilder
>>>>>>> c254aad (Merge pull request #748 from derek-ho/insights)

/**
 * Base response which give REST status.
 */
internal abstract class BaseResponse : ActionResponse(), ToXContentObject {

    /**
     * {@inheritDoc}
     */
    fun toXContent(params: ToXContent.Params = ToXContent.EMPTY_PARAMS): XContentBuilder {
        return toXContent(XContentFactory.jsonBuilder(), params)
    }

    /**
     * get rest status for the response. Useful override for multi-status response.
     * @return RestStatus for the response
     */
    open fun getStatus(): RestStatus {
        return RestStatus.OK
    }
}
