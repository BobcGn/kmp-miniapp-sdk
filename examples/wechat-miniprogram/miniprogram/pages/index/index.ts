import MiniAppSdk = require('../../libs/kmp-miniapp-sdk');

const sdkVersion: string = MiniAppSdk.sdkVersion();
const storageKey = 'kmp-miniapp-sdk.storage-smoke';
// Any HTTPS endpoint works here. WeChat Developer Tools must either list this
// host in the request domain whitelist or run with domain checking disabled.
const networkUrl = 'https://example.com/';
const secondPageRoute = '/pages/second/index';
// The only permission this SDK maps today. It is requested only from a button.
const permissionName: MiniAppSdk.PermissionName = 'microphone';

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
});
