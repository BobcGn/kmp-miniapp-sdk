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
  'getLocation',
  'scanCode',
  'chooseMedia',
  'requestSubscribeMessage',
  'getNetworkType',
  'onNetworkStatusChange',
  'offNetworkStatusChange',
  'uploadFile',
  'downloadFile',
  'requestPayment',
]);

// Permission state this fake host holds, plus a record of what it was asked to
// do. Nothing here may run before the test explicitly asks for it.
const scopeDecisions = new Map();
const authorizeCalls = [];
let openSettingCalls = 0;
let getSettingCalls = 0;

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

// Location is a device capability with its own permission scope, its own privacy
// precondition, and its own coordinate system. The fake returns a fixed position
// and records what it was asked for.
let locationAnswer = { latitude: 31.5, longitude: 121.5, accuracy: 15.0 };
let locationFailure = null;
const locationCalls = [];

// Scanning is a device capability driven entirely from a user gesture. The fake
// records what it was asked for and answers with the shape its fields describe;
// a field left undefined is one the host did not report.
let scanAnswer = {
  result: 'node-smoke-scan-payload',
  scanType: 'QR_CODE',
  charSet: 'utf-8',
  rawData: 'node-smoke-raw',
  path: '/node-sandbox/scanned.png',
};
let scanFailure = null;
const scanCalls = [];

// Media selection is another user-gesture capability. The fake records what it was
// asked for and returns the entries it is given, so a test can supply a malformed
// selection as easily as a valid one.
let mediaTempFiles = [
  { tempFilePath: '/node-sandbox/media/image.png', size: 2048, fileType: 'image' },
];
let mediaFailure = null;
const mediaCalls = [];

// A subscription request is a user-gesture capability whose answer is keyed by the
// template the host answered about. The fake records what it was asked about and
// returns the per-template statuses it is given.
let subscribeAnswer = {
  'node-template-one': 'accept',
  'node-template-two': 'reject',
};
let subscribeFailure = null;
const subscribeCalls = [];

// The network extensions are five host APIs gated one at a time. The fake records what
// it was asked, holds the listener it was given so a test can prove the SDK removes it,
// and hands back transfer tasks so abort and progress can be observed.
let networkTypeAnswer = 'wifi';
let networkTypeFailure = null;
const networkTypeCalls = [];
const networkListeners = [];
let networkListenerAdds = 0;
let networkListenerRemoves = 0;

let uploadAnswer = { statusCode: 200, data: 'upload-ok' };
let uploadFailure = null;
let uploadHold = false;
const uploadCalls = [];
const uploadRecords = [];
let pendingUpload = null;

let downloadAnswer = {
  tempFilePath: '/node-sandbox/downloaded.bin',
  statusCode: 200,
  filePath: null,
};
let downloadFailure = null;
let downloadHold = false;
const downloadCalls = [];
const downloadRecords = [];
let pendingDownload = null;

// Standard payment is one host API, and every answer it gives offline is a fake. The fake
// records the option bag it was handed — but never prints it, because a bag holds a
// signature — so a test can prove both what the SDK sent and that a caller with nothing
// configured never reaches it.
let paymentFailure = null;
let paymentHold = false;
let paymentPending = null;
const paymentCalls = [];

// One transfer task, so abort and progress registration can be counted.
function makeTransferTask(record) {
  return {
    progressListeners: [],
    abort() {
      record.aborts += 1;
    },
    onProgressUpdate(listener) {
      record.registrations += 1;
      this.progressListeners.push(listener);
    },
    offProgressUpdate(listener) {
      record.removals += 1;
      const index = this.progressListeners.indexOf(listener);
      if (index >= 0) {
        this.progressListeners.splice(index, 1);
      }
    },
    emit(progress) {
      this.progressListeners.slice().forEach(function (listener) {
        listener(progress);
      });
    },
  };
}

// The observation session runs on the JavaScript event loop, so a test that observes it
// must yield before the registration, the event, and the removal can be seen.
function settle() {
  return new Promise(function (resolve) {
    setTimeout(resolve, 0);
  });
}

function newTransferRecord() {
  return { aborts: 0, registrations: 0, removals: 0, task: null };
}

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
    getSettingCalls += 1;
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
  getLocation(options) {
    locationCalls.push({ type: options.type });
    if (locationFailure !== null) {
      options.fail({ errMsg: locationFailure });
      return;
    }
    options.success({
      latitude: locationAnswer.latitude,
      longitude: locationAnswer.longitude,
      accuracy: locationAnswer.accuracy,
      errMsg: 'getLocation:ok',
    });
  },
  scanCode(options) {
    scanCalls.push({ onlyFromCamera: options.onlyFromCamera, scanType: options.scanType });
    if (scanFailure !== null) {
      options.fail({ errMsg: scanFailure });
      return;
    }
    const answer = {
      result: scanAnswer.result,
      errMsg: 'scanCode:ok',
    };
    if (scanAnswer.scanType !== undefined) answer.scanType = scanAnswer.scanType;
    if (scanAnswer.charSet !== undefined) answer.charSet = scanAnswer.charSet;
    if (scanAnswer.rawData !== undefined) answer.rawData = scanAnswer.rawData;
    if (scanAnswer.path !== undefined) answer.path = scanAnswer.path;
    options.success(answer);
  },
  chooseMedia(options) {
    mediaCalls.push({
      count: options.count,
      mediaType: options.mediaType,
      sourceType: options.sourceType,
      maxDuration: options.maxDuration,
      sizeType: options.sizeType,
      camera: options.camera,
    });
    if (mediaFailure !== null) {
      options.fail({ errMsg: mediaFailure });
      return;
    }
    options.success({ errMsg: 'chooseMedia:ok', tempFiles: mediaTempFiles });
  },
  getNetworkType(options) {
    networkTypeCalls.push(1);
    if (networkTypeFailure !== null) {
      options.fail({ errMsg: networkTypeFailure });
      return;
    }
    options.success({ errMsg: 'getNetworkType:ok', networkType: networkTypeAnswer });
  },
  onNetworkStatusChange(listener) {
    networkListenerAdds += 1;
    networkListeners.push(listener);
  },
  offNetworkStatusChange(listener) {
    networkListenerRemoves += 1;
    const index = networkListeners.indexOf(listener);
    if (index >= 0) {
      networkListeners.splice(index, 1);
    }
  },
  uploadFile(options) {
    const record = newTransferRecord();
    record.task = makeTransferTask(record);
    uploadRecords.push(record);
    uploadCalls.push({
      url: options.url,
      filePath: options.filePath,
      name: options.name,
      header: options.header,
      formData: options.formData,
      timeout: options.timeout,
    });
    if (uploadFailure !== null) {
      options.fail({ errMsg: uploadFailure });
      return record.task;
    }
    if (uploadHold) {
      pendingUpload = { options: options, record: record };
    } else {
      options.success({
        errMsg: 'uploadFile:ok',
        statusCode: uploadAnswer.statusCode,
        data: uploadAnswer.data,
      });
    }
    return record.task;
  },
  downloadFile(options) {
    const record = newTransferRecord();
    record.task = makeTransferTask(record);
    downloadRecords.push(record);
    downloadCalls.push({
      url: options.url,
      header: options.header,
      timeout: options.timeout,
      filePath: options.filePath,
    });
    if (downloadFailure !== null) {
      options.fail({ errMsg: downloadFailure });
      return record.task;
    }
    if (downloadHold) {
      pendingDownload = { options: options, record: record };
    } else {
      options.success({
        errMsg: 'downloadFile:ok',
        statusCode: downloadAnswer.statusCode,
        tempFilePath: downloadAnswer.tempFilePath,
        filePath: downloadAnswer.filePath,
      });
    }
    return record.task;
  },
  requestPayment(options) {
    paymentCalls.push({
      // The whole bag, so a test can prove the SDK invented no field. The callbacks the
      // adapter attaches are part of the bag and are filtered where they are asserted.
      keys: Object.keys(options),
      timeStamp: options.timeStamp,
      nonceStr: options.nonceStr,
      package: options.package,
      signType: options.signType,
      paySign: options.paySign,
    });
    if (paymentFailure !== null) {
      options.fail({ errMsg: paymentFailure });
      return;
    }
    if (paymentHold) {
      paymentPending = options;
      return;
    }
    // The host's success callback carries nothing about an order, and the SDK reads
    // nothing from it: the callback itself is the whole signal.
    options.success({ errMsg: 'requestPayment:ok' });
  },
  requestSubscribeMessage(options) {
    subscribeCalls.push({ tmplIds: options.tmplIds });
    if (subscribeFailure !== null) {
      options.fail({ errMsg: subscribeFailure });
      return;
    }
    const answer = { errMsg: 'requestSubscribeMessage:ok' };
    for (const [templateId, status] of Object.entries(subscribeAnswer)) {
      answer[templateId] = status;
    }
    options.success(answer);
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
    'wechatGetCurrentLocation',
    'wechatScanCode',
    'wechatChooseMedia',
    'wechatRequestSubscribeMessage',
    'networkStatus',
    'startNetworkStatusObservation',
    'stopNetworkStatusObservation',
    'networkStatusObservationFailure',
    'wechatUploadFile',
    'wechatDownloadFile',
    'wechatRequestPayment',
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
  // A schema this fake base library does not confirm still answers false.
  assert.equal(miniAppSdk.wechatCanIUse('chooseLocation'), false);

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

  // Loading the module must not have asked for a position.
  assert.deepEqual(locationCalls, []);

  // Location availability, its permission, and the privacy contract are three
  // separate answers, and the gate reports only the first.
  assert.equal(miniAppSdk.capabilitySupport('wechat.location').state, 'Supported');
  assert.equal(await miniAppSdk.permissionState('location'), 'NotRequested');

  // The privacy precondition is enforced before the host is asked.
  privacyRequired = true;
  await assert.rejects(miniAppSdk.wechatGetCurrentLocation('gcj02'));
  assert.equal(locationCalls.length, 0);

  privacyRequired = false;
  assert.equal(await miniAppSdk.requestPermission('location'), 'Granted');
  assert.equal(await miniAppSdk.permissionState('location'), 'Granted');

  const position = await miniAppSdk.wechatGetCurrentLocation('gcj02');
  assert.ok(Number.isFinite(position.latitude));
  assert.ok(Number.isFinite(position.longitude));
  assert.ok(position.accuracyMeters >= 0);
  assert.equal(position.coordinateSystem, 'gcj02');

  // The SDK never lets the host pick a coordinate system on its own.
  assert.equal(locationCalls[locationCalls.length - 1].type, 'gcj02');

  const wgs = await miniAppSdk.wechatGetCurrentLocation('wgs84');
  assert.equal(wgs.coordinateSystem, 'wgs84');
  assert.equal(locationCalls[locationCalls.length - 1].type, 'wgs84');

  // An unknown coordinate system is a caller mistake, not a host condition.
  await assert.rejects(miniAppSdk.wechatGetCurrentLocation('mars'), /Unsupported coordinate system/);

  // A host answer that is not a usable coordinate is reported rather than
  // defaulted to zero.
  locationAnswer = { latitude: Number.NaN, longitude: 121.5, accuracy: 15.0 };
  await assert.rejects(miniAppSdk.wechatGetCurrentLocation('gcj02'));

  locationAnswer = { latitude: 31.5, longitude: 200.0, accuracy: 15.0 };
  await assert.rejects(miniAppSdk.wechatGetCurrentLocation('gcj02'));

  // An ordinary host failure stays a failure.
  locationAnswer = { latitude: 31.5, longitude: 121.5, accuracy: 15.0 };
  locationFailure = 'getLocation:fail system error';
  await assert.rejects(miniAppSdk.wechatGetCurrentLocation('gcj02'));
  locationFailure = null;

  // Scanning opens WeChat's own interface, and only from a user gesture. Nothing
  // opened it while the module loaded, and the SDK asks for no permission to do
  // so, because no permission precondition for this API could be established.
  assert.deepEqual(scanCalls, []);
  assert.equal(miniAppSdk.capabilitySupport('wechat.scan-code').state, 'Supported');

  const permissionQueriesBeforeScan = getSettingCalls;
  const authorizationsBeforeScan = authorizeCalls.length;
  const settingsVisitsBeforeScan = openSettingCalls;
  const privacyAuthorizationsBeforeScan = privacyAuthorizations.length;

  // An omitted request asks for every category the host supports. Leaving the
  // category field unset is how that is expressed, not an empty array.
  const scanned = await miniAppSdk.wechatScanCode();
  assert.equal(scanned.text, 'node-smoke-scan-payload');
  assert.equal(scanned.scanType, 'QR_CODE');
  assert.equal(scanned.format, 'QR_CODE');
  assert.equal(scanned.charSet, 'utf-8');
  assert.equal(scanned.rawData, 'node-smoke-raw');
  assert.equal(scanned.path, '/node-sandbox/scanned.png');
  assert.equal(scanCalls[scanCalls.length - 1].onlyFromCamera, false);
  assert.equal(scanCalls[scanCalls.length - 1].scanType, undefined);

  // No permission was queried, requested, or opened, and no privacy consent was
  // asked for, on the way to opening the interface.
  assert.equal(getSettingCalls, permissionQueriesBeforeScan);
  assert.equal(authorizeCalls.length, authorizationsBeforeScan);
  assert.equal(openSettingCalls, settingsVisitsBeforeScan);
  assert.equal(privacyAuthorizations.length, privacyAuthorizationsBeforeScan);

  // The camera choice and the categories are forwarded in the caller's order.
  await miniAppSdk.wechatScanCode(true, ['qrCode', 'barCode']);
  assert.equal(scanCalls[scanCalls.length - 1].onlyFromCamera, true);
  assert.deepEqual(scanCalls[scanCalls.length - 1].scanType, ['qrCode', 'barCode']);

  // A format this SDK does not know is reported rather than turned into a failure:
  // the host decoded something real and named it in its own vocabulary. The fields
  // it did not report stay absent instead of becoming empty strings.
  scanAnswer = { result: 'payload', scanType: 'BRAND_NEW_FORMAT' };
  const unknownFormat = await miniAppSdk.wechatScanCode();
  assert.equal(unknownFormat.text, 'payload');
  assert.equal(unknownFormat.scanType, 'BRAND_NEW_FORMAT');
  assert.equal(unknownFormat.format, null);
  assert.equal(unknownFormat.charSet, null);
  assert.equal(unknownFormat.rawData, null);
  assert.equal(unknownFormat.path, null);

  scanAnswer = {
    result: 'node-smoke-scan-payload',
    scanType: 'QR_CODE',
    charSet: 'utf-8',
    rawData: 'node-smoke-raw',
    path: '/node-sandbox/scanned.png',
  };

  // A category this SDK does not know is a caller mistake, not a host condition.
  await assert.rejects(
    miniAppSdk.wechatScanCode(false, ['notACategory']),
    /Unsupported scan category/,
  );

  // Dismissing the interface is the user's decision, and the SDK reports it as its
  // own error type so a consumer never has to read a host message. Both forms the
  // adapter recognizes are covered; nothing here matches on a substring.
  scanFailure = 'scanCode:cancel';
  await assert.rejects(
    miniAppSdk.wechatScanCode(),
    (error) => error.name === 'HostInteractionInterrupted',
  );
  scanFailure = 'scanCode:fail cancel';
  await assert.rejects(
    miniAppSdk.wechatScanCode(),
    (error) => error.name === 'HostInteractionInterrupted',
  );

  // A failure that merely mentions cancelling is not a user decision.
  scanFailure = 'scanCode:fail user cancel';
  await assert.rejects(miniAppSdk.wechatScanCode(), (error) => error.name === 'HostFailure');
  scanFailure = 'scanCode:fail system error';
  await assert.rejects(miniAppSdk.wechatScanCode(), (error) => error.name === 'HostFailure');
  scanFailure = null;

  // Content the contract cannot carry is reported rather than becoming an empty
  // scan the user never made.
  scanAnswer = { result: undefined, scanType: 'QR_CODE' };
  await assert.rejects(miniAppSdk.wechatScanCode(), (error) => error.name === 'InvalidResponse');
  scanAnswer = { result: 'node-smoke-scan-payload', scanType: 'QR_CODE' };

  // A host without the API fails through the capability path instead of throwing
  // out of the JavaScript boundary. The API is removed from the fake host itself,
  // which is the condition the adapter probes.
  supportedSchemas.delete('scanCode');
  const scanCodeImpl = global.wx.scanCode;
  delete global.wx.scanCode;

  assert.equal(miniAppSdk.wechatCanIUse('scanCode'), false);
  assert.equal(miniAppSdk.capabilitySupport('wechat.scan-code').state, 'Unsupported');
  await assert.rejects(
    miniAppSdk.wechatScanCode(),
    (error) => error.name === 'UnsupportedCapability',
  );

  global.wx.scanCode = scanCodeImpl;
  supportedSchemas.add('scanCode');

  // Media selection opens WeChat's own picker, and only from a user gesture.
  // Nothing opened it while the module loaded, and the SDK asks for no permission
  // to do so, because the host ties none to this API.
  assert.deepEqual(mediaCalls, []);
  assert.equal(miniAppSdk.capabilitySupport('wechat.choose-media').state, 'Supported');

  const permissionQueriesBeforeMedia = getSettingCalls;
  const authorizationsBeforeMedia = authorizeCalls.length;
  const settingsVisitsBeforeMedia = openSettingCalls;
  const privacyAuthorizationsBeforeMedia = privacyAuthorizations.length;

  // Only what the caller asked for is sent, so the host's own defaults apply to
  // everything else.
  const chosen = await miniAppSdk.wechatChooseMedia({ mediaTypes: ['image'] });
  assert.equal(chosen.length, 1);
  assert.equal(chosen[0].tempFilePath, '/node-sandbox/media/image.png');
  assert.equal(chosen[0].sizeBytes, 2048);
  assert.equal(chosen[0].fileType, 'image');
  assert.equal(chosen[0].hostFileType, 'image');
  // WeChat reports no duration, dimensions, or thumbnail for an image, so those
  // stay absent instead of becoming zeros.
  assert.equal(chosen[0].durationSeconds, null);
  assert.equal(chosen[0].width, null);
  assert.equal(chosen[0].height, null);
  assert.equal(chosen[0].thumbTempFilePath, null);

  const lastMediaCall = mediaCalls[mediaCalls.length - 1];
  assert.equal(lastMediaCall.count, 1);
  assert.deepEqual(lastMediaCall.mediaType, ['image']);
  assert.equal(lastMediaCall.sourceType, undefined);
  assert.equal(lastMediaCall.maxDuration, undefined);
  assert.equal(lastMediaCall.sizeType, undefined);
  assert.equal(lastMediaCall.camera, undefined);

  // No permission was queried, requested, or opened, and no privacy consent was
  // asked for, on the way to opening the picker.
  assert.equal(getSettingCalls, permissionQueriesBeforeMedia);
  assert.equal(authorizeCalls.length, authorizationsBeforeMedia);
  assert.equal(openSettingCalls, settingsVisitsBeforeMedia);
  assert.equal(privacyAuthorizations.length, privacyAuthorizationsBeforeMedia);

  // Every option the caller can set is forwarded.
  await miniAppSdk.wechatChooseMedia({
    mediaTypes: ['mix'],
    count: 3,
    sourceTypes: ['album', 'camera'],
    maxDurationSeconds: 30,
    sizeTypes: ['compressed'],
    camera: 'front',
  });
  const fullCall = mediaCalls[mediaCalls.length - 1];
  assert.equal(fullCall.count, 3);
  assert.deepEqual(fullCall.mediaType, ['mix']);
  assert.deepEqual(fullCall.sourceType, ['album', 'camera']);
  assert.equal(fullCall.maxDuration, 30);
  assert.deepEqual(fullCall.sizeType, ['compressed']);
  assert.equal(fullCall.camera, 'front');

  // A video reports the metadata an image has none of.
  mediaTempFiles = [
    {
      tempFilePath: '/node-sandbox/media/video.mp4',
      size: 1048576,
      fileType: 'video',
      duration: 12.5,
      width: 1920,
      height: 1080,
      thumbTempFilePath: '/node-sandbox/media/video-thumb.jpg',
    },
  ];
  const video = await miniAppSdk.wechatChooseMedia({ mediaTypes: ['video'] });
  assert.equal(video[0].fileType, 'video');
  assert.equal(video[0].durationSeconds, 12.5);
  assert.equal(video[0].width, 1920);
  assert.equal(video[0].height, 1080);
  assert.equal(video[0].thumbTempFilePath, '/node-sandbox/media/video-thumb.jpg');

  // A kind this SDK does not know is reported rather than turned into a failure,
  // and the host's own name is kept.
  mediaTempFiles = [
    { tempFilePath: '/node-sandbox/media/live.heic', size: 512, fileType: 'livePhoto' },
  ];
  const unknownKind = await miniAppSdk.wechatChooseMedia({ mediaTypes: ['image'] });
  assert.equal(unknownKind[0].fileType, null);
  assert.equal(unknownKind[0].hostFileType, 'livePhoto');

  // A successful picker response must identify at least one selected file.
  mediaTempFiles = [];
  await assert.rejects(
    miniAppSdk.wechatChooseMedia({ mediaTypes: ['image'] }),
    (error) => error.name === 'InvalidResponse',
  );

  mediaTempFiles = [
    { tempFilePath: '/node-sandbox/media/image.png', size: 2048, fileType: 'image' },
  ];

  // Caller mistakes are refused rather than quietly corrected.
  await assert.rejects(
    miniAppSdk.wechatChooseMedia({ mediaTypes: ['hologram'] }),
    /Unsupported media type/,
  );
  await assert.rejects(miniAppSdk.wechatChooseMedia({ mediaTypes: [] }), /at least one media type/);
  await assert.rejects(
    miniAppSdk.wechatChooseMedia({ mediaTypes: ['image'], count: 0 }),
    /at least 1/,
  );
  await assert.rejects(
    miniAppSdk.wechatChooseMedia({ mediaTypes: ['video'], maxDurationSeconds: 2 }),
    /between 3 and 60/,
  );
  await assert.rejects(
    miniAppSdk.wechatChooseMedia({ mediaTypes: ['video'], maxDurationSeconds: 61 }),
    /between 3 and 60/,
  );
  await assert.rejects(
    miniAppSdk.wechatChooseMedia({ mediaTypes: ['image'], sourceTypes: ['disk'] }),
    /Unsupported media source/,
  );

  // The interaction ending without a selection is reported as an interruption whose
  // cause the host did not identify. The two exact signals observed in Developer
  // Tools and on Android are recognized; nothing matches on a substring.
  mediaFailure = 'chooseMedia:cancel';
  await assert.rejects(
    miniAppSdk.wechatChooseMedia({ mediaTypes: ['image'] }),
    (error) => error.name === 'HostInteractionInterrupted',
  );
  mediaFailure = 'chooseMedia:fail cancel';
  await assert.rejects(
    miniAppSdk.wechatChooseMedia({ mediaTypes: ['image'] }),
    (error) => error.name === 'HostInteractionInterrupted',
  );

  // A failure that merely mentions cancelling is not an interruption.
  mediaFailure = 'chooseMedia:fail user cancel';
  await assert.rejects(
    miniAppSdk.wechatChooseMedia({ mediaTypes: ['image'] }),
    (error) => error.name === 'HostFailure',
  );
  mediaFailure = 'chooseMedia:fail system error';
  await assert.rejects(
    miniAppSdk.wechatChooseMedia({ mediaTypes: ['image'] }),
    (error) => error.name === 'HostFailure',
  );
  mediaFailure = null;

  // Answers the contract cannot carry are reported rather than becoming fabricated
  // metadata or a missing file the caller never chose.
  mediaTempFiles = [{ tempFilePath: undefined, size: 2048, fileType: 'image' }];
  await assert.rejects(
    miniAppSdk.wechatChooseMedia({ mediaTypes: ['image'] }),
    (error) => error.name === 'InvalidResponse',
  );
  mediaTempFiles = [{ tempFilePath: '/node-sandbox/media/image.png', size: -1, fileType: 'image' }];
  await assert.rejects(
    miniAppSdk.wechatChooseMedia({ mediaTypes: ['image'] }),
    (error) => error.name === 'InvalidResponse',
  );
  mediaTempFiles = [{ tempFilePath: '/node-sandbox/media/image.png', size: 2048 }];
  await assert.rejects(
    miniAppSdk.wechatChooseMedia({ mediaTypes: ['image'] }),
    (error) => error.name === 'InvalidResponse',
  );
  mediaTempFiles = [
    { tempFilePath: '/node-sandbox/media/image.png', size: 2048, fileType: 'image' },
  ];

  // A host without the API fails through the capability path instead of throwing
  // out of the JavaScript boundary.
  supportedSchemas.delete('chooseMedia');
  const chooseMediaImpl = global.wx.chooseMedia;
  delete global.wx.chooseMedia;

  assert.equal(miniAppSdk.wechatCanIUse('chooseMedia'), false);
  assert.equal(miniAppSdk.capabilitySupport('wechat.choose-media').state, 'Unsupported');
  await assert.rejects(
    miniAppSdk.wechatChooseMedia({ mediaTypes: ['image'] }),
    (error) => error.name === 'UnsupportedCapability',
  );

  global.wx.chooseMedia = chooseMediaImpl;
  supportedSchemas.add('chooseMedia');

  // A subscription request is a user-gesture capability whose answer is keyed by the
  // template the host answered about. Nothing asked the host while the module loaded,
  // and the SDK asks for no permission to do so.
  assert.deepEqual(subscribeCalls, []);
  assert.equal(
    miniAppSdk.capabilitySupport('wechat.request-subscribe-message').state,
    'Supported',
  );

  const permissionQueriesBeforeSubscribe = getSettingCalls;
  const authorizationsBeforeSubscribe = authorizeCalls.length;
  const settingsVisitsBeforeSubscribe = openSettingCalls;
  const privacyAuthorizationsBeforeSubscribe = privacyAuthorizations.length;

  // The template ids reach the host in the caller's order.
  const subscribed = await miniAppSdk.wechatRequestSubscribeMessage([
    'node-template-one',
    'node-template-two',
  ]);
  assert.deepEqual(subscribeCalls[subscribeCalls.length - 1].tmplIds, [
    'node-template-one',
    'node-template-two',
  ]);

  // One entry per requested template, in caller order.
  assert.equal(subscribed.length, 2);
  assert.equal(subscribed[0].templateId, 'node-template-one');
  assert.equal(subscribed[0].status, 'accept');
  assert.equal(subscribed[0].hostStatus, 'accept');
  assert.equal(subscribed[1].templateId, 'node-template-two');
  assert.equal(subscribed[1].status, null);
  assert.equal(subscribed[1].hostStatus, 'reject');
  // The host's own status line is never read as a template.
  assert.equal(subscribed.some((entry) => entry.templateId === 'errMsg'), false);


  // No permission was queried, requested, or opened, and no privacy consent was asked
  // for, on the way to the request.
  assert.equal(getSettingCalls, permissionQueriesBeforeSubscribe);
  assert.equal(authorizeCalls.length, authorizationsBeforeSubscribe);
  assert.equal(openSettingCalls, settingsVisitsBeforeSubscribe);
  assert.equal(privacyAuthorizations.length, privacyAuthorizationsBeforeSubscribe);

  // A status this SDK cannot name is preserved verbatim with no resolved status, so a
  // caller can act on what the host actually said instead of a guess.
  subscribeAnswer = { 'node-template-one': 'reject' };
  const unnamed = await miniAppSdk.wechatRequestSubscribeMessage(['node-template-one']);
  assert.equal(unnamed[0].status, null);
  assert.equal(unnamed[0].hostStatus, 'reject');

  // An unexpected template breaks request/response correlation.
  subscribeAnswer = { 'node-template-one': 'accept', 'node-template-other': 'ban' };
  await assert.rejects(
    miniAppSdk.wechatRequestSubscribeMessage(['node-template-one']),
    (error) => error.name === 'InvalidResponse',
  );

  // A success callback must correlate exactly with the templates in the request.
  subscribeAnswer = {};
  await assert.rejects(
    miniAppSdk.wechatRequestSubscribeMessage(['node-template-one']),
    (error) => error.name === 'InvalidResponse',
  );

  subscribeAnswer = {
    'node-template-one': 'accept',
    'node-template-unexpected': 'reject',
  };
  await assert.rejects(
    miniAppSdk.wechatRequestSubscribeMessage(['node-template-one']),
    (error) => error.name === 'InvalidResponse',
  );

  subscribeAnswer = {
    'node-template-two': 'reject',
    'node-template-one': 'accept',
  };

  // Duplicate ids are collapsed with the caller's order kept.
  await miniAppSdk.wechatRequestSubscribeMessage(['node-template-two', 'node-template-one', 'node-template-two']);
  assert.deepEqual(subscribeCalls[subscribeCalls.length - 1].tmplIds, [
    'node-template-two',
    'node-template-one',
  ]);

  // Caller mistakes are refused rather than quietly corrected.
  await assert.rejects(
    miniAppSdk.wechatRequestSubscribeMessage([]),
    /at least one template id/,
  );
  await assert.rejects(
    miniAppSdk.wechatRequestSubscribeMessage(['node-template-one', '   ']),
    /non-blank/,
  );

  // A status the host reports as something other than text is a broken answer.
  subscribeAnswer = { 'node-template-one': 7 };
  await assert.rejects(
    miniAppSdk.wechatRequestSubscribeMessage(['node-template-one']),
    (error) => error.name === 'InvalidResponse',
  );
  subscribeAnswer = { 'node-template-one': 'accept' };

  // These are only conventional candidates. They remain host failures until a real
  // subscription request establishes an exact cancellation signal.
  subscribeFailure = 'requestSubscribeMessage:cancel';
  await assert.rejects(
    miniAppSdk.wechatRequestSubscribeMessage(['node-template-one']),
    (error) => error.name === 'HostFailure',
  );
  subscribeFailure = 'requestSubscribeMessage:fail cancel';
  await assert.rejects(
    miniAppSdk.wechatRequestSubscribeMessage(['node-template-one']),
    (error) => error.name === 'HostFailure',
  );

  // A failure that merely mentions cancelling is not an interruption.
  subscribeFailure = 'requestSubscribeMessage:fail user cancel';
  await assert.rejects(
    miniAppSdk.wechatRequestSubscribeMessage(['node-template-one']),
    (error) => error.name === 'HostFailure',
  );
  subscribeFailure = 'requestSubscribeMessage:fail template not found';
  await assert.rejects(
    miniAppSdk.wechatRequestSubscribeMessage(['node-template-one']),
    (error) => error.name === 'HostFailure',
  );
  subscribeFailure = null;

  // A host without the API fails through the capability path instead of throwing out of
  // the JavaScript boundary.
  supportedSchemas.delete('requestSubscribeMessage');
  const requestSubscribeMessageImpl = global.wx.requestSubscribeMessage;
  delete global.wx.requestSubscribeMessage;

  assert.equal(miniAppSdk.wechatCanIUse('requestSubscribeMessage'), false);
  assert.equal(
    miniAppSdk.capabilitySupport('wechat.request-subscribe-message').state,
    'Unsupported',
  );
  await assert.rejects(
    miniAppSdk.wechatRequestSubscribeMessage(['node-template-one']),
    (error) => error.name === 'UnsupportedCapability',
  );

  global.wx.requestSubscribeMessage = requestSubscribeMessageImpl;
  supportedSchemas.add('requestSubscribeMessage');

  // The network extensions are five host APIs, gated one at a time. Nothing asked the
  // host while the module loaded: no query, no listener, no transfer.
  assert.deepEqual(networkTypeCalls, []);
  assert.deepEqual(uploadCalls, []);
  assert.deepEqual(downloadCalls, []);
  assert.equal(networkListenerAdds, 0);
  assert.equal(miniAppSdk.capabilitySupport('network-status-query').state, 'Supported');
  assert.equal(miniAppSdk.capabilitySupport('network-status-listener').state, 'Supported');
  assert.equal(miniAppSdk.capabilitySupport('wechat.upload-file').state, 'Supported');
  assert.equal(miniAppSdk.capabilitySupport('wechat.download-file').state, 'Supported');

  // A query answers with the host's own word beside the name this SDK has for it.
  const networkState = await miniAppSdk.networkStatus();
  assert.equal(networkState.isConnected, true);
  assert.equal(networkState.networkType, 'WIFI');
  assert.equal(networkState.hostNetworkType, 'wifi');
  assert.equal(networkTypeCalls.length, 1);
  // Asking is not observing: the query must not leave a listener behind.
  assert.equal(networkListenerAdds, 0);

  // A kind this SDK does not know is preserved rather than reported as unknown.
  networkTypeAnswer = '6g';
  const unknownNetworkKind = await miniAppSdk.networkStatus();
  assert.equal(unknownNetworkKind.networkType, null);
  assert.equal(unknownNetworkKind.hostNetworkType, '6g');
  assert.equal(unknownNetworkKind.isConnected, true);

  // No connection is the host's own word for it, and the SDK reads connectivity from it.
  networkTypeAnswer = 'none';
  const none = await miniAppSdk.networkStatus();
  assert.equal(none.isConnected, false);
  assert.equal(none.networkType, 'NONE');

  // Answers the contract cannot carry are reported rather than filled in.
  networkTypeAnswer = '   ';
  await assert.rejects(miniAppSdk.networkStatus(), (error) => error.name === 'InvalidResponse');
  networkTypeAnswer = 7;
  await assert.rejects(miniAppSdk.networkStatus(), (error) => error.name === 'InvalidResponse');
  networkTypeAnswer = 'wifi';

  networkTypeFailure = 'getNetworkType:fail system error';
  await assert.rejects(miniAppSdk.networkStatus(), (error) => error.name === 'HostFailure');
  networkTypeFailure = null;

  // Observation registers one host listener and removes it when the session stops.
  const queriesBeforeObservation = networkTypeCalls.length;
  assert.equal(miniAppSdk.startNetworkStatusObservation(), undefined);
  await settle();
  assert.equal(networkListenerAdds, 1);
  assert.equal(networkListeners.length, 1);
  networkListeners.slice().forEach(function (listener) {
    listener({ isConnected: true, networkType: '4g' });
  });
  await settle();
  const observed = await miniAppSdk.stopNetworkStatusObservation();
  assert.equal(networkListenerRemoves, 1);
  assert.equal(networkListeners.length, 0);
  assert.equal(observed.length, 1);
  assert.equal(observed[0].networkType, 'CELLULAR_4G');
  assert.equal(observed[0].hostNetworkType, '4g');
  assert.equal(miniAppSdk.networkStatusObservationFailure(), null);

  // Observing is not polling: the host was asked by the queries above and never by the
  // session itself.
  assert.equal(networkTypeCalls.length, queriesBeforeObservation);

  // A host that can register a listener but not remove one is reported unsupported, so
  // the session records why instead of leaking the listener it could not remove.
  const offNetworkStatusChangeImpl = global.wx.offNetworkStatusChange;
  delete global.wx.offNetworkStatusChange;
  supportedSchemas.delete('offNetworkStatusChange');
  assert.equal(miniAppSdk.capabilitySupport('network-status-listener').state, 'Unsupported');
  miniAppSdk.startNetworkStatusObservation();
  await settle();
  const blocked = await miniAppSdk.stopNetworkStatusObservation();
  assert.deepEqual(blocked, []);
  assert.equal(miniAppSdk.networkStatusObservationFailure(), 'UnsupportedCapability');
  assert.equal(networkListenerAdds, 1, 'nothing may be registered that cannot be removed');
  global.wx.offNetworkStatusChange = offNetworkStatusChangeImpl;
  supportedSchemas.add('offNetworkStatusChange');

  // An upload carries what the caller described and reports the host's answer.
  const uploaded = await miniAppSdk
    .wechatUploadFile({
      url: 'https://example.com/upload',
      filePath: '/node-sandbox/upload.bin',
      name: 'file',
      headers: { 'X-Test': 'yes' },
      formData: { field: 'value' },
      timeoutMillis: 5000,
    })
    .result();
  assert.equal(uploaded.statusCode, 200);
  assert.equal(uploaded.responseText, 'upload-ok');
  const uploadCall = uploadCalls[uploadCalls.length - 1];
  assert.equal(uploadCall.url, 'https://example.com/upload');
  assert.equal(uploadCall.filePath, '/node-sandbox/upload.bin');
  assert.equal(uploadCall.name, 'file');
  assert.deepEqual(uploadCall.header, { 'X-Test': 'yes' });
  assert.deepEqual(uploadCall.formData, { field: 'value' });
  assert.equal(uploadCall.timeout, 5000);
  // An omitted option is not sent, so the host's own default applies.
  await miniAppSdk
    .wechatUploadFile({ url: 'https://example.com/upload', filePath: '/node-sandbox/upload.bin' })
    .result();
  assert.equal(uploadCalls[uploadCalls.length - 1].header, undefined);
  assert.equal(uploadCalls[uploadCalls.length - 1].formData, undefined);
  assert.equal(uploadCalls[uploadCalls.length - 1].timeout, undefined);

  // A completed upload reports its HTTP status, including an error status.
  uploadAnswer = { statusCode: 500, data: 'server error' };
  const failedStatus = await miniAppSdk
    .wechatUploadFile({ url: 'https://example.com/upload', filePath: '/node-sandbox/upload.bin' })
    .result();
  assert.equal(failedStatus.statusCode, 500);
  uploadAnswer = { statusCode: 200, data: 'upload-ok' };

  // Answers the contract cannot carry are reported.
  uploadAnswer = { statusCode: 200, data: 7 };
  await assert.rejects(
    miniAppSdk
      .wechatUploadFile({ url: 'https://example.com/upload', filePath: '/node-sandbox/upload.bin' })
      .result(),
    (error) => error.name === 'InvalidResponse',
  );
  uploadAnswer = { statusCode: '200', data: 'ok' };
  await assert.rejects(
    miniAppSdk
      .wechatUploadFile({ url: 'https://example.com/upload', filePath: '/node-sandbox/upload.bin' })
      .result(),
    (error) => error.name === 'InvalidResponse',
  );
  uploadAnswer = { statusCode: 200, data: 'upload-ok' };

  // Caller mistakes are refused before the host is called.
  await assert.rejects(
    Promise.resolve().then(function () {
      return miniAppSdk.wechatUploadFile({ url: '  ', filePath: '/node-sandbox/upload.bin' });
    }),
    /requires a url/,
  );

  // The exact host timeout is a timeout; a message that merely mentions it is not.
  uploadFailure = 'uploadFile:fail timeout';
  await assert.rejects(
    miniAppSdk
      .wechatUploadFile({ url: 'https://example.com/upload', filePath: '/node-sandbox/upload.bin' })
      .result(),
    (error) => error.name === 'Timeout',
  );
  uploadFailure = 'uploadFile:fail connection timeout';
  await assert.rejects(
    miniAppSdk
      .wechatUploadFile({ url: 'https://example.com/upload', filePath: '/node-sandbox/upload.bin' })
      .result(),
    (error) => error.name === 'HostFailure',
  );
  uploadFailure = null;

  // An in-flight upload registers one progress listener, reports the host's figure, and
  // removes the listener when it finishes.
  uploadHold = true;
  const slowUpload = miniAppSdk.wechatUploadFile({
    url: 'https://example.com/slow',
    filePath: '/node-sandbox/upload.bin',
  });
  assert.equal(slowUpload.abortable, true);
  const uploadRecord = uploadRecords[uploadRecords.length - 1];
  assert.equal(uploadRecord.registrations, 1);
  assert.equal(slowUpload.progress(), null, 'no progress has been reported yet');
  uploadRecord.task.emit({ progress: 40, totalBytesSent: 400, totalBytesExpectedToSend: 1000 });
  const uploadProgress = slowUpload.progress();
  assert.equal(uploadProgress.percent, 40);
  assert.equal(uploadProgress.bytesTransferred, 400);
  assert.equal(uploadProgress.bytesExpected, 1000);
  pendingUpload.options.success({ errMsg: 'uploadFile:ok', statusCode: 200, data: 'done' });
  assert.equal((await slowUpload.result()).responseText, 'done');
  assert.equal(uploadRecord.removals, 1);
  assert.equal(uploadRecord.task.progressListeners.length, 0);
  uploadHold = false;

  // Aborting stops the host task once and ends the transfer without an answer.
  uploadHold = true;
  const abortedUpload = miniAppSdk.wechatUploadFile({
    url: 'https://example.com/slow',
    filePath: '/node-sandbox/upload.bin',
  });
  const abortedRecord = uploadRecords[uploadRecords.length - 1];
  assert.equal(abortedUpload.abort(), true);
  assert.equal(abortedUpload.abort(), false);
  assert.equal(abortedRecord.aborts, 1);
  assert.equal(abortedRecord.removals, 1, 'progress must be cleaned up on abort');
  // A late answer for an aborted transfer is ignored.
  pendingUpload.options.success({ errMsg: 'uploadFile:ok', statusCode: 200, data: 'late' });
  await assert.rejects(abortedUpload.result());
  uploadHold = false;

  // A host that returns no task cannot be aborted, and the SDK says so rather than
  // pretending the host operation was stopped.
  const uploadFileImpl = global.wx.uploadFile;
  global.wx.uploadFile = function uploadWithoutTask(options) {
    uploadCalls.push({ url: options.url, filePath: options.filePath, name: options.name });
    options.success({ errMsg: 'uploadFile:ok', statusCode: 200, data: 'no task' });
  };
  const tasklessUpload = miniAppSdk.wechatUploadFile({
    url: 'https://example.com/upload',
    filePath: '/node-sandbox/upload.bin',
  });
  assert.equal(tasklessUpload.abortable, false);
  assert.equal(tasklessUpload.abort(), false);
  assert.equal((await tasklessUpload.result()).responseText, 'no task');
  global.wx.uploadFile = uploadFileImpl;

  // A download reports the host's temporary path and never reads the file.
  const downloaded = await miniAppSdk
    .wechatDownloadFile({ url: 'https://example.com/file.bin' })
    .result();
  assert.equal(downloaded.statusCode, 200);
  assert.equal(downloaded.tempFilePath, '/node-sandbox/downloaded.bin');
  assert.equal(downloaded.filePath, null);
  assert.equal(downloadCalls[downloadCalls.length - 1].filePath, undefined);

  // A requested target path is forwarded, and the path the host reports comes back.
  downloadAnswer = {
    tempFilePath: '/node-sandbox/downloaded.bin',
    statusCode: 200,
    filePath: '/node-sandbox/target.bin',
  };
  const targeted = await miniAppSdk
    .wechatDownloadFile({
      url: 'https://example.com/file.bin',
      filePath: '/node-sandbox/target.bin',
      headers: { 'X-Test': 'yes' },
    })
    .result();
  assert.equal(targeted.filePath, '/node-sandbox/target.bin');
  assert.deepEqual(downloadCalls[downloadCalls.length - 1].header, { 'X-Test': 'yes' });
  assert.equal(downloadCalls[downloadCalls.length - 1].filePath, '/node-sandbox/target.bin');

  // A download without a file to point at is a broken answer, not a successful one.
  downloadAnswer = { tempFilePath: null, statusCode: 200, filePath: null };
  await assert.rejects(
    miniAppSdk.wechatDownloadFile({ url: 'https://example.com/file.bin' }).result(),
    (error) => error.name === 'InvalidResponse',
  );
  downloadAnswer = {
    tempFilePath: '/node-sandbox/downloaded.bin',
    statusCode: 200,
    filePath: null,
  };

  downloadFailure = 'downloadFile:fail timeout';
  await assert.rejects(
    miniAppSdk.wechatDownloadFile({ url: 'https://example.com/file.bin' }).result(),
    (error) => error.name === 'Timeout',
  );
  downloadFailure = null;

  // The download task cleans up its progress listener on abort, exactly as the upload does.
  downloadHold = true;
  const abortedDownload = miniAppSdk.wechatDownloadFile({ url: 'https://example.com/slow' });
  const downloadRecord = downloadRecords[downloadRecords.length - 1];
  assert.equal(downloadRecord.registrations, 1);
  assert.equal(abortedDownload.abort(), true);
  assert.equal(downloadRecord.aborts, 1);
  assert.equal(downloadRecord.removals, 1);
  downloadHold = false;

  // A host without each API fails through the capability path instead of throwing out of
  // the JavaScript boundary, one API at a time.
  const removable = [
    ['getNetworkType', 'network-status-query'],
    ['uploadFile', 'wechat.upload-file'],
    ['downloadFile', 'wechat.download-file'],
  ];
  for (const [api, capability] of removable) {
    const implementation = global.wx[api];
    delete global.wx[api];
    supportedSchemas.delete(api);
    assert.equal(miniAppSdk.capabilitySupport(capability).state, 'Unsupported', api);
    global.wx[api] = implementation;
    supportedSchemas.add(api);
  }
  const missingApiDownload = global.wx.downloadFile;
  delete global.wx.downloadFile;
  supportedSchemas.delete('downloadFile');
  await assert.rejects(
    Promise.resolve().then(function () {
      return miniAppSdk.wechatDownloadFile({ url: 'https://example.com/file.bin' });
    }),
    (error) => error.name === 'UnsupportedCapability',
  );
  global.wx.downloadFile = missingApiDownload;
  supportedSchemas.add('downloadFile');

  // Standard payment. The capability key answers whether the host exposes the API, and
  // nothing else: with no merchant configured the API is still present, so this stays
  // Supported and a payment failure must not change it.
  assert.equal(miniAppSdk.capabilitySupport('wechat.request-payment').state, 'Supported');

  // Legal arguments, in the shape the example page's placeholder uses. This is not a real
  // order and cannot be: only a trusted backend can produce one, so the values are
  // obviously synthetic and the test never prints the bag.
  const legalPayment = function legalPayment() {
    return {
      timeStamp: '1700000000',
      nonceStr: 'node-nonce',
      package: 'prepay_id=node',
      signType: 'HMAC-SHA256',
      paySign: 'node-signature',
    };
  };

  const paymentOutcome = await miniAppSdk.wechatRequestPayment(legalPayment());
  const forwardedPayment = paymentCalls[paymentCalls.length - 1];
  assert.deepEqual(
    forwardedPayment.keys.filter(
      (key) => key !== 'success' && key !== 'fail' && key !== 'complete',
    ),
    ['timeStamp', 'nonceStr', 'package', 'signType', 'paySign'],
  );
  assert.equal(forwardedPayment.package, 'prepay_id=node');
  assert.equal(forwardedPayment.signType, 'HMAC-SHA256');

  // What the caller receives says one thing: the host reported the interaction completed.
  // There is no order field in it to mistake for one, which is the point — the
  // authoritative order state is the consumer backend's, never the client's.
  assert.deepEqual(Object.keys(paymentOutcome), ['interactionCompleted']);
  assert.equal(paymentOutcome.interactionCompleted, true);
  assert.equal(paymentOutcome.paid, undefined);
  assert.equal(paymentOutcome.orderConfirmed, undefined);

  // A later callback never changes the first terminal answer, whichever order they arrive
  // in, and a repeated success does not settle twice.
  paymentHold = true;
  const heldPayment = miniAppSdk.wechatRequestPayment(legalPayment());
  const heldOptions = paymentPending;
  assert.notEqual(heldOptions, null);
  heldOptions.success({ errMsg: 'requestPayment:ok' });
  heldOptions.fail({ errMsg: 'requestPayment:fail too late' });
  heldOptions.success({ errMsg: 'requestPayment:ok' });
  const heldOutcome = await heldPayment;
  assert.equal(heldOutcome.interactionCompleted, true);
  paymentPending = null;
  paymentHold = false;

  // An ended interaction is the SDK's interruption rather than a plain host failure. The
  // host uses one signal for a dismissal and for every other way the interaction ends, and
  // the SDK does not claim to know which it was — which is why this is not reported as a
  // user cancellation either.
  paymentFailure = 'requestPayment:cancel';
  await assert.rejects(
    miniAppSdk.wechatRequestPayment(legalPayment()),
    (error) => error.name === 'HostInteractionInterrupted',
  );

  // The near miss stays a host failure. The host's other interfaces use this form, but no
  // evidence covers it for this API, and a guess here would decide whether a payment was
  // cancelled or broken.
  paymentFailure = 'requestPayment:fail cancel';
  await assert.rejects(
    miniAppSdk.wechatRequestPayment(legalPayment()),
    (error) => error.name === 'HostFailure',
  );

  paymentFailure = 'requestPayment:fail merchant not configured';
  await assert.rejects(
    miniAppSdk.wechatRequestPayment(legalPayment()),
    (error) => error.name === 'HostFailure',
  );
  paymentFailure = null;

  // Caller mistakes are refused rather than quietly corrected, and the host is not asked:
  // a field naming nothing, and a sign type outside the two the host accepts.
  const paymentCallsBeforeRefusals = paymentCalls.length;
  const unsupportedSignType = legalPayment();
  unsupportedSignType.signType = 'SHA1';
  await assert.rejects(
    miniAppSdk.wechatRequestPayment(unsupportedSignType),
    /Unsupported payment sign type/,
  );
  // The example page's committed placeholder is empty on purpose, and this is the property
  // that makes it safe: with nothing configured the SDK refuses before the host is called,
  // so an unconfigured card can never open a payment interface.
  await assert.rejects(
    miniAppSdk.wechatRequestPayment({
      timeStamp: '',
      nonceStr: '',
      package: '',
      signType: 'HMAC-SHA256',
      paySign: '',
    }),
    /requestPayment requires a/,
  );
  assert.equal(paymentCalls.length, paymentCallsBeforeRefusals);

  // A host without the API fails through the capability path rather than throwing out of
  // the JavaScript boundary.
  const paymentApi = global.wx.requestPayment;
  delete global.wx.requestPayment;
  supportedSchemas.delete('requestPayment');
  assert.equal(miniAppSdk.capabilitySupport('wechat.request-payment').state, 'Unsupported');
  await assert.rejects(
    miniAppSdk.wechatRequestPayment(legalPayment()),
    (error) => error.name === 'UnsupportedCapability',
  );
  global.wx.requestPayment = paymentApi;
  supportedSchemas.add('requestPayment');

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
  console.log('[node-smoke] location: PASS');
  console.log('[node-smoke] scanner: PASS');
  console.log('[node-smoke] media: PASS');
  console.log('[node-smoke] subscription: PASS');
  console.log('[node-smoke] network extensions: PASS');
  console.log('[node-smoke] standard payment: PASS');
}

main().catch((error) => {
  console.error(error);
  process.exitCode = 1;
});
