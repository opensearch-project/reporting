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

import { NOTIFICATIONS_API } from '../../common';

export function NotificationsPlugin(Client: any, config: any, components: any) {
  const clientAction = components.clientAction.factory;

  Client.prototype.notifications = components.clientAction.namespaceFactory();
  const notifications = Client.prototype.notifications.prototype;

  notifications.getConfigs = clientAction({
    url: {
      fmt: NOTIFICATIONS_API.CONFIGS,
    },
    method: 'GET',
  });

  notifications.getEventById = clientAction({
    url: {
      fmt: `${NOTIFICATIONS_API.EVENTS}/<%=eventId%>`,
      req: {
        eventId: {
          type: 'string',
          required: true,
        },
      },
    },
    method: 'GET',
  })

  notifications.sendTestMessage = clientAction({
    url: {
      fmt: `${NOTIFICATIONS_API.TEST_MESSAGE}/<%=configId%>`,
      req: {
        configId: {
          type: 'string',
          required: true,
        },
      },
    },
    method: 'GET',
  });
}