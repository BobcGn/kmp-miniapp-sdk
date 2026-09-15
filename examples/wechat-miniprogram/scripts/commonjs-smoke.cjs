'use strict';

const assert = require('node:assert/strict');

const storage = new Map();
const requests = [];
const navigations = [];

// What this fake base library claims to provide. The runtime-inspection calls
// below are the only ones the SDK asks before touching a capability.
const supportedSchemas = new Set([
  'getStorage',
  'setStorage',
  'removeStorage',
  'request',
  'getAppBaseInfo',
  'getSetting',
  'authorize',
  'openSetting',
]);

// Permission state this fake host holds, plus a record of what it was asked to
// do. Nothing here may run before the test explicitly asks for it.
const scopeDecisions = new Map();
const authorizeCalls = [];
let openSettingCalls = 0;

function authSettingSnapshot() {
  const authSetting = {};
  for (const [scope, granted] of scopeDecisions) {
    authSetting[scope] = granted;
  }
  return authSetting;
}
const baseLibraryVersion = '3.17.3';

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
  canIUse(schema) {
    return supportedSchemas.has(schema);
  },
  getAppBaseInfo() {
    return { SDKVersion: baseLibraryVersion, version: '8.0.5' };
  },
  getDeviceInfo() {
    return { platform: 'devtools' };
  },
  getSetting(options) {
    options.success({ authSetting: authSettingSnapshot(), errMsg: 'getSetting:ok' });
  },
  authorize(options) {
    authorizeCalls.push(options.scope);
    scopeDecisions.set(options.scope, true);
    options.success({ errMsg: 'authorize:ok' });
  },
  openSetting(options) {
    openSettingCalls += 1;
    options.success({ authSetting: authSettingSnapshot(), errMsg: 'openSetting:ok' });
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
    'capabilitySupport',
    'requireCapability',
    'wechatRuntimeInfo',
    'wechatCanIUse',
    'permissionState',
    'requestPermission',
    'openPermissionSettings',
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

  // Runtime detection reads the host rather than a hardcoded list.
  const runtimeInfo = miniAppSdk.wechatRuntimeInfo();
  assert.equal(runtimeInfo.baseLibraryVersion, baseLibraryVersion);
  assert.equal(runtimeInfo.platform, 'devtools');
  assert.equal(runtimeInfo.isDeveloperTools, true);

  assert.equal(miniAppSdk.wechatCanIUse('getStorage'), true);
  assert.equal(miniAppSdk.wechatCanIUse('scanCode'), false);

  assert.equal(miniAppSdk.capabilitySupport('storage').state, 'Supported');
  assert.equal(miniAppSdk.capabilitySupport('network').state, 'Supported');
  assert.equal(miniAppSdk.capabilitySupport('lifecycle').state, 'Supported');
  // Base library 3.17.3 is new enough for the inspection API the SDK probes.
  assert.equal(miniAppSdk.capabilitySupport('wechat.runtime-detection').state, 'Supported');
  // A capability the SDK does not gate is never assumed present.
  assert.equal(miniAppSdk.capabilitySupport('not-a-capability').state, 'Unsupported');

  miniAppSdk.requireCapability('storage');
  assert.throws(() => miniAppSdk.requireCapability('not-a-capability'));

  // Loading the module must not have asked the host for anything. A permission
  // prompt may only ever follow an explicit user gesture.
  assert.deepEqual(authorizeCalls, []);
  assert.equal(miniAppSdk.wechatCanIUse('getSetting'), true);

  // A permission the host holds no decision for is not requested yet.
  assert.equal(await miniAppSdk.permissionState('microphone'), 'NotRequested');

  // Asking reaches the host once, through the adapter's scope mapping.
  assert.equal(await miniAppSdk.requestPermission('microphone'), 'Granted');
  assert.deepEqual(authorizeCalls, ['scope.record']);

  // Every read comes from the host rather than from a remembered answer.
  assert.equal(await miniAppSdk.permissionState('microphone'), 'Granted');
  scopeDecisions.set('scope.record', false);
  assert.equal(await miniAppSdk.permissionState('microphone'), 'Denied');

  // A refusal is reported as denied, and no second prompt is attempted.
  await assert.rejects(miniAppSdk.requestPermission('microphone'), /already refused/);
  assert.equal(authorizeCalls.length, 1);

  // The settings page can change the decision; the state is read afterwards.
  scopeDecisions.set('scope.record', true);
  assert.equal(await miniAppSdk.openPermissionSettings('microphone'), 'Granted');
  assert.equal(openSettingCalls, 1);

  // A permission this SDK does not map is refused rather than forwarded.
  await assert.rejects(miniAppSdk.permissionState('not-a-permission'));

  console.log('[node-smoke] sdkVersion:', miniAppSdk.sdkVersion());
  console.log('[node-smoke] storage: PASS');
  console.log('[node-smoke] auth bootstrap: PASS');
  console.log('[node-smoke] network: PASS');
  console.log('[node-smoke] lifecycle: PASS');
  console.log('[node-smoke] navigation: PASS');
  console.log('[node-smoke] runtime detection: PASS');
  console.log('[node-smoke] permission: PASS');
}

main().catch((error) => {
  console.error(error);
  process.exitCode = 1;
});
