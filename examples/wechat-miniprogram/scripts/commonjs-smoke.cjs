'use strict';

const assert = require('node:assert/strict');

const storage = new Map();
const requests = [];
const navigations = [];
global.wx = {
  login(options) {
    options.success({ code: 'node-login-code', errMsg: 'login:ok' });
  },
  getStorage(options) {
    if (storage.has(options.key)) {
      options.success({ data: storage.get(options.key), errMsg: 'getStorage:ok' });
    } else {
      options.fail({ errMsg: 'getStorage:fail data not found' });
    }
  },
  setStorage(options) {
    storage.set(options.key, options.data);
    options.success({ errMsg: 'setStorage:ok' });
  },
  removeStorage(options) {
    storage.delete(options.key);
    options.success({ errMsg: 'removeStorage:ok' });
  },
  request(options) {
    requests.push(options);
    options.success({
      statusCode: 200,
      data: '{"ok":true}',
      errMsg: 'request:ok',
      header: { 'Content-Type': 'application/json' },
    });
    return {
      abort() {
        throw new Error('the smoke test never cancels a request');
      },
    };
  },
  navigateTo(options) {
    navigations.push({ operation: 'navigateTo', url: options.url, delta: options.delta });
    options.success({ errMsg: 'navigateTo:ok' });
  },
  redirectTo(options) {
    navigations.push({ operation: 'redirectTo', url: options.url, delta: options.delta });
    options.success({ errMsg: 'redirectTo:ok' });
  },
  navigateBack(options) {
    navigations.push({ operation: 'navigateBack', url: options.url, delta: options.delta });
    options.success({ errMsg: 'navigateBack:ok' });
  },
};

const miniAppSdk = require('../miniprogram/libs/kmp-miniapp-sdk.js');

async function main() {
  assert.deepEqual(Object.keys(miniAppSdk), [
    'sdkVersion',
    'storageGet',
    'storageSet',
    'storageRemove',
    'wechatLogin',
    'networkRequest',
    'wechatAppOnLaunch',
    'wechatAppOnShow',
    'wechatAppOnHide',
    'wechatAppLifecycleState',
    'wechatPageOnShow',
    'wechatPageOnHide',
    'wechatPageOnUnload',
    'wechatPageRoute',
    'wechatNavigateTo',
    'wechatRedirectTo',
    'wechatNavigateBack',
  ]);
  assert.equal(miniAppSdk.sdkVersion(), '0.1.0-SNAPSHOT');

  assert.equal(await miniAppSdk.storageGet('missing'), null);
  await miniAppSdk.storageSet('key', 'first');
  assert.equal(await miniAppSdk.storageGet('key'), 'first');
  await miniAppSdk.storageSet('key', 'second');
  assert.equal(await miniAppSdk.storageGet('key'), 'second');
  await miniAppSdk.storageRemove('key');
  assert.equal(await miniAppSdk.storageGet('key'), null);

  const loginResult = await miniAppSdk.wechatLogin();
  assert.equal(loginResult.code, 'node-login-code');

  const response = await miniAppSdk.networkRequest('https://example.com/ping', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json', 'X-Token': 'node-token' },
    body: '{"hello":"world"}',
    timeoutMillis: 5000,
  });
  assert.equal(response.statusCode, 200);
  assert.equal(response.body, '{"ok":true}');
  assert.deepEqual(response.headers, { 'Content-Type': 'application/json' });

  // The transport contract carries text, so the adapter must not let WeChat parse JSON.
  const request = requests[0];
  assert.equal(requests.length, 1);
  assert.equal(request.url, 'https://example.com/ping');
  assert.equal(request.method, 'POST');
  assert.equal(request.dataType, 'text');
  assert.equal(request.data, '{"hello":"world"}');
  assert.equal(request.timeout, 5000);
  assert.deepEqual(request.header, {
    'Content-Type': 'application/json',
    'X-Token': 'node-token',
  });

  const defaulted = await miniAppSdk.networkRequest('https://example.com/defaulted');
  assert.equal(requests[1].method, 'GET');
  assert.equal(requests[1].timeout, undefined);
  assert.equal(requests[1].data, undefined);
  assert.equal(defaulted.statusCode, 200);

  // A method outside the closed SDK method set rejects without reaching the host.
  await assert.rejects(
    miniAppSdk.networkRequest('https://example.com/unsupported', { method: 'TRACE' }),
    /Unsupported HTTP method/,
  );
  assert.equal(requests.length, 2);

  // App lifecycle: WeChat reports onLaunch and then onShow; both report foreground.
  assert.equal(miniAppSdk.wechatAppLifecycleState(), 'BACKGROUND');
  miniAppSdk.wechatAppOnLaunch();
  assert.equal(miniAppSdk.wechatAppLifecycleState(), 'FOREGROUND');
  miniAppSdk.wechatAppOnHide();
  assert.equal(miniAppSdk.wechatAppLifecycleState(), 'BACKGROUND');
  miniAppSdk.wechatAppOnShow();
  assert.equal(miniAppSdk.wechatAppLifecycleState(), 'FOREGROUND');

  // Page lifecycle is WeChat-specific and tracks the page the user is on.
  assert.equal(miniAppSdk.wechatPageRoute(), null);
  miniAppSdk.wechatPageOnShow('pages/index/index');
  assert.equal(miniAppSdk.wechatPageRoute(), 'pages/index/index');
  miniAppSdk.wechatPageOnShow('pages/second/index');
  assert.equal(miniAppSdk.wechatPageRoute(), 'pages/second/index');
  // Going back unloads the deeper page and then reveals the page underneath.
  miniAppSdk.wechatPageOnUnload('pages/second/index');
  assert.equal(miniAppSdk.wechatPageRoute(), null);
  miniAppSdk.wechatPageOnShow('pages/index/index');
  assert.equal(miniAppSdk.wechatPageRoute(), 'pages/index/index');

  await miniAppSdk.wechatNavigateTo('/pages/second/index');
  await miniAppSdk.wechatRedirectTo('/pages/third/index');
  await miniAppSdk.wechatNavigateBack();
  assert.deepEqual(navigations, [
    { operation: 'navigateTo', url: '/pages/second/index', delta: undefined },
    { operation: 'redirectTo', url: '/pages/third/index', delta: undefined },
    { operation: 'navigateBack', url: undefined, delta: undefined },
  ]);

  console.log('[node-smoke] sdkVersion:', miniAppSdk.sdkVersion());
  console.log('[node-smoke] storage: PASS');
  console.log('[node-smoke] auth bootstrap: PASS');
  console.log('[node-smoke] network: PASS');
  console.log('[node-smoke] lifecycle: PASS');
  console.log('[node-smoke] navigation: PASS');
}

main().catch((error) => {
  console.error(error);
  process.exitCode = 1;
});
