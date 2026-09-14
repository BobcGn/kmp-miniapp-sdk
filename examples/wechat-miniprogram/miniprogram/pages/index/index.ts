import MiniAppSdk = require('../../libs/kmp-miniapp-sdk');

const sdkVersion: string = MiniAppSdk.sdkVersion();

console.log('[kmp-miniapp-sdk] sdkVersion:', sdkVersion);

Page({
  data: {
    sdkVersion,
  },
});
