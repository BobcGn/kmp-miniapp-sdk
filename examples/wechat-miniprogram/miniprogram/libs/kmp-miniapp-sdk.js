'use strict';

const kotlinModule = require('./kmp-miniapp-sdk-kotlin.js');
const miniAppExports = kotlinModule.io.github.bobcgn.miniapp.export.MiniAppExports;

module.exports = {
  sdkVersion: function sdkVersion() {
    return miniAppExports.sdkVersion();
  },
};
