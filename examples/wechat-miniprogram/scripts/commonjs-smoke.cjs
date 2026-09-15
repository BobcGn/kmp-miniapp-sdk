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
  'getPrivacySetting',
  'requirePrivacyAuthorize',
  'checkSession',
  'getClipboardData',
  'setClipboardData',
  'vibrateShort',
  'vibrateLong',
  'getFileSystemManager',
]);

// Permission state this fake host holds, plus a record of what it was asked to
// do. Nothing here may run before the test explicitly asks for it.
const scopeDecisions = new Map();
const authorizeCalls = [];
let openSettingCalls = 0;

// Privacy is a separate host condition from the permissions above, so it gets
// its own state and its own record of what the host was asked to do.
let privacyRequired = true;
let privacyAuthorizeFails = false;
const privacyQueries = [];
const privacyAuthorizations = [];
const privacyContractName = '《node smoke privacy contract》';

// The WeChat client login session is its own condition, asked about separately.
let sessionFailure = null;
const sessionCheckCalls = [];
let loginCalls = 0;

// Clipboard and haptics are WeChat device capabilities. The fake records what it
// was asked to do; it cannot show that anything vibrated, and this test does not
// claim otherwise.
let clipboardText = '';
const clipboardWrites = [];
const vibrateCalls = [];

// The file system is an in-memory sandbox. The fake exposes the manager as a
// host object with its methods, because the SDK probes each method rather than
// assuming the manager implies them.
const sandboxRoot = '/node-sandbox';
const sandboxFiles = new Map();
const fileSystemCalls = [];

function fileSystemManager() {
  return {
    readFile(options) {
      fileSystemCalls.push({ operation: 'readFile', path: options.filePath, encoding: options.encoding });
      if (sandboxFiles.has(options.filePath)) {
        options.success({ data: sandboxFiles.get(options.filePath), errMsg: 'readFile:ok' });
      } else {
        options.fail({ errMsg: 'readFile:fail no such file or directory ' + options.filePath });
      }
    },
    writeFile(options) {
      fileSystemCalls.push({
        operation: 'writeFile',
        path: options.filePath,
        data: options.data,
        encoding: options.encoding,
      });
      sandboxFiles.set(options.filePath, options.data);
      options.success({ errMsg: 'writeFile:ok' });
    },
    access(options) {
      fileSystemCalls.push({ operation: 'access', path: options.path });
      if (sandboxFiles.has(options.path)) {
        options.success({ errMsg: 'access:ok' });
      } else {
        options.fail({ errMsg: 'access:fail no such file or directory ' + options.path });
      }
    },
    unlink(options) {
      fileSystemCalls.push({ operation: 'unlink', path: options.filePath });
      if (sandboxFiles.has(options.filePath)) {
        sandboxFiles.delete(options.filePath);
        options.success({ errMsg: 'unlink:ok' });
      } else {
        options.fail({ errMsg: 'unlink:fail no such file or directory ' + options.filePath });
      }
    },
  };
}

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
    loginCalls += 1;
    options.success({ code: 'node-login-code', errMsg: 'login:ok' });
  },
  checkSession(options) {
    sessionCheckCalls.push(1);
    if (sessionFailure === null) {
      options.success({ errMsg: 'checkSession:ok' });
    } else {
      options.fail({ errMsg: sessionFailure });
    }
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
  getPrivacySetting(options) {
    privacyQueries.push(1);
    options.success({
      needAuthorization: privacyRequired,
      privacyContractName,
      errMsg: 'getPrivacySetting:ok',
    });
  },
  getClipboardData(options) {
    options.success({ data: clipboardText, errMsg: 'getClipboardData:ok' });
  },
  setClipboardData(options) {
    clipboardWrites.push(options.data);
    clipboardText = options.data;
    options.success({ errMsg: 'setClipboardData:ok' });
  },
  vibrateShort(options) {
    vibrateCalls.push('short');
    options.success({ errMsg: 'vibrateShort:ok' });
  },
  vibrateLong(options) {
    vibrateCalls.push('long');
    options.success({ errMsg: 'vibrateLong:ok' });
  },
  getFileSystemManager() {
    return fileSystemManager();
  },
  env: {
    USER_DATA_PATH: sandboxRoot,
  },
  requirePrivacyAuthorize(options) {
    privacyAuthorizations.push(1);
    if (privacyAuthorizeFails) {
      options.fail({ errMsg: 'requirePrivacyAuthorize:fail privacy permission is not authorized' });
      return;
    }
    privacyRequired = false;
    options.success({ errMsg: 'requirePrivacyAuthorize:ok' });
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
    'wechatCheckSession',
    'wechatGetClipboardText',
    'wechatSetClipboardText',
    'wechatVibrateShort',
    'wechatVibrateLong',
    'wechatUserDataPath',
    'wechatReadTextFile',
    'wechatWriteTextFile',
    'wechatFileExists',
    'wechatRemoveFile',
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
    'privacyStatus',
    'requestPrivacyAuthorization',
    'requirePrivacySatisfied',
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

  // Loading the module must not have touched privacy either.
  assert.deepEqual(privacyQueries, []);
  assert.deepEqual(privacyAuthorizations, []);

  // The host states its requirement, and the SDK reports it unchanged.
  const privacy = await miniAppSdk.privacyStatus();
  assert.equal(privacy.requirement, 'REQUIRED');
  assert.equal(privacy.contractName, privacyContractName);

  // Accepting the contract clears the host's requirement.
  assert.equal(await miniAppSdk.requestPrivacyAuthorization(), 'Authorized');
  assert.equal((await miniAppSdk.privacyStatus()).requirement, 'NOT_REQUIRED');

  // A refusal is a result rather than a rejection.
  privacyRequired = true;
  privacyAuthorizeFails = true;
  assert.equal(await miniAppSdk.requestPrivacyAuthorization(), 'Refused');

  // The precondition point reports the host's requirement and starts no prompt.
  await assert.rejects(miniAppSdk.requirePrivacySatisfied());
  assert.equal(privacyAuthorizations.length, 2);

  privacyAuthorizeFails = false;
  privacyRequired = false;
  await miniAppSdk.requirePrivacySatisfied();
  assert.equal(privacyAuthorizations.length, 2);

  // Privacy and permission are separate host conditions.
  assert.equal(miniAppSdk.wechatCanIUse('getPrivacySetting'), true);
  assert.equal(scopeDecisions.has('scope.record'), true);

  // Loading the module must not have checked anything. The auth-bootstrap check
  // above has already asked for one code, so the baseline is recorded here: what
  // matters is that checking the session never adds another.
  assert.deepEqual(sessionCheckCalls, []);
  const loginsBeforeSessionCheck = loginCalls;

  // An intact client login session is reported as Valid.
  assert.equal(await miniAppSdk.wechatCheckSession(), 'Valid');
  assert.equal(loginCalls, loginsBeforeSessionCheck);

  // The failure callback itself means the session is invalid; its message is not ABI.
  sessionFailure = 'checkSession:fail session time out, need relogin';
  assert.equal(await miniAppSdk.wechatCheckSession(), 'Invalid');
  // An invalid session does not acquire a new code by itself.
  assert.equal(loginCalls, loginsBeforeSessionCheck);

  // A bare failure is also invalid, independent of localized host wording.
  sessionFailure = 'checkSession:fail';
  assert.equal(await miniAppSdk.wechatCheckSession(), 'Invalid');
  assert.equal(loginCalls, loginsBeforeSessionCheck);
  sessionFailure = null;

  // Loading the module must not have touched the clipboard or the vibrator.
  assert.deepEqual(clipboardWrites, []);
  assert.deepEqual(vibrateCalls, []);

  // The four device capabilities are gated separately and all read Supported here.
  for (const capability of [
    'wechat.clipboard-read',
    'wechat.clipboard-write',
    'wechat.vibrate-short',
    'wechat.vibrate-long',
  ]) {
    assert.equal(miniAppSdk.capabilitySupport(capability).state, 'Supported');
  }

  // The write reaches the host with the exact text, and the read returns it.
  const clipboardTestText = 'kmp-miniapp-sdk clipboard test';
  await miniAppSdk.wechatSetClipboardText(clipboardTestText);
  assert.deepEqual(clipboardWrites, [clipboardTestText]);
  assert.equal(await miniAppSdk.wechatGetClipboardText(), clipboardTestText);

  // An empty clipboard reads as an empty string rather than failing.
  clipboardText = '';
  assert.equal(await miniAppSdk.wechatGetClipboardText(), '');

  // A host answer that is not text is reported rather than silently coerced.
  clipboardText = 42;
  await assert.rejects(miniAppSdk.wechatGetClipboardText());

  // Each vibration reaches its own host API exactly once.
  await miniAppSdk.wechatVibrateShort();
  await miniAppSdk.wechatVibrateLong();
  assert.deepEqual(vibrateCalls, ['short', 'long']);

  // Loading the module must not have touched the file system.
  assert.deepEqual(fileSystemCalls, []);

  // The file operations are gated separately and all read Supported here.
  for (const capability of [
    'wechat.filesystem-read',
    'wechat.filesystem-write',
    'wechat.filesystem-access',
    'wechat.filesystem-remove',
    'wechat.filesystem-sandbox-path',
  ]) {
    assert.equal(miniAppSdk.capabilitySupport(capability).state, 'Supported');
  }

  // The sandbox root comes from the host rather than being guessed.
  assert.equal(miniAppSdk.wechatUserDataPath(), sandboxRoot);
  const testPath = `${sandboxRoot}/kmp-miniapp-sdk-bob72.txt`;
  const testContent = 'kmp-miniapp-sdk bob72 test';

  // write -> access true -> read matched -> remove -> access false.
  assert.equal(await miniAppSdk.wechatFileExists(testPath), false);

  await miniAppSdk.wechatWriteTextFile(testPath, testContent);
  assert.equal(await miniAppSdk.wechatFileExists(testPath), true);
  assert.equal(await miniAppSdk.wechatReadTextFile(testPath), testContent);

  await miniAppSdk.wechatRemoveFile(testPath);
  assert.equal(await miniAppSdk.wechatFileExists(testPath), false);

  // Every call carried UTF-8, because a read without an encoding returns bytes
  // the text contract cannot carry.
  const readCall = fileSystemCalls.find((call) => call.operation === 'readFile');
  const writeCall = fileSystemCalls.find((call) => call.operation === 'writeFile');
  assert.equal(readCall.encoding, 'utf8');
  assert.equal(writeCall.encoding, 'utf8');
  assert.equal(writeCall.data, testContent);

  // Removing a file that is not there follows the host contract and fails.
  await assert.rejects(miniAppSdk.wechatRemoveFile(testPath));

  console.log('[node-smoke] sdkVersion:', miniAppSdk.sdkVersion());
  console.log('[node-smoke] storage: PASS');
  console.log('[node-smoke] auth bootstrap: PASS');
  console.log('[node-smoke] network: PASS');
  console.log('[node-smoke] lifecycle: PASS');
  console.log('[node-smoke] navigation: PASS');
  console.log('[node-smoke] runtime detection: PASS');
  console.log('[node-smoke] permission: PASS');
  console.log('[node-smoke] privacy: PASS');
  console.log('[node-smoke] session check: PASS');
  console.log('[node-smoke] clipboard: PASS');
  console.log('[node-smoke] haptics: PASS');
  console.log('[node-smoke] filesystem: PASS');
}

main().catch((error) => {
  console.error(error);
  process.exitCode = 1;
});
