import MiniAppSdk = require('../../libs/kmp-miniapp-sdk');

const sdkVersion: string = MiniAppSdk.sdkVersion();
const storageKey = 'kmp-miniapp-sdk.storage-smoke';
// Any HTTPS endpoint works here. WeChat Developer Tools must either list this
// host in the request domain whitelist or run with domain checking disabled.
const networkUrl = 'https://example.com/';
const secondPageRoute = '/pages/second/index';
// Declared in app.json's tabBar, which is the only kind of route wx.switchTab accepts.
const tabTargetRoute = '/pages/tabtarget/index';
// A page the example deliberately registers outside the tabBar, to show what the host
// reports when switchTab is asked for a route it has no tab for.
const nonTabRoute = '/pages/third/index';
// The only permission this SDK maps today. It is requested only from a button.
const permissionName: MiniAppSdk.PermissionName = 'microphone';
// A fixed, non-sensitive string this page writes itself. The page only ever
// compares against what it wrote; it never shows or logs clipboard contents.
const clipboardTestText = 'kmp-miniapp-sdk clipboard test';
// The file this page writes, reads, and removes. Only the file name is ever
// displayed; the sandbox root it sits under is never printed.
const testFileName = 'kmp-miniapp-sdk-bob72.txt';
const testFileContent = 'kmp-miniapp-sdk bob72 test';
// Subscription template ids are host identifiers that belong to this mini program's
// account, so none is committed here: paste your own test template ids locally to try
// the card, and do not commit them. While this is empty the page refuses to ask the
// host anything and reports NOT CONFIGURED, so the card never fires a request without
// a deliberate local edit. The page never displays or logs these values.
const subscriptionTemplateIds: string[] = [];
// Endpoints for the network extension checks. Both are empty on purpose: accepting an
// upload or a download needs a controlled HTTPS service the consumer provides, so none
// is committed and no request is made until you fill one in locally. The page never
// logs a URL, a header, or a response body.
const uploadTestUrl: string = '';
const downloadTestUrl: string = '';
// The fixed, non-sensitive file this page writes itself before uploading. Only its name
// is ever displayed, and the sandbox root is never printed.
const uploadTestFileName = 'kmp-miniapp-sdk-bob68-upload.txt';
const uploadTestContent = 'kmp-miniapp-sdk bob68 upload';
// The in-flight transfers, so the cancel buttons can reach the one they cancel.
let activeUpload: MiniAppSdk.UploadTransfer | null = null;
let activeDownload: MiniAppSdk.DownloadTransfer | null = null;
let cancelledUpload: MiniAppSdk.UploadTransfer | null = null;
let cancelledDownload: MiniAppSdk.DownloadTransfer | null = null;
// Payment parameters are produced by a trusted backend from WeChat Pay's unified-order
// API. The SDK does not sign, holds no merchant key, and cannot invent them, so nothing
// real is committed here and the placeholder below is deliberately empty: the card's
// button therefore reports NOT CONFIGURED rather than opening a payment interface. Fill it
// in locally, from your own backend, only with a legal merchant environment, and never
// commit it — `nonceStr`, `package`, and `paySign` are credentials for one payment, and
// this page never displays or logs any of them.
const paymentRequest: MiniAppSdk.PaymentRequest = {
  timeStamp: '',
  nonceStr: '',
  package: '',
  signType: 'HMAC-SHA256',
  paySign: '',
};

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
  switchTabStatus: string;
  switchTabDetails: string;
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
  locationPermission: string;
  locationPrivacy: string;
  locationStatus: string;
  locationDetails: string;
  scannerStatus: string;
  scannerDetails: string;
  mediaStatus: string;
  mediaDetails: string;
  subscriptionStatus: string;
  subscriptionDetails: string;
  networkCapabilityStatus: string;
  networkCapabilityDetails: string;
  networkTypeStatus: string;
  networkTypeDetails: string;
  observationStatus: string;
  observationDetails: string;
  uploadStatus: string;
  uploadDetails: string;
  downloadStatus: string;
  downloadDetails: string;
  paymentStatus: string;
  paymentDetails: string;
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

/**
 * Reports how the gate answers for the location capability.
 *
 * This is a different question from the permission and the privacy requirement
 * the other buttons report: the API can exist while the permission is refused,
 * and the answer is read from the host rather than from a version table.
 */
function checkLocationCapability(this: IndexPage): void {
  try {
    const support: MiniAppSdk.CapabilitySupportResult =
      MiniAppSdk.capabilitySupport('wechat.location');
    console.log('[kmp-miniapp-sdk] location capability: PASS wechat.location=' + support.state);
    this.setData({
      locationStatus: support.state,
      locationDetails: 'wechat.location=' + support.state,
    });
  } catch (error) {
    console.error('[kmp-miniapp-sdk] location capability: FAIL', error);
    this.setData({ locationStatus: 'FAIL', locationDetails: String(error) });
  }
}

/**
 * Reports the location permission the host currently holds.
 *
 * This is a query only. The location capability needs `scope.userLocation`, and
 * the permission lifecycle is a separate question from whether the API exists.
 */
async function refreshLocationPermission(this: IndexPage): Promise<void> {
  try {
    const state = await MiniAppSdk.permissionState('location');
    console.log('[kmp-miniapp-sdk] location permission query: PASS state=' + state);
    this.setData({ locationPermission: state, locationDetails: 'permission=' + state });
  } catch (error) {
    const details = String(error);
    console.error('[kmp-miniapp-sdk] location permission query: FAIL', error);
    this.setData({ locationPermission: 'FAIL', locationDetails: details });
  }
}

/** Asks the host for the location permission. A tap is required. */
async function requestLocationPermission(this: IndexPage): Promise<void> {
  try {
    const state = await MiniAppSdk.requestPermission('location');
    console.log('[kmp-miniapp-sdk] location permission request: PASS state=' + state);
    this.setData({ locationPermission: state, locationDetails: 'permission=' + state });
  } catch (error) {
    // A refusal and a host failure both reject, so the host's own state decides
    // which one this was.
    try {
      const state = await MiniAppSdk.permissionState('location');
      if (state === 'Denied') {
        console.error('[kmp-miniapp-sdk] location permission request: DENIED state=' + state);
        this.setData({ locationPermission: state, locationDetails: 'permission=' + state });
        return;
      }
    } catch (stateError) {
      console.error('[kmp-miniapp-sdk] location permission state after failure: FAIL', stateError);
    }

    const details = String(error);
    console.error('[kmp-miniapp-sdk] location permission request: FAIL', error);
    this.setData({ locationPermission: 'FAIL', locationDetails: details });
  }
}

/**
 * Reports what the host currently requires for its privacy contract.
 *
 * Takes the page rather than binding `this`, so the privacy request handler can
 * reuse it without working around a bound receiver.
 */
async function readLocationPrivacy(page: IndexPage): Promise<void> {
  try {
    const status: MiniAppSdk.PrivacyStatusResult = await MiniAppSdk.privacyStatus();
    console.log('[kmp-miniapp-sdk] location privacy query: PASS requirement=' + status.requirement);
    page.setData({ locationPrivacy: status.requirement });
  } catch (error) {
    const details = String(error);
    console.error('[kmp-miniapp-sdk] location privacy query: FAIL', error);
    page.setData({ locationPrivacy: 'FAIL', locationDetails: details });
  }
}

async function refreshLocationPrivacy(this: IndexPage): Promise<void> {
  await readLocationPrivacy(this);
}

/**
 * Asks the host to obtain the user's acceptance of its privacy contract.
 *
 * A tap is required. Resolving does not mean the contract was accepted.
 */
async function requestLocationPrivacy(this: IndexPage): Promise<void> {
  try {
    const result: MiniAppSdk.PrivacyAuthorizationResult =
      await MiniAppSdk.requestPrivacyAuthorization();
    console.log('[kmp-miniapp-sdk] location privacy request: result=' + result);
    await readLocationPrivacy(this);
  } catch (error) {
    const details = String(error);
    console.error('[kmp-miniapp-sdk] location privacy request: FAIL', error);
    this.setData({ locationPrivacy: 'FAIL', locationDetails: details });
  }
}

/**
 * Reads the current position and checks the shape of what came back.
 *
 * The coordinates themselves are never displayed or logged: a precise position is
 * the user's, and this check only needs to know that the SDK handed over a
 * well-formed result. `coordinatesValid` and `accuracyValid` are recomputed here
 * rather than assumed, so they are evidence about the value and not a formality.
 */
async function getCurrentLocation(this: IndexPage): Promise<void> {
  try {
    const position: MiniAppSdk.GeoPosition = await MiniAppSdk.wechatGetCurrentLocation('gcj02');

    const coordinatesValid =
      Number.isFinite(position.latitude) &&
      Number.isFinite(position.longitude) &&
      Math.abs(position.latitude) <= 90 &&
      Math.abs(position.longitude) <= 180;
    const accuracyValid =
      Number.isFinite(position.accuracyMeters) && position.accuracyMeters >= 0;

    const details = `coordinatesValid=${coordinatesValid}, accuracyValid=${accuracyValid}`;
    if (coordinatesValid && accuracyValid) {
      console.log('[kmp-miniapp-sdk] location: PASS', details);
      this.setData({ locationStatus: 'PASS', locationDetails: details });
    } else {
      console.error('[kmp-miniapp-sdk] location: FAIL', details);
      this.setData({ locationStatus: 'FAIL', locationDetails: details });
    }
  } catch (error) {
    // Re-read the two preconditions so the acceptance log can distinguish a
    // permission refusal from an unsatisfied privacy contract without exposing
    // any coordinate or relying on a localized exception message.
    try {
      const privacy = await MiniAppSdk.privacyStatus();
      if (privacy.requirement === 'REQUIRED') {
        console.error('[kmp-miniapp-sdk] location: PRIVACY_REQUIRED');
        this.setData({ locationStatus: 'PRIVACY_REQUIRED', locationDetails: 'privacy=REQUIRED' });
        return;
      }

      const permission = await MiniAppSdk.permissionState('location');
      if (permission !== 'Granted') {
        console.error('[kmp-miniapp-sdk] location: DENIED permissionState=' + permission);
        this.setData({ locationStatus: 'DENIED', locationDetails: 'permission=' + permission });
        return;
      }
    } catch (preconditionError) {
      console.error('[kmp-miniapp-sdk] location precondition query: FAIL', preconditionError);
    }

    const details = String(error);
    console.error('[kmp-miniapp-sdk] location: FAIL', error);
    this.setData({ locationStatus: 'FAIL', locationDetails: details });
  }
}

/**
 * Reports how the gate answers for the scanning capability.
 *
 * This is a smaller question than the location card's three: scanning needs no
 * permission the SDK could establish, so the API answer is the whole answer.
 */
function checkScannerCapability(this: IndexPage): void {
  try {
    const support: MiniAppSdk.CapabilitySupportResult =
      MiniAppSdk.capabilitySupport('wechat.scan-code');
    console.log('[kmp-miniapp-sdk] scanner capability: PASS wechat.scan-code=' + support.state);
    this.setData({
      scannerStatus: support.state,
      scannerDetails: 'wechat.scan-code=' + support.state,
    });
  } catch (error) {
    console.error('[kmp-miniapp-sdk] scanner capability: FAIL', error);
    this.setData({ scannerStatus: 'FAIL', scannerDetails: String(error) });
  }
}

/**
 * Opens WeChat's scanning interface and checks the shape of what came back.
 *
 * The decoded content, the character set, the raw bytes, and the image path are
 * never displayed or logged: what the user scanned is theirs, and this check only
 * needs to know that the SDK handed over a result of the contract's type and
 * whether the format the host named is one the SDK recognizes. Both are recomputed
 * here rather than assumed, so they are evidence about the value and not a
 * formality. An empty payload still counts as present, because that is what the
 * host decoded.
 */
async function runScan(page: IndexPage, onlyFromCamera: boolean): Promise<void> {
  try {
    const result: MiniAppSdk.ScanResult = await MiniAppSdk.wechatScanCode(onlyFromCamera, []);

    const resultPresent = typeof result.text === 'string';
    const typeRecognized = result.format !== null && result.format !== undefined;

    const details = `resultPresent=${resultPresent}, typeRecognized=${typeRecognized}`;
    console.log('[kmp-miniapp-sdk] scan: PASS', details);
    page.setData({ scannerStatus: 'PASS', scannerDetails: details });
  } catch (error) {
    // The real host uses one signal for both dismissal and a camera restriction.
    // The SDK preserves that uncertainty instead of assigning intent to the user.
    if (isHostInteractionInterrupted(error)) {
      console.log('[kmp-miniapp-sdk] scan: INTERRUPTED cause=indeterminate');
      page.setData({
        scannerStatus: 'INTERRUPTED',
        scannerDetails: 'The host did not distinguish cancellation from camera restriction.',
      });
      return;
    }

    console.error('[kmp-miniapp-sdk] scan: FAIL', error);
    page.setData({ scannerStatus: 'FAIL', scannerDetails: String(error) });
  }
}

async function scanCode(this: IndexPage): Promise<void> {
  await runScan(this, false);
}

/** Forces the real-device acceptance path through the camera rather than an album fallback. */
async function scanCodeFromCamera(this: IndexPage): Promise<void> {
  await runScan(this, true);
}

/** Whether the value is the SDK's user-cancelled error rather than a failure. */
function isHostInteractionInterrupted(error: unknown): boolean {
  if (typeof error !== 'object' || error === null) {
    return false;
  }
  return (error as { name?: unknown }).name === 'HostInteractionInterrupted';
}

/**
 * Reports how the gate answers for the media-selection capability.
 *
 * This is the whole answer for this capability: WeChat's own picker needs no
 * permission the host ties to it, so there is nothing else to report here.
 */
function checkMediaCapability(this: IndexPage): void {
  try {
    const support: MiniAppSdk.CapabilitySupportResult =
      MiniAppSdk.capabilitySupport('wechat.choose-media');
    console.log('[kmp-miniapp-sdk] media capability: PASS wechat.choose-media=' + support.state);
    this.setData({
      mediaStatus: support.state,
      mediaDetails: 'wechat.choose-media=' + support.state,
    });
  } catch (error) {
    console.error('[kmp-miniapp-sdk] media capability: FAIL', error);
    this.setData({ mediaStatus: 'FAIL', mediaDetails: String(error) });
  }
}

/**
 * A safe one-line description of a failed host interaction.
 *
 * The host's own message is deliberately left out: a failure message can carry part
 * of a path or a file name, and this page prints neither. The raw error object is
 * deliberately not logged for the same reason.
 */
function failureReason(error: unknown): string {
  if (typeof error === 'object' && error !== null) {
    const named = error as { name?: unknown };
    if (typeof named.name === 'string' && named.name.length > 0) {
      return named.name;
    }
  }
  return 'unknown';
}

/**
 * Classifies the media host signal without printing the signal itself.
 *
 * A host message may contain a file name or path, so the diagnostic is deliberately
 * a closed label. The two exact candidates are separated because the next real-host
 * run needs to establish which one WeChat actually returns for a manual dismissal.
 */
function mediaFailureSignal(error: unknown): string {
  if (typeof error !== 'object' || error === null) {
    return 'unavailable';
  }

  // Kotlin keeps hostMessage internal at the JavaScript boundary, while the
  // standard Error.message contains the HostFailure prefix plus that text.
  const message = (error as { message?: unknown }).message;
  if (typeof message !== 'string') {
    return 'unavailable';
  }
  if (message === 'wechat host failure: chooseMedia:cancel') {
    return 'cancel';
  }
  if (message === 'wechat host failure: chooseMedia:fail cancel') {
    return 'fail-cancel';
  }
  if (message.toLowerCase().includes('cancel')) {
    return 'cancel-like';
  }
  return 'other';
}

/**
 * Classifies a subscription failure without logging its raw message.
 *
 * The labels are diagnostics for real-host acceptance, not semantic SDK outcomes.
 * In particular, seeing a cancel-like label does not prove user intent.
 */
function subscriptionFailureSignal(error: unknown): string {
  if (typeof error !== 'object' || error === null) {
    return 'unavailable';
  }
  const message = (error as { message?: unknown }).message;
  if (typeof message !== 'string') {
    return 'unavailable';
  }
  if (message === 'wechat host failure: requestSubscribeMessage:cancel') {
    return 'cancel';
  }
  if (message === 'wechat host failure: requestSubscribeMessage:fail cancel') {
    return 'fail-cancel';
  }
  if (message.toLowerCase().includes('cancel')) {
    return 'cancel-like';
  }
  return 'other';
}

/** Reduces host status vocabulary to non-sensitive labels for acceptance evidence. */
function subscriptionStatusSignal(status: string): string {
  switch (status) {
    case 'accept':
    case 'reject':
    case 'ban':
    case 'filter':
      return status;
    default:
      return 'other';
  }
}

/**
 * Opens WeChat's picker and checks the shape of what came back.
 *
 * The selected files are never displayed or logged: what the user picked is theirs,
 * and the temporary paths belong to the host. This check only needs to know how many
 * files came back, whether every file's kind is one the SDK recognizes, and whether
 * the metadata it reported is usable. All three are recomputed here rather than
 * assumed, so they are evidence about the values and not a formality. An image
 * reports no duration or dimensions, so those are expected to be absent rather than
 * zero. The SDK rejects a successful host response containing no selected files.
 */
async function runMediaSelection(
  page: IndexPage,
  request: MiniAppSdk.MediaRequest,
): Promise<void> {
  try {
    const files: MiniAppSdk.MediaFile[] = await MiniAppSdk.wechatChooseMedia(request);

    const count = files.length;
    const typesValid = files.every(
      (file) => file.fileType === 'image' || file.fileType === 'video',
    );
    const metadataValid = files.every((file) => {
      const sizeValid = Number.isFinite(file.sizeBytes) && file.sizeBytes >= 0;
      const pathPresent = typeof file.tempFilePath === 'string' && file.tempFilePath.length > 0;
      const optionalNumbersValid = [file.durationSeconds, file.width, file.height].every(
        (value) => value === null || value === undefined || (Number.isFinite(value) && value >= 0),
      );
      return sizeValid && pathPresent && optionalNumbersValid;
    });

    const details = `count=${count}, typesValid=${typesValid}, metadataValid=${metadataValid}`;
    console.log('[kmp-miniapp-sdk] media choose: PASS', details);
    page.setData({ mediaStatus: 'PASS', mediaDetails: details });
  } catch (error) {
    // An exact host signal can report an ended interaction without a structured
    // cause, so the page does not attribute it to user intent or a permission.
    if (isHostInteractionInterrupted(error)) {
      console.log('[kmp-miniapp-sdk] media choose: INTERRUPTED cause=indeterminate');
      page.setData({
        mediaStatus: 'INTERRUPTED',
        mediaDetails: 'the interaction ended without a selection',
      });
      return;
    }

    const reason = failureReason(error);
    const signal = mediaFailureSignal(error);
    const details = `reason=${reason}, signal=${signal}`;
    console.error('[kmp-miniapp-sdk] media choose: FAIL ' + details);
    page.setData({ mediaStatus: 'FAIL', mediaDetails: details });
  }
}

/** Chooses one image, or an image and a video, from anywhere the host allows. */
function chooseImage(this: IndexPage): Promise<void> {
  return runMediaSelection(this, { mediaTypes: ['image'] });
}

/** Chooses one video from anywhere the host allows. */
function chooseVideo(this: IndexPage): Promise<void> {
  return runMediaSelection(this, { mediaTypes: ['video'] });
}

/** Chooses an image or a video, letting the host decide which. */
function chooseImageOrVideo(this: IndexPage): Promise<void> {
  return runMediaSelection(this, { mediaTypes: ['mix'] });
}

/** Asks the host to take a new photo or video rather than offering the album. */
function chooseFromCamera(this: IndexPage): Promise<void> {
  return runMediaSelection(this, { mediaTypes: ['mix'], sourceTypes: ['camera'] });
}

/**
 * Reports how the gate answers for the subscription capability.
 *
 * This is a smaller question than the card's other one: the request needs no
 * permission the SDK could establish, so the API answer is the whole answer.
 */
function checkSubscriptionCapability(this: IndexPage): void {
  try {
    const support: MiniAppSdk.CapabilitySupportResult =
      MiniAppSdk.capabilitySupport('wechat.request-subscribe-message');
    console.log(
      '[kmp-miniapp-sdk] subscription capability: PASS wechat.request-subscribe-message=' +
        support.state,
    );
    this.setData({
      subscriptionStatus: support.state,
      subscriptionDetails: 'wechat.request-subscribe-message=' + support.state,
    });
  } catch (error) {
    console.error('[kmp-miniapp-sdk] subscription capability: FAIL', error);
    this.setData({ subscriptionStatus: 'FAIL', subscriptionDetails: String(error) });
  }
}

/**
 * Asks the host to put the configured templates in front of the user.
 *
 * **This runs only from this button.** WeChat requires a subscription request to
 * follow a user gesture, so nothing here calls it while the page loads, and the SDK
 * does not either.
 *
 * The summary counts answers rather than listing them, and never prints a template id:
 * which templates a user subscribed to is between the user and the mini program, and a
 * screenshot of this page must not carry it. `accepted` counts the answers this SDK can
 * name; `otherStatuses` counts answers the host gave that this SDK has no evidence to
 * name, which the console does not spell out because the vocabulary could not be
 * verified offline. A missing, extra, blank, or non-text answer fails validation
 * instead of being reported as a successful request.
 *
 * Acceptance is a subscription state. It does not mean a message was sent, will be
 * sent, or was delivered.
 */
async function requestSubscription(this: IndexPage): Promise<void> {
  if (subscriptionTemplateIds.length === 0) {
    // No host call, and no prompt: without a configured template there is nothing to
    // ask about, and guessing one would be worse than saying so.
    console.log('[kmp-miniapp-sdk] subscription request: NOT CONFIGURED templateCount=0');
    this.setData({
      subscriptionStatus: 'NOT CONFIGURED',
      subscriptionDetails: 'no test template ids configured locally',
    });
    return;
  }

  try {
    const results: MiniAppSdk.SubscriptionResult[] =
      await MiniAppSdk.wechatRequestSubscribeMessage(subscriptionTemplateIds);

    const accepted = results.filter((entry) => entry.status === 'accept').length;
    const otherStatuses = results.filter(
      (entry) => entry.status === null,
    ).length;
    const signals = Array.from(
      new Set(results.map((entry) => subscriptionStatusSignal(entry.hostStatus))),
    ).sort().join('|');
    const details =
      `accepted=${accepted}, otherStatuses=${otherStatuses}, signals=${signals}`;
    console.log('[kmp-miniapp-sdk] subscription request: PASS', details);
    this.setData({ subscriptionStatus: 'PASS', subscriptionDetails: details });
  } catch (error) {
    const reason = failureReason(error);
    const signal = subscriptionFailureSignal(error);
    const details = `reason=${reason}, signal=${signal}`;
    console.error('[kmp-miniapp-sdk] subscription request: FAIL ' + details);
    this.setData({ subscriptionStatus: 'FAIL', subscriptionDetails: details });
  }
}

/**
 * Reports how the gate answers for each network API.
 *
 * The five APIs are gated one at a time, so a host can support some of them; this
 * reports each answer rather than collapsing them into one.
 */
function checkNetworkCapabilities(this: IndexPage): void {
  const query = MiniAppSdk.capabilitySupport('network-status-query').state;
  const listener = MiniAppSdk.capabilitySupport('network-status-listener').state;
  const upload = MiniAppSdk.capabilitySupport('wechat.upload-file').state;
  const download = MiniAppSdk.capabilitySupport('wechat.download-file').state;
  const details = `query=${query}, listener=${listener}, upload=${upload}, download=${download}`;
  console.log('[kmp-miniapp-sdk] network capabilities: PASS', details);
  this.setData({ networkCapabilityStatus: 'PASS', networkCapabilityDetails: details });
}

/**
 * Reads the host's current network state.
 *
 * This is a query, not an observation: it registers nothing and leaves nothing behind.
 */
async function getNetworkType(this: IndexPage): Promise<void> {
  try {
    const state: MiniAppSdk.NetworkState = await MiniAppSdk.networkStatus();
    // The host's own word is safe metadata, and it is what a caller needs when this SDK
    // has no name for the connection kind.
    const details = `connected=${state.isConnected}, type=${state.networkType ?? 'unrecognized'}`;
    console.log('[kmp-miniapp-sdk] network type: PASS', details);
    this.setData({ networkTypeStatus: 'PASS', networkTypeDetails: details });
  } catch (error) {
    const reason = failureReason(error);
    console.error('[kmp-miniapp-sdk] network type: FAIL reason=' + reason, error);
    this.setData({ networkTypeStatus: 'FAIL', networkTypeDetails: 'reason=' + reason });
  }
}

/** Begins observing network changes, which registers one host listener. */
function startNetworkObservation(this: IndexPage): void {
  MiniAppSdk.startNetworkStatusObservation();
  console.log('[kmp-miniapp-sdk] network observation: STARTED');
  this.setData({
    observationStatus: 'OBSERVING',
    observationDetails: 'switch Wi-Fi, cellular, or connectivity and then stop',
  });
}

/**
 * Stops observing and reports what the session saw.
 *
 * Only a count and the last connection kind are printed: a change event is about the
 * device, not about the user, but the page reports what it needs and no more.
 */
async function stopNetworkObservation(this: IndexPage): Promise<void> {
  try {
    const events: MiniAppSdk.NetworkState[] = await MiniAppSdk.stopNetworkStatusObservation();
    const failure = MiniAppSdk.networkStatusObservationFailure();
    const last = events.length > 0 ? events[events.length - 1] : null;
    const details =
      `events=${events.length}, last=${last === null ? 'none' : last.networkType ?? 'unrecognized'}`;

    if (failure !== null && failure !== undefined) {
      console.error('[kmp-miniapp-sdk] network observation: FAIL reason=' + failure);
      this.setData({ observationStatus: 'FAIL', observationDetails: 'reason=' + failure });
      return;
    }

    console.log('[kmp-miniapp-sdk] network observation: PASS', details);
    this.setData({
      observationStatus: events.length > 0 ? 'PASS' : 'READY',
      observationDetails: details,
    });
  } catch (error) {
    const reason = failureReason(error);
    console.error('[kmp-miniapp-sdk] network observation: FAIL reason=' + reason, error);
    this.setData({ observationStatus: 'FAIL', observationDetails: 'reason=' + reason });
  }
}

/**
 * Uploads a fixed test file to the configured endpoint.
 *
 * Without a configured endpoint nothing is sent and the card says so. The check writes
 * its own benign file first, and never prints the path, the headers, or the response
 * body: only the status, the response length, and whether the host reported progress.
 */
async function runUploadCheck(this: IndexPage): Promise<void> {
  if (uploadTestUrl.length === 0) {
    console.log('[kmp-miniapp-sdk] upload check: NOT CONFIGURED');
    this.setData({
      uploadStatus: 'NOT CONFIGURED',
      uploadDetails: 'no upload endpoint configured locally',
    });
    return;
  }

  if (activeUpload !== null) {
    console.log('[kmp-miniapp-sdk] upload check: BUSY');
    this.setData({ uploadStatus: 'BUSY', uploadDetails: 'an upload is already in flight' });
    return;
  }

  let transfer: MiniAppSdk.UploadTransfer | null = null;
  try {
    const filePath = MiniAppSdk.wechatUserDataPath() + '/' + uploadTestFileName;
    await MiniAppSdk.wechatWriteTextFile(filePath, uploadTestContent);

    transfer = MiniAppSdk.wechatUploadFile({
      url: uploadTestUrl,
      filePath,
      name: 'file',
    });
    activeUpload = transfer;
    cancelledUpload = null;
    this.setData({ uploadStatus: 'READY', uploadDetails: 'upload in flight' });

    const result: MiniAppSdk.UploadResult = await transfer.result();
    const progress = transfer.progress();
    const details =
      `status=${result.statusCode}, bytes=${result.responseText.length}, ` +
      `progressSeen=${progress !== null}`;
    console.log('[kmp-miniapp-sdk] upload check: PASS', details);
    this.setData({ uploadStatus: 'PASS', uploadDetails: details });
  } catch (error) {
    if (transfer !== null && cancelledUpload === transfer) {
      console.log('[kmp-miniapp-sdk] upload check: CANCELLED');
      this.setData({ uploadStatus: 'CANCELLED', uploadDetails: 'consumer aborted transfer' });
      return;
    }
    const reason = failureReason(error);
    console.error('[kmp-miniapp-sdk] upload check: FAIL reason=' + reason, error);
    this.setData({ uploadStatus: 'FAIL', uploadDetails: 'reason=' + reason });
  } finally {
    if (activeUpload === transfer) activeUpload = null;
    if (cancelledUpload === transfer) cancelledUpload = null;
  }
}

/**
 * Stops an in-flight upload.
 *
 * The return value is what says whether the host operation was actually stopped: a host
 * that returned no task cannot be aborted, and this never claims otherwise.
 */
function cancelUpload(this: IndexPage): void {
  const transfer = activeUpload;
  if (transfer === null) {
    console.log('[kmp-miniapp-sdk] upload cancel: READY no upload in flight');
    this.setData({ uploadStatus: 'READY', uploadDetails: 'no upload in flight' });
    return;
  }

  const invoked = transfer.abort();
  if (invoked) cancelledUpload = transfer;
  console.log('[kmp-miniapp-sdk] upload cancel: PASS abortInvoked=' + invoked);
  this.setData({
    uploadStatus: invoked ? 'CANCELLED' : 'NOT ABORTABLE',
    uploadDetails: 'abortInvoked=' + invoked,
  });
}

/**
 * Downloads from the configured endpoint.
 *
 * The result carries the host's own file reference, which this page does not print: it
 * reports the status and whether a file was reported, and nothing else. The SDK reads
 * no content, and neither does this check.
 */
async function runDownloadCheck(this: IndexPage): Promise<void> {
  if (downloadTestUrl.length === 0) {
    console.log('[kmp-miniapp-sdk] download check: NOT CONFIGURED');
    this.setData({
      downloadStatus: 'NOT CONFIGURED',
      downloadDetails: 'no download endpoint configured locally',
    });
    return;
  }

  if (activeDownload !== null) {
    console.log('[kmp-miniapp-sdk] download check: BUSY');
    this.setData({ downloadStatus: 'BUSY', downloadDetails: 'a download is already in flight' });
    return;
  }

  let transfer: MiniAppSdk.DownloadTransfer | null = null;
  try {
    transfer = MiniAppSdk.wechatDownloadFile({ url: downloadTestUrl });
    activeDownload = transfer;
    cancelledDownload = null;
    this.setData({ downloadStatus: 'READY', downloadDetails: 'download in flight' });

    const result: MiniAppSdk.DownloadResult = await transfer.result();
    const progress = transfer.progress();
    const details =
      `status=${result.statusCode}, fileReported=${result.tempFilePath.length > 0}, ` +
      `progressSeen=${progress !== null}`;
    console.log('[kmp-miniapp-sdk] download check: PASS', details);
    this.setData({ downloadStatus: 'PASS', downloadDetails: details });
  } catch (error) {
    if (transfer !== null && cancelledDownload === transfer) {
      console.log('[kmp-miniapp-sdk] download check: CANCELLED');
      this.setData({ downloadStatus: 'CANCELLED', downloadDetails: 'consumer aborted transfer' });
      return;
    }
    const reason = failureReason(error);
    console.error('[kmp-miniapp-sdk] download check: FAIL reason=' + reason, error);
    this.setData({ downloadStatus: 'FAIL', downloadDetails: 'reason=' + reason });
  } finally {
    if (activeDownload === transfer) activeDownload = null;
    if (cancelledDownload === transfer) cancelledDownload = null;
  }
}

/** Stops an in-flight download. Semantics match {@link cancelUpload}. */
function cancelDownload(this: IndexPage): void {
  const transfer = activeDownload;
  if (transfer === null) {
    console.log('[kmp-miniapp-sdk] download cancel: READY no download in flight');
    this.setData({ downloadStatus: 'READY', downloadDetails: 'no download in flight' });
    return;
  }

  const invoked = transfer.abort();
  if (invoked) cancelledDownload = transfer;
  console.log('[kmp-miniapp-sdk] download cancel: PASS abortInvoked=' + invoked);
  this.setData({
    downloadStatus: invoked ? 'CANCELLED' : 'NOT ABORTABLE',
    downloadDetails: 'abortInvoked=' + invoked,
  });
}

/**
 * Whether the local placeholder holds anything that could be a real payment.
 *
 * Every field is required, so a partially filled placeholder is still "not configured":
 * the card never hands the host a request it cannot have meant to send.
 */
function isPaymentConfigured(request: MiniAppSdk.PaymentRequest): boolean {
  return (
    request.timeStamp.trim().length > 0 &&
    request.nonceStr.trim().length > 0 &&
    request.package.trim().length > 0 &&
    (request.signType === 'MD5' || request.signType === 'HMAC-SHA256') &&
    request.paySign.trim().length > 0
  );
}

/**
 * Reduces a payment failure to a closed label.
 *
 * The raw message is deliberately never printed. A payment failure can carry text the
 * payment backend produced, and a screenshot of this page must not carry anything from a
 * payment interaction, so the page reports only which kind of problem it saw.
 */
function paymentFailureReason(error: unknown): string {
  if (typeof error !== 'object' || error === null) {
    return 'unknown';
  }
  switch ((error as { name?: unknown }).name) {
    case 'UnsupportedCapability':
      return 'unsupported';
    case 'InvalidResponse':
      return 'invalid-response';
    case 'HostFailure':
      return 'host-failure';
    default:
      return 'other';
  }
}

/**
 * Reports how the gate answers for the payment API.
 *
 * This is the API answer alone. Whether a merchant is configured, an order exists, or the
 * parameters the backend produced are acceptable are all answers the host gives when it
 * is called, so none of them belongs in this card's capability answer.
 */
function checkPaymentCapability(this: IndexPage): void {
  try {
    const support: MiniAppSdk.CapabilitySupportResult =
      MiniAppSdk.capabilitySupport('wechat.request-payment');
    console.log(
      '[kmp-miniapp-sdk] payment capability: PASS wechat.request-payment=' + support.state,
    );
    this.setData({
      paymentStatus: support.state,
      paymentDetails: 'wechat.request-payment=' + support.state,
    });
  } catch (error) {
    console.error('[kmp-miniapp-sdk] payment capability: FAIL', error);
    this.setData({ paymentStatus: 'FAIL', paymentDetails: String(error) });
  }
}

/**
 * Runs the payment interaction for whatever the local placeholder holds.
 *
 * **This runs only from this button.** WeChat requires a payment to follow a user
 * gesture, and nothing here — or in the SDK — calls it while the page loads.
 *
 * With the committed placeholder empty this reports NOT CONFIGURED and never reaches the
 * host, because there is no legal way to produce payment parameters in this repository:
 * they come from a trusted backend and a real merchant account. A PASS below means the
 * host reported the interaction completed. It does not mean an order was paid, and the
 * card says so, because the answer to that is the consumer backend's.
 */
async function requestPayment(this: IndexPage): Promise<void> {
  if (!isPaymentConfigured(paymentRequest)) {
    console.log('[kmp-miniapp-sdk] payment request: NOT CONFIGURED');
    this.setData({
      paymentStatus: 'NOT CONFIGURED',
      paymentDetails: 'no backend payment parameters configured locally',
    });
    return;
  }

  try {
    const outcome: MiniAppSdk.PaymentOutcome =
      await MiniAppSdk.wechatRequestPayment(paymentRequest);
    const details = `interactionCompleted=${outcome.interactionCompleted}`;
    console.log('[kmp-miniapp-sdk] payment request: PASS', details);
    this.setData({
      paymentStatus: 'PASS',
      paymentDetails: details + '; the order state belongs to your backend, not this page',
    });
  } catch (error) {
    // One host signal covers a dismissal and any other way the interaction ended, and the
    // SDK does not attribute it to the user. The label below matches that limit.
    if (isHostInteractionInterrupted(error)) {
      console.log('[kmp-miniapp-sdk] payment request: CANCELLED cause=indeterminate');
      this.setData({
        paymentStatus: 'CANCELLED',
        paymentDetails: 'the payment interaction ended without completing',
      });
      return;
    }

    const reason = paymentFailureReason(error);
    console.error('[kmp-miniapp-sdk] payment request: FAIL reason=' + reason);
    this.setData({ paymentStatus: 'FAIL', paymentDetails: 'reason=' + reason });
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
  // A page must not leave host work or listeners behind when navigation removes it.
  // Stopping observation begins listener cleanup immediately; the Promise is not
  // awaited because WeChat lifecycle hooks are synchronous.
  void MiniAppSdk.stopNetworkStatusObservation();
  activeUpload?.abort();
  activeDownload?.abort();
  activeUpload = null;
  activeDownload = null;
  cancelledUpload = null;
  cancelledDownload = null;
  MiniAppSdk.wechatPageOnUnload(this.route);
}

/**
 * Closed classification of an SDK failure, for logging and for the card.
 *
 * Only the SDK's own failure names are reported; the raw host message the SDK
 * carries inside a HostFailure is deliberately not read, so nothing WeChat said
 * reaches the Console or the page.
 */
const sdkFailureNames: string[] = [
  'HostFailure',
  'HostInteractionInterrupted',
  'Timeout',
  'InvalidResponse',
  'UnsupportedCapability',
  'PermissionDenied',
  'PrivacyAuthorizationRequired',
  'UserCancelled',
  'InternalFailure',
];

function classifyFailure(error: unknown): string {
  const name: unknown = (error as { name?: unknown }).name;
  const simple = typeof name === 'string' ? name.slice(name.lastIndexOf('.') + 1) : '';
  return sdkFailureNames.indexOf(simple) >= 0 ? simple : 'Failure';
}

async function switchToTabTarget(this: IndexPage): Promise<void> {
  try {
    await MiniAppSdk.wechatSwitchTab(tabTargetRoute);
    const details = `wx.switchTab ${tabTargetRoute}`;
    console.log('[kmp-miniapp-sdk] switch tab: PASS', details);
    this.setData({ switchTabStatus: 'PASS', switchTabDetails: details });
  } catch (error) {
    // The page stays on the Main tab, which is itself the evidence that the switch
    // did not happen.
    const category = classifyFailure(error);
    console.error('[kmp-miniapp-sdk] switch tab: FAIL', category);
    this.setData({ switchTabStatus: 'FAIL', switchTabDetails: category });
  }
}

async function switchToNonTabPage(this: IndexPage): Promise<void> {
  try {
    await MiniAppSdk.wechatSwitchTab(nonTabRoute);
    // WeChat is expected to refuse this, so a success here would mean the SDK
    // reported a host failure as a success.
    const details = `unexpectedly switched to ${nonTabRoute}`;
    console.error('[kmp-miniapp-sdk] switch tab (non-tab route): FAIL', details);
    this.setData({ switchTabStatus: 'FAIL', switchTabDetails: details });
  } catch (error) {
    const category = classifyFailure(error);
    console.log('[kmp-miniapp-sdk] switch tab (non-tab route): REFUSED', category);
    this.setData({
      switchTabStatus: 'FAIL',
      switchTabDetails: `expected refusal: ${category} for ${nonTabRoute}`,
    });
  }
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
    switchTabStatus: 'READY',
    switchTabDetails: `Tap the button to switch to ${tabTargetRoute}; nothing navigates on load.`,
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
    locationPermission: 'NOT RUN',
    locationPrivacy: 'NOT RUN',
    locationStatus: 'NOT RUN',
    locationDetails: 'Tap a button; nothing requests a position on load.',
    scannerStatus: 'NOT RUN',
    scannerDetails: 'Tap a button; nothing opens the scanning interface on load.',
    mediaStatus: 'NOT RUN',
    mediaDetails: 'Tap a button; nothing opens the picker on load.',
    subscriptionStatus: 'NOT RUN',
    subscriptionDetails: 'Tap a button; nothing asks the host on load.',
    networkCapabilityStatus: 'NOT RUN',
    networkCapabilityDetails: 'Tap a button; nothing is queried or registered on load.',
    networkTypeStatus: 'NOT RUN',
    networkTypeDetails: 'Tap a button; the query registers nothing.',
    observationStatus: 'NOT RUN',
    observationDetails: 'Switch networks by hand after starting observation.',
    uploadStatus: 'NOT RUN',
    uploadDetails: 'Tap a button; nothing is uploaded on load.',
    downloadStatus: 'NOT RUN',
    downloadDetails: 'Tap a button; nothing is downloaded on load.',
    paymentStatus: 'NOT CONFIGURED',
    paymentDetails: 'no backend payment parameters configured locally',
    permissionStatus: 'UNKNOWN',
    permissionDetails:
      'Permission is never requested on load. Tap a button to query or request it.',
  },
  onLoad,
  onShow,
  onHide,
  onUnload,
  openSecondPage,
  switchToTabTarget,
  switchToNonTabPage,
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
  checkLocationCapability,
  refreshLocationPermission,
  requestLocationPermission,
  refreshLocationPrivacy,
  requestLocationPrivacy,
  getCurrentLocation,
  checkScannerCapability,
  scanCode,
  scanCodeFromCamera,
  checkMediaCapability,
  chooseImage,
  chooseVideo,
  chooseImageOrVideo,
  chooseFromCamera,
  checkSubscriptionCapability,
  requestSubscription,
  checkNetworkCapabilities,
  getNetworkType,
  startNetworkObservation,
  stopNetworkObservation,
  runUploadCheck,
  cancelUpload,
  runDownloadCheck,
  cancelDownload,
  checkPaymentCapability,
  requestPayment,
});
