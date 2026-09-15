import MiniAppSdk = require('../../libs/kmp-miniapp-sdk');

const sdkVersion: string = MiniAppSdk.sdkVersion();
const storageKey = 'kmp-miniapp-sdk.storage-smoke';
// Any HTTPS endpoint works here. WeChat Developer Tools must either list this
// host in the request domain whitelist or run with domain checking disabled.
const networkUrl = 'https://example.com/';
const secondPageRoute = '/pages/second/index';
// The only permission this SDK maps today. It is requested only from a button.
const permissionName: MiniAppSdk.PermissionName = 'microphone';
// A fixed, non-sensitive string this page writes itself. The page only ever
// compares against what it wrote; it never shows or logs clipboard contents.
const clipboardTestText = 'kmp-miniapp-sdk clipboard test';
// The file this page writes, reads, and removes. Only the file name is ever
// displayed; the sandbox root it sits under is never printed.
const testFileName = 'kmp-miniapp-sdk-bob72.txt';
const testFileContent = 'kmp-miniapp-sdk bob72 test';

interface IndexPageData {
  sdkVersion: string;
  lifecycleState: string;
  pageRoute: string;
  runtimeStatus: string;
  runtimeDetails: string;
  storageStatus: string;
  storageDetails: string;
  authStatus: string;
  authDetails: string;
  networkStatus: string;
  networkDetails: string;
  navigationStatus: string;
  navigationDetails: string;
  permissionName: string;
  permissionStatus: string;
  permissionDetails: string;
  privacyRequirement: string;
  privacyContract: string;
  privacyLastAction: string;
  privacyDetails: string;
  sessionStatus: string;
  sessionDetails: string;
  sessionChecks: number;
  clipboardWriteStatus: string;
  clipboardReadStatus: string;
  clipboardDetails: string;
  hapticsShortStatus: string;
  hapticsLongStatus: string;
  hapticsDetails: string;
  fileSystemPath: string;
  fileWriteStatus: string;
  fileReadStatus: string;
  fileAccessStatus: string;
  fileRemoveStatus: string;
  fileSystemDetails: string;
}

type IndexPage = MiniProgramPageInstance<IndexPageData>;

console.log('[kmp-miniapp-sdk] sdkVersion:', sdkVersion);

/** Mirrors the lifecycle state the SDK recorded into the card the user sees. */
function showLifecycle(page: IndexPage): void {
  page.setData({
    lifecycleState: MiniAppSdk.wechatAppLifecycleState(),
    pageRoute: MiniAppSdk.wechatPageRoute() || '(none)',
  });
}

/**
 * Reads what the runtime reports and how the gate answers for it.
 *
 * This is the check whose expected value depends on the base library: on one older
 * than 2.20.1 the gate reports `VersionDependent` for the runtime-detection
 * capability instead of `Supported`. Both are correct answers, so the check
 * cross-checks the state against the host's own answer for the inspection API
 * rather than pinning one state.
 */
function runRuntimeDetectionCheck(page: IndexPage): void {
  try {
    const runtimeInfo: MiniAppSdk.RuntimeInfo = MiniAppSdk.wechatRuntimeInfo();
    const support: MiniAppSdk.CapabilitySupportResult =
      MiniAppSdk.capabilitySupport('wechat.runtime-detection');
    const unknownSupport: MiniAppSdk.CapabilitySupportResult =
      MiniAppSdk.capabilitySupport('capability-not-registered-by-this-sdk');

    // The inspection API existing means the base library is new enough, so a
    // version-dependent answer alongside it would contradict the host itself.
    if (MiniAppSdk.wechatCanIUse('getAppBaseInfo') && support.state !== 'Supported') {
      throw new Error(`The host provides getAppBaseInfo yet the gate reported ${support.state}`);
    }
    if (unknownSupport.state !== 'Unsupported') {
      throw new Error(`An ungated capability was reported as ${unknownSupport.state}`);
    }

    // A capability the host really provides must pass the requirement guard.
    MiniAppSdk.requireCapability('storage');

    const detailParts: string[] = [
      `baseLibrary=${runtimeInfo.baseLibraryVersion ?? 'unreadable'}`,
      `platform=${runtimeInfo.platform ?? 'unreadable'}`,
      `runtime-detection=${support.state}`,
      `storage=${MiniAppSdk.capabilitySupport('storage').state}`,
      `ungated=${unknownSupport.state}`,
    ];
    if (support.state === 'VersionDependent') {
      detailParts.push(
        `requires>=${support.requiredVersion ?? 'unreadable'}`,
        `current=${support.currentVersion ?? 'unreadable'}`,
      );
    }
    const details = detailParts.join(', ');

    console.log('[kmp-miniapp-sdk] runtime detection: PASS', details);
    page.setData({ runtimeStatus: 'PASS', runtimeDetails: details });
  } catch (error) {
    const details = String(error);
    console.error('[kmp-miniapp-sdk] runtime detection: FAIL', error);
    page.setData({ runtimeStatus: 'FAIL', runtimeDetails: details });
  }
}

/**
 * Reads the permission state from the host.
 *
 * Nothing in this page requests a permission while loading: a prompt may only
 * follow a tap, which is what the host requires and what a user should expect.
 */
async function refreshPermission(this: IndexPage): Promise<void> {
  try {
    const state = await MiniAppSdk.permissionState(permissionName);
    const details = `permission=${permissionName}, state=${state}`;
    console.log('[kmp-miniapp-sdk] permission query: PASS', details);
    this.setData({ permissionStatus: state, permissionDetails: details });
  } catch (error) {
    const details = String(error);
    console.error('[kmp-miniapp-sdk] permission query: FAIL', error);
    this.setData({ permissionStatus: 'FAIL', permissionDetails: details });
  }
}

async function requestPermission(this: IndexPage): Promise<void> {
  try {
    const state = await MiniAppSdk.requestPermission(permissionName);
    const details = `permission=${permissionName}, state=${state}`;
    console.log('[kmp-miniapp-sdk] permission request: PASS', details);
    this.setData({ permissionStatus: state, permissionDetails: details });
  } catch (error) {
    // A refusal and a host failure both reject, so the host's own state decides
    // which one this was rather than an error message.
    try {
      const state = await MiniAppSdk.permissionState(permissionName);
      if (state === 'Denied') {
        const details = `permission=${permissionName}, state=${state}`;
        console.error('[kmp-miniapp-sdk] permission request: DENIED', details);
        this.setData({ permissionStatus: state, permissionDetails: details });
        return;
      }
    } catch (stateError) {
      // Fall through to the generic failure below.
      console.error('[kmp-miniapp-sdk] permission state after failure: FAIL', stateError);
    }

    const details = String(error);
    console.error('[kmp-miniapp-sdk] permission request: FAIL', error);
    this.setData({ permissionStatus: 'FAIL', permissionDetails: details });
  }
}

async function openPermissionSettings(this: IndexPage): Promise<void> {
  try {
    // The settings page closing is not a grant; the state below is the host's.
    const state = await MiniAppSdk.openPermissionSettings(permissionName);
    const details = `permission=${permissionName}, state=${state}`;
    console.log('[kmp-miniapp-sdk] permission settings: PASS', details);
    this.setData({ permissionStatus: state, permissionDetails: details });
  } catch (error) {
    const details = String(error);
    console.error('[kmp-miniapp-sdk] permission settings: FAIL', error);
    this.setData({ permissionStatus: 'FAIL', permissionDetails: details });
  }
}

/**
 * Reads what the host currently requires for its privacy contract.
 *
 * This is a query only. It has no side effect, shows nothing, and is therefore
 * safe to run while the page loads; asking the user is a separate step that only
 * a tap may start.
 */
async function readPrivacy(page: IndexPage): Promise<void> {
  try {
    const status: MiniAppSdk.PrivacyStatusResult = await MiniAppSdk.privacyStatus();
    const contract = status.contractName ?? 'unavailable';
    const details = `requirement=${status.requirement}, contract=${contract}`;
    console.log('[kmp-miniapp-sdk] privacy query: PASS', details);
    page.setData({
      privacyRequirement: status.requirement,
      privacyContract: contract,
      privacyDetails: details,
    });
  } catch (error) {
    const details = String(error);
    console.error('[kmp-miniapp-sdk] privacy query: FAIL', error);
    page.setData({
      privacyRequirement: 'UNKNOWN',
      privacyContract: 'unavailable',
      privacyDetails: details,
    });
  }
}

async function refreshPrivacy(this: IndexPage): Promise<void> {
  await readPrivacy(this);
}

async function requestPrivacy(this: IndexPage): Promise<void> {
  try {
    const result: MiniAppSdk.PrivacyAuthorizationResult =
      await MiniAppSdk.requestPrivacyAuthorization();
    const details = `result=${result}`;

    if (result === 'Authorized') {
      console.log('[kmp-miniapp-sdk] privacy request: PASS', details);
    } else {
      // WeChat reports a declined and a dismissed prompt through the same path,
      // so this is the one refusal state the host actually provides.
      console.error('[kmp-miniapp-sdk] privacy request: REFUSED', details);
    }

    this.setData({ privacyLastAction: result === 'Authorized' ? 'AUTHORIZED' : 'REFUSED' });
    // The requirement is the host's answer after the prompt, so it is re-read
    // rather than assumed.
    await readPrivacy(this);
  } catch (error) {
    const details = String(error);
    console.error('[kmp-miniapp-sdk] privacy request: FAIL', error);
    this.setData({
      privacyLastAction: 'HOST_FAILURE',
      privacyDetails: details,
    });
  }
}

/**
 * Asks WeChat whether its own client login session is still usable.
 *
 * A valid answer says only that WeChat's login state is intact. It is not an
 * authenticated user, a backend session, or an accepted credential, and an
 * invalid answer changes nothing by itself: acquiring a new login code stays the
 * consumer's decision.
 */
async function runSessionCheck(page: IndexPage): Promise<void> {
  const attempt = page.data.sessionChecks + 1;

  try {
    const state: MiniAppSdk.WeChatSessionState = await MiniAppSdk.wechatCheckSession();
    const status = state === 'Valid' ? 'VALID' : 'INVALID';
    const details = `check #${attempt}, state=${state}`;
    console.log('[kmp-miniapp-sdk] session check: PASS', details);
    page.setData({ sessionStatus: status, sessionDetails: details, sessionChecks: attempt });
  } catch (error) {
    const details = `check #${attempt}, ${String(error)}`;
    console.error('[kmp-miniapp-sdk] session check: FAIL', error);
    page.setData({ sessionStatus: 'FAIL', sessionDetails: details, sessionChecks: attempt });
  }
}

async function checkSession(this: IndexPage): Promise<void> {
  await runSessionCheck(this);
}

/**
 * Writes a fixed test string to the clipboard.
 *
 * Nothing touches the clipboard while the page loads; every clipboard and
 * haptics action below is triggered by a tap.
 */
async function writeClipboardTest(this: IndexPage): Promise<void> {
  try {
    await MiniAppSdk.wechatSetClipboardText(clipboardTestText);
    console.log('[kmp-miniapp-sdk] clipboard write: PASS');
    this.setData({
      clipboardWriteStatus: 'PASS',
      clipboardDetails: `wrote ${clipboardTestText.length} characters`,
    });
  } catch (error) {
    const details = String(error);
    console.error('[kmp-miniapp-sdk] clipboard write: FAIL', error);
    this.setData({ clipboardWriteStatus: 'FAIL', clipboardDetails: details });
  }
}

/**
 * Reads the clipboard and compares it with the test string this page wrote.
 *
 * Only the comparison result is reported. Whatever else the clipboard may hold is
 * the user's, so it is never displayed or logged.
 */
async function readClipboard(this: IndexPage): Promise<void> {
  try {
    const value = await MiniAppSdk.wechatGetClipboardText();
    const matched = value === clipboardTestText;
    const details = `matched=${matched}`;

    if (matched) {
      console.log('[kmp-miniapp-sdk] clipboard read: PASS matched=true');
    } else {
      // The mismatch is reported without revealing either side.
      console.error('[kmp-miniapp-sdk] clipboard read: FAIL matched=false');
    }

    this.setData({ clipboardReadStatus: matched ? 'PASS' : 'FAIL', clipboardDetails: details });
  } catch (error) {
    const details = String(error);
    console.error('[kmp-miniapp-sdk] clipboard read: FAIL', error);
    this.setData({ clipboardReadStatus: 'FAIL', clipboardDetails: details });
  }
}

async function vibrateShort(this: IndexPage): Promise<void> {
  try {
    await MiniAppSdk.wechatVibrateShort();
    // The host accepted the call. Whether a vibration was felt is for the person
    // holding the device to say, not for this log to claim.
    console.log('[kmp-miniapp-sdk] haptics short: PASS');
    this.setData({ hapticsShortStatus: 'PASS', hapticsDetails: 'host accepted the short call' });
  } catch (error) {
    const details = String(error);
    console.error('[kmp-miniapp-sdk] haptics short: FAIL', error);
    this.setData({ hapticsShortStatus: 'FAIL', hapticsDetails: details });
  }
}

async function vibrateLong(this: IndexPage): Promise<void> {
  try {
    await MiniAppSdk.wechatVibrateLong();
    console.log('[kmp-miniapp-sdk] haptics long: PASS');
    this.setData({ hapticsLongStatus: 'PASS', hapticsDetails: 'host accepted the long call' });
  } catch (error) {
    const details = String(error);
    console.error('[kmp-miniapp-sdk] haptics long: FAIL', error);
    this.setData({ hapticsLongStatus: 'FAIL', hapticsDetails: details });
  }
}

/**
 * Builds the sandbox path this page operates on.
 *
 * The root comes from the host; the SDK never guesses it. Only the file name is
 * ever shown to the user.
 */
function testFilePath(): string {
  return `${MiniAppSdk.wechatUserDataPath()}/${testFileName}`;
}

/** Writes a fixed, non-sensitive test string to the sandbox. */
async function writeTestFile(this: IndexPage): Promise<void> {
  try {
    await MiniAppSdk.wechatWriteTextFile(testFilePath(), testFileContent);
    console.log('[kmp-miniapp-sdk] filesystem write: PASS');
    this.setData({ fileWriteStatus: 'PASS', fileSystemDetails: `wrote ${testFileName}` });
  } catch (error) {
    const details = String(error);
    console.error('[kmp-miniapp-sdk] filesystem write: FAIL', error);
    this.setData({ fileWriteStatus: 'FAIL', fileSystemDetails: details });
  }
}

/** Reads the test file back and compares it with what this page wrote. */
async function readTestFile(this: IndexPage): Promise<void> {
  try {
    const content = await MiniAppSdk.wechatReadTextFile(testFilePath());
    const matched = content === testFileContent;

    if (matched) {
      console.log('[kmp-miniapp-sdk] filesystem read: PASS matched=true');
    } else {
      console.error('[kmp-miniapp-sdk] filesystem read: FAIL matched=false');
    }

    this.setData({ fileReadStatus: matched ? 'PASS' : 'FAIL', fileSystemDetails: `matched=${matched}` });
  } catch (error) {
    const details = String(error);
    console.error('[kmp-miniapp-sdk] filesystem read: FAIL', error);
    this.setData({ fileReadStatus: 'FAIL', fileSystemDetails: details });
  }
}

/** Reports whether the test file is there. */
async function checkTestFile(this: IndexPage): Promise<void> {
  try {
    const exists = await MiniAppSdk.wechatFileExists(testFilePath());
    console.log('[kmp-miniapp-sdk] filesystem access: PASS exists=' + exists);
    this.setData({ fileAccessStatus: 'PASS', fileSystemDetails: `exists=${exists}` });
  } catch (error) {
    const details = String(error);
    console.error('[kmp-miniapp-sdk] filesystem access: FAIL', error);
    this.setData({ fileAccessStatus: 'FAIL', fileSystemDetails: details });
  }
}

/** Removes the test file. */
async function removeTestFile(this: IndexPage): Promise<void> {
  try {
    await MiniAppSdk.wechatRemoveFile(testFilePath());
    console.log('[kmp-miniapp-sdk] filesystem remove: PASS');
    this.setData({ fileRemoveStatus: 'PASS', fileSystemDetails: `removed ${testFileName}` });
  } catch (error) {
    const details = String(error);
    console.error('[kmp-miniapp-sdk] filesystem remove: FAIL', error);
    this.setData({ fileRemoveStatus: 'FAIL', fileSystemDetails: details });
  }
}

/** Reports whether the test file is gone after removal. */
async function checkRemovedFile(this: IndexPage): Promise<void> {
  try {
    const exists = await MiniAppSdk.wechatFileExists(testFilePath());
    if (exists) {
      console.error('[kmp-miniapp-sdk] filesystem access: FAIL exists=true');
    } else {
      console.log('[kmp-miniapp-sdk] filesystem access: PASS exists=false');
    }
    this.setData({
      fileAccessStatus: exists ? 'FAIL' : 'PASS',
      fileSystemDetails: `exists=${exists}`,
    });
  } catch (error) {
    const details = String(error);
    console.error('[kmp-miniapp-sdk] filesystem access: FAIL', error);
    this.setData({ fileAccessStatus: 'FAIL', fileSystemDetails: details });
  }
}

async function runStorageCheck(page: IndexPage): Promise<void> {
  try {
    await MiniAppSdk.storageSet(storageKey, 'first');
    const first = await MiniAppSdk.storageGet(storageKey);

    await MiniAppSdk.storageSet(storageKey, 'second');
    const overwritten = await MiniAppSdk.storageGet(storageKey);

    await MiniAppSdk.storageRemove(storageKey);
    const missing = await MiniAppSdk.storageGet(storageKey);

    if (first !== 'first' || overwritten !== 'second' || missing != null) {
      throw new Error('Unexpected Storage result');
    }

    const details = `first=${first}, overwritten=${overwritten}, missing=null`;
    console.log('[kmp-miniapp-sdk] storage: PASS', details);
    page.setData({ storageStatus: 'PASS', storageDetails: details });
  } catch (error) {
    const details = String(error);
    console.error('[kmp-miniapp-sdk] storage: FAIL', error);
    page.setData({ storageStatus: 'FAIL', storageDetails: details });
  }
}

async function runAuthCheck(page: IndexPage): Promise<void> {
  try {
    const loginResult: MiniAppSdk.WeChatLoginResult = await MiniAppSdk.wechatLogin();
    if (loginResult.code.length === 0) {
      throw new Error('WeChat returned an empty login code');
    }

    // Never print or render the credential itself. It must be sent to a trusted
    // backend for exchange before the consumer considers a user authenticated.
    const details = `codeReceived=true, length=${loginResult.code.length}`;
    console.log('[kmp-miniapp-sdk] auth bootstrap: PASS', details);
    page.setData({ authStatus: 'PASS', authDetails: details });
  } catch (error) {
    const details = String(error);
    console.error('[kmp-miniapp-sdk] auth bootstrap: FAIL', error);
    page.setData({ authStatus: 'FAIL', authDetails: details });
  }
}

async function runNetworkCheck(page: IndexPage): Promise<void> {
  try {
    const response: MiniAppSdk.NetworkResponse = await MiniAppSdk.networkRequest(networkUrl, {
      method: 'GET',
      timeoutMillis: 10000,
    });

    if (response.statusCode !== 200) {
      throw new Error(`WeChat completed the exchange with status ${response.statusCode}`);
    }

    const details = `status=${response.statusCode}, bytes=${response.body.length}`;
    console.log('[kmp-miniapp-sdk] network: PASS', details);
    page.setData({ networkStatus: 'PASS', networkDetails: details });
  } catch (error) {
    const details = String(error);
    console.error('[kmp-miniapp-sdk] network: FAIL', error);
    page.setData({ networkStatus: 'FAIL', networkDetails: details });
  }
}

async function onLoad(this: IndexPage): Promise<void> {
  runRuntimeDetectionCheck(this);
  // A query only: no prompt is started while the page loads.
  await readPrivacy(this);
  // Deliberately before the auth bootstrap below: acquiring a login code
  // refreshes the client login state, which would hide an expired session from
  // the first check and make an invalid result impossible to observe.
  await runSessionCheck(this);
  await runStorageCheck(this);
  await runAuthCheck(this);
  await runNetworkCheck(this);
}

function onShow(this: IndexPage): void {
  // WeChat reports the route on the page itself, so the page supplies its own.
  MiniAppSdk.wechatPageOnShow(this.route);
  showLifecycle(this);
}

function onHide(this: IndexPage): void {
  MiniAppSdk.wechatPageOnHide();
}

function onUnload(this: IndexPage): void {
  MiniAppSdk.wechatPageOnUnload(this.route);
}

async function openSecondPage(this: IndexPage): Promise<void> {
  try {
    await MiniAppSdk.wechatNavigateTo(secondPageRoute);
    const details = `wx.navigateTo ${secondPageRoute}`;
    console.log('[kmp-miniapp-sdk] navigation: PASS', details);
    this.setData({ navigationStatus: 'PASS', navigationDetails: details });
  } catch (error) {
    const details = String(error);
    console.error('[kmp-miniapp-sdk] navigation: FAIL', error);
    this.setData({ navigationStatus: 'FAIL', navigationDetails: details });
  }
}

Page<IndexPageData>({
  data: {
    sdkVersion,
    lifecycleState: 'UNKNOWN',
    pageRoute: '(none)',
    runtimeStatus: 'RUNNING',
    runtimeDetails: 'Reading the runtime and the capability gate…',
    storageStatus: 'RUNNING',
    storageDetails: 'Checking set, get, overwrite, remove, and missing key…',
    authStatus: 'RUNNING',
    authDetails: 'Requesting a short-lived WeChat login code…',
    networkStatus: 'RUNNING',
    networkDetails: 'Requesting an HTTPS endpoint through wx.request…',
    navigationStatus: 'READY',
    navigationDetails: 'Tap the button to open the second page.',
    permissionName,
    privacyRequirement: 'UNKNOWN',
    privacyContract: 'unavailable',
    privacyLastAction: 'NOT_RUN',
    privacyDetails: 'Reading the host privacy requirement…',
    sessionStatus: 'UNKNOWN',
    sessionDetails: 'Not checked yet.',
    sessionChecks: 0,
    clipboardWriteStatus: 'READY',
    clipboardReadStatus: 'READY',
    clipboardDetails: 'Tap a button; nothing touches the clipboard on load.',
    hapticsShortStatus: 'READY',
    hapticsLongStatus: 'READY',
    hapticsDetails: 'Tap a button; nothing vibrates on load.',
    fileSystemPath: testFileName,
    fileWriteStatus: 'READY',
    fileReadStatus: 'READY',
    fileAccessStatus: 'READY',
    fileRemoveStatus: 'READY',
    fileSystemDetails: 'Tap a button; nothing touches the file system on load.',
    permissionStatus: 'UNKNOWN',
    permissionDetails:
      'Permission is never requested on load. Tap a button to query or request it.',
  },
  onLoad,
  onShow,
  onHide,
  onUnload,
  openSecondPage,
  refreshPermission,
  requestPermission,
  openPermissionSettings,
  refreshPrivacy,
  requestPrivacy,
  checkSession,
  writeClipboardTest,
  readClipboard,
  vibrateShort,
  vibrateLong,
  writeTestFile,
  readTestFile,
  checkTestFile,
  removeTestFile,
  checkRemovedFile,
});
