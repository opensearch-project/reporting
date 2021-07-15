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
import { testMessageConfirmationMessage } from './delivery_constants';
import 'react-mde/lib/styles/css/react-mde-all.css';
import { reportDefinitionParams } from '../create/create_report_definition';
import ReactMDE from 'react-mde';
import { converter } from '../utils';

const styles: CSS.Properties = {
  maxWidth: '800px',
};

export type ReportDeliveryProps = {
  edit: boolean;
  editDefinitionId: string;
  reportDefinitionRequest: reportDefinitionParams;
  httpClientProps: any;
  showEmailRecipientsError: boolean;
  emailRecipientsErrorMessage: string;
};

export function ReportDelivery(props: ReportDeliveryProps) {
  const {
    edit,
    editDefinitionId,
    reportDefinitionRequest,
    httpClientProps,
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

  const handleSendNotification = (e: { target: { checked: boolean | ((prevState: boolean) => boolean); }; }) => {
    setSendNotification(e.target.checked);
  }

  const handleSelectedChannels = (e: React.SetStateAction<never[]>) => {
    setSelectedChannels(e);
  }

  const handleNotificationSubject = (e: { target: { value: React.SetStateAction<string>; }; }) => {
    setNotificationSubject(e.target.value);
  }

  const handleNotificationMessage = (e: React.SetStateAction<string>) => {
    setNotificationMessage(e);
  }

  const handleTestMessageConfirmation = (e: { target: { value: React.SetStateAction<string>; }; }) => {
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
    if (edit) {
      httpClientProps
        .get(`../api/reporting/reportDefinitions/${editDefinitionId}`)
        .then(async (response: { report_definition: { delivery: { delivery_type: string; }; }; }) => {
          
        });
    } else {
      // By default it's set to deliver to OpenSearch Dashboards user
      defaultCreateDeliveryParams();

    }
  }, []);

  const showNotificationsBody = sendNotification ? (
    <div>
      <EuiSpacer />
      <EuiFormRow label='Channels'>
        <EuiComboBox
          id='notificationsChannelSelect'
          placeholder={'Select channels'}
          options={channels}
          selectedOptions={selectedChannels}
          onChange={handleSelectedChannels}
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
