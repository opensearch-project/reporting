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

export const testMessageConfirmationMessage = (
  <EuiText size='s'>
    <EuiIcon type='check'/> Test message sent to selected channels. If no test message is received, try again or check your channel settings in Notifications.
  </EuiText>
);

// TODO: Remove and replace references after connecting to backend
export const placeholderChannels = [
  {
    label: 'Chime',
    id: 'abcdefghijk'
  },
  {
    label: 'Slack',
    id: 'zyxwvutsrq'
  }
]
