/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * The OpenSearch Contributors require contributions made to
 * this file be licensed under the Apache-2.0 license or a
 * compatible open source license.
 */

module.exports = {
  presets: [
    require('@babel/preset-env', {
      targets: { node: '10' },
    }),
    require('@babel/preset-react'),
    require('@babel/preset-typescript'),
  ],
  plugins: [
    require('@babel/plugin-proposal-class-properties'),
    require('@babel/plugin-proposal-object-rest-spread'),
    ['@babel/plugin-transform-modules-commonjs', { allowTopLevelThis: true }],
    [require('@babel/plugin-transform-runtime'), { regenerator: true }],
  ],
};
