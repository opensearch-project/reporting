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

import React from "react";
import { EuiIcon, EuiText } from '@elastic/eui'

/*
 * Copyright 2020 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * A copy of the License is located at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * or in the "license" file accompanying this file. This file is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

export const noDeliveryChannelsSelectedMessage = (
  <EuiText size='s'>
    <EuiIcon type='alert'/> Please select a channel. 
  </EuiText>
)

export const testMessageConfirmationMessage = (
  <EuiText size='s'>
    <EuiIcon type='check'/> Test message sent to selected channels. If no test message is received, try again or check your channel settings in Notifications.
  </EuiText>
);

export const testMessageFailureMessage = (failedChannels: Array<string>) => (
  <EuiText size='s'>
    <EuiIcon type='alert'/> Failed to send test message for some channels. Please adjust channel settings for {failedChannels.toString()}
  </EuiText>
)

export const getChannelsQueryObject = {
  config_type: ['slack', 'email', 'chime', 'webhook', 'sns', 'ses'],
  from_index: 0,
  max_items: 1000,
  sort_field: 'name',
  sort_order: 'asc',
  feature_list: ['reports']
}
