'use strict';

const assert = require('node:assert/strict');
const miniAppSdk = require('../miniprogram/libs/kmp-miniapp-sdk.js');

assert.deepEqual(Object.keys(miniAppSdk), ['sdkVersion']);
assert.equal(miniAppSdk.sdkVersion(), '0.1.0-SNAPSHOT');

console.log('[node-smoke] sdkVersion:', miniAppSdk.sdkVersion());
