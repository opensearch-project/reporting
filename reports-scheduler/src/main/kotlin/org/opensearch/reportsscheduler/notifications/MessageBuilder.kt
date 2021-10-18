/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * The OpenSearch Contributors require contributions made to
 * this file be licensed under the Apache-2.0 license or a
 * compatible open source license.
 *
 * Modifications Copyright OpenSearch Contributors. See
 * GitHub history for details.
 */

package org.opensearch.reportsscheduler.notifications

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.opensearch.commons.utils.logger
import java.util.Base64

/**
 * Provide functions to build email message with html template, or pure text message for non-email channels
 */
internal object MessageBuilder {
    private val log by logger(MessageBuilder::class.java)

    private const val LOGO_ID = "logo"
    private const val REPORT_NAME_ID = "report_name"
    private const val OPEN_IN_REPORTING_BUTTON_ID = "open_in_reporting_button"
    private const val OPTIONAL_MESSAGE_ID = "optional_message"
    private const val EMAIL_HTML_TEMPLATE_PATH = "/notifications/template/email_content_template.html"
    private const val LOGO_PATH = "/notifications/template/logo.png"
    // load logo and html template
    private val templateHtml = javaClass.getResource(EMAIL_HTML_TEMPLATE_PATH)!!.readText()
    private val logo = javaClass.getResource(LOGO_PATH)!!.readBytes()
    private val encodedLogo: String = Base64.getEncoder().encodeToString(logo)

    fun buildEmailMessageWithTemplate(htmlDescription: String, reportLink: String, reportName: String): String {
        val doc: Document = Jsoup.parse(templateHtml)
        doc.getElementById(LOGO_ID)?.attr("src", "data:image/png;base64,$encodedLogo")
        doc.getElementById(REPORT_NAME_ID)?.attr("href", reportLink)?.text(reportName)
        doc.getElementById(OPEN_IN_REPORTING_BUTTON_ID)?.attr("href", reportLink)
        doc.getElementById(OPTIONAL_MESSAGE_ID)?.html(htmlDescription)

        return doc.html()
    }

    fun buildTextMessage(textDescription: String, reportLink: String, reportName: String): String {
        return "$textDescription\nYour Report [$reportName] is now available at $reportLink"
    }
}
