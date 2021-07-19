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

import React, { useEffect, useState } from 'react';
import {
  EuiFormRow,
  EuiPageHeader,
  EuiTitle,
  EuiPageContent,
  EuiPageContentBody,
  EuiHorizontalRule,
  EuiSpacer,
  EuiCheckbox,
  EuiComboBox,
  EuiFieldText,
  EuiButton,
} from '@elastic/eui';
import CSS from 'csstype';
import { placeholderChannels, testMessageConfirmationMessage } from './delivery_constants';
import 'react-mde/lib/styles/css/react-mde-all.css';
import { reportDefinitionParams } from '../create/create_report_definition';
import ReactMDE from 'react-mde';
import { converter } from '../utils';

const styles: CSS.Properties = {
  maxWidth: '800px',
};

// TODO: add to schema to avoid need for export
export let includeDelivery = false;

export type ReportDeliveryProps = {
  edit: boolean;
  editDefinitionId: string;
  reportDefinitionRequest: reportDefinitionParams;
  httpClientProps: any;
  showDeliveryChannelError: boolean;
  deliveryChannelError: string;
};

export function ReportDelivery(props: ReportDeliveryProps) {
  const {
    edit,
    editDefinitionId,
    reportDefinitionRequest,
    httpClientProps,
    showDeliveryChannelError,
    deliveryChannelError,
  } = props;

  const [sendNotification, setSendNotification] = useState(false);
  const [channels, setChannels] = useState([]);
  const [selectedChannels, setSelectedChannels] = useState([]);
  const [notificationSubject, setNotificationSubject] = useState('');
  const [notificationMessage, setNotificationMessage] = useState('');
  const [selectedTab, setSelectedTab] = React.useState<'write' | 'preview'>(
    'write'
  );
  const [testMessageConfirmation, setTestMessageConfirmation] = useState('');

  const handleSendNotification = (e: { target: { checked: boolean }; }) => {
    setSendNotification(e.target.checked);
    includeDelivery = e.target.checked;
  }

  const handleSelectedChannels = (e: Array<{ label: string, id: string}>) => {
    setSelectedChannels(e);
    reportDefinitionRequest.delivery.configIds = [];
    for (let i = 0; i < e.length; ++i) {
      reportDefinitionRequest.delivery.configIds.push(e[i].id);
    }
  }

  const handleNotificationSubject = (e: { target: { value: string }; }) => {
    setNotificationSubject(e.target.value);
    reportDefinitionRequest.delivery.title = e.target.value;
  }

  const handleNotificationMessage = (e: string) => {
    setNotificationMessage(e);
    reportDefinitionRequest.delivery.textDescription = e.toString();
    reportDefinitionRequest.delivery.htmlDescription = converter.makeHtml(e.toString());
  }

  const handleTestMessageConfirmation = (e: string) => {
    setTestMessageConfirmation(e);
  }

  const defaultCreateDeliveryParams = () => {
    reportDefinitionRequest.delivery = {
      configIds: [],
      title: '',
      textDescription: '',
      htmlDescription: ''
    };
  };

  const sendTestNotificationsMessage = () => {
    // implement send message
    // on success, set test message confirmation message
    handleTestMessageConfirmation(testMessageConfirmationMessage)
  }

  useEffect(() => {
    // to replace with actual channels from notifications plugin
    setChannels(placeholderChannels);
    if (edit) {
      httpClientProps
        .get(`../api/reporting/reportDefinitions/${editDefinitionId}`)
        .then(async (response: any) => {
          if (response.report_definition.delivery.configIds.length > 0) {
            // add config IDs
            handleSendNotification({target: {checked: true}});
            let delivery = response.report_definition.delivery;
            let editChannelOptions = [];
            for (let i = 0; i < delivery.configIds.length; ++i) {
              for (let j = 0; j < placeholderChannels.length; ++j) {
                if (delivery.configIds[i] === placeholderChannels[j].id) {
                  let editChannelOption = {
                    label: placeholderChannels[j].label,
                    id: placeholderChannels[j].id
                  };
                  
                  editChannelOptions.push(editChannelOption);                  
                }
              }
            }
            setSelectedChannels(editChannelOptions);
            setNotificationSubject(delivery.title);
            setNotificationMessage(delivery.textDescription);
            reportDefinitionRequest.delivery = delivery;
          }
        });
    } else {
      defaultCreateDeliveryParams();
    }
  }, []);

  const showNotificationsBody = sendNotification ? (
    <div>
      <EuiSpacer />
      <EuiFormRow 
        label='Channels'
        isInvalid={showDeliveryChannelError}
        error={deliveryChannelError}
      >
        <EuiComboBox
          id='notificationsChannelSelect'
          placeholder={'Select channels'}
          options={channels}
          selectedOptions={selectedChannels}
          onChange={handleSelectedChannels}
          isClearable={true}
        />
      </EuiFormRow>
      <EuiSpacer />
      <EuiFormRow
        label='Notification subject'
        helpText='Required if at least one channel type is Email.'
        style={styles}
      >
        <EuiFieldText
          placeholder={'Enter notification message subject'}
          fullWidth={true}
          value={notificationSubject}
          onChange={handleNotificationSubject}
        />
      </EuiFormRow>
      <EuiSpacer />
      <EuiFormRow
        label='Notification message'
        helpText='Embed variables in your message using Mustache template.'
        style={styles}
      >
        <ReactMDE
          value={notificationMessage}
          onChange={handleNotificationMessage}
          selectedTab={selectedTab}
          onTabChange={setSelectedTab}
          toolbarCommands={[
            ['header', 'bold', 'italic', 'strikethrough'],
            ['unordered-list', 'ordered-list', 'checked-list'],
          ]}
          generateMarkdownPreview={(markdown) =>
            Promise.resolve(converter.makeHtml(markdown))
          }
        />
      </EuiFormRow>
      <EuiSpacer />
      <EuiFormRow
        helpText={testMessageConfirmation}
        fullWidth={true}
      >
        <EuiButton
          onClick={sendTestNotificationsMessage}
        >
          Send test message
        </EuiButton>
      </EuiFormRow>
    </div>
  ) : null;

  return (
    <EuiPageContent panelPaddingSize={'l'}>
      <EuiPageHeader>
        <EuiTitle>
          <h2>Notification settings</h2>
        </EuiTitle>
      </EuiPageHeader>
      <EuiHorizontalRule />
      <EuiPageContentBody>
        <EuiCheckbox
          id='notificationsDeliveryCheckbox'
          label='Send notification when report is available'
          checked={sendNotification}
          onChange={handleSendNotification}
        />
        {showNotificationsBody}
      </EuiPageContentBody>
    </EuiPageContent>
  );
}
