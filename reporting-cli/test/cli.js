/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

const path = require('path');
const exec = require('child_process').exec;

module.exports = function (args, cwd) {
  return new Promise(resolve => {
    exec(`node ${path.resolve('./src/index.js')} ${args.join(' ')}`,
      { cwd },
      (error, stdout, stderr) => {
        resolve({
          code: error && error.code ? error.code : 0,
          error,
          stdout,
          stderr
        })
      })
  })
}