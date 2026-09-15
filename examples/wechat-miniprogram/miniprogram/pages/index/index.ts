import MiniAppSdk = require('../../libs/kmp-miniapp-sdk');

const sdkVersion: string = MiniAppSdk.sdkVersion();
const storageKey = 'kmp-miniapp-sdk.storage-smoke';
// Any HTTPS endpoint works here. WeChat Developer Tools must either list this
// host in the request domain whitelist or run with domain checking disabled.
const networkUrl = 'https://example.com/';
const secondPageRoute = '/pages/second/index';

interface IndexPageData {
  sdkVersion: string;
  lifecycleState: string;
  pageRoute: string;
  storageStatus: string;
  storageDetails: string;
  authStatus: string;
  authDetails: string;
  networkStatus: string;
  networkDetails: string;
  navigationStatus: string;
  navigationDetails: string;
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
    storageStatus: 'RUNNING',
    storageDetails: 'Checking set, get, overwrite, remove, and missing key…',
    authStatus: 'RUNNING',
    authDetails: 'Requesting a short-lived WeChat login code…',
    networkStatus: 'RUNNING',
    networkDetails: 'Requesting an HTTPS endpoint through wx.request…',
    navigationStatus: 'READY',
    navigationDetails: 'Tap the button to open the second page.',
  },
  onLoad,
  onShow,
  onHide,
  onUnload,
  openSecondPage,
});
