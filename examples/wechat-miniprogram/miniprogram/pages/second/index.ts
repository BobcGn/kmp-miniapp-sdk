import MiniAppSdk = require('../../libs/kmp-miniapp-sdk');

const thirdPageRoute = '/pages/third/index';

interface SecondPageData {
  lifecycleState: string;
  pageRoute: string;
  navigationStatus: string;
  navigationDetails: string;
}

type SecondPage = MiniProgramPageInstance<SecondPageData>;

function showLifecycle(page: SecondPage): void {
  page.setData({
    lifecycleState: MiniAppSdk.wechatAppLifecycleState(),
    pageRoute: MiniAppSdk.wechatPageRoute() || '(none)',
  });
}

function onShow(this: SecondPage): void {
  MiniAppSdk.wechatPageOnShow(this.route);
  showLifecycle(this);
  console.log('[kmp-miniapp-sdk] second page: SHOWN', this.route);
}

function onHide(this: SecondPage): void {
  MiniAppSdk.wechatPageOnHide();
}

function onUnload(this: SecondPage): void {
  MiniAppSdk.wechatPageOnUnload(this.route);
  console.log('[kmp-miniapp-sdk] second page: UNLOADED', this.route);
}

async function goBack(this: SecondPage): Promise<void> {
  try {
    await MiniAppSdk.wechatNavigateBack();
    const details = 'wx.navigateBack';
    console.log('[kmp-miniapp-sdk] navigation: PASS', details);
    this.setData({ navigationStatus: 'PASS', navigationDetails: details });
  } catch (error) {
    const details = String(error);
    console.error('[kmp-miniapp-sdk] navigation: FAIL', error);
    this.setData({ navigationStatus: 'FAIL', navigationDetails: details });
  }
}

async function replaceWithThird(this: SecondPage): Promise<void> {
  try {
    await MiniAppSdk.wechatRedirectTo(thirdPageRoute);
    const details = `wx.redirectTo ${thirdPageRoute}`;
    console.log('[kmp-miniapp-sdk] navigation: PASS', details);
    this.setData({ navigationStatus: 'PASS', navigationDetails: details });
  } catch (error) {
    const details = String(error);
    console.error('[kmp-miniapp-sdk] navigation: FAIL', error);
    this.setData({ navigationStatus: 'FAIL', navigationDetails: details });
  }
}

Page<SecondPageData>({
  data: {
    lifecycleState: 'UNKNOWN',
    pageRoute: '(none)',
    navigationStatus: 'READY',
    navigationDetails: 'Go back returns to the index page; replacing shows the third page.',
  },
  onShow,
  onHide,
  onUnload,
  goBack,
  replaceWithThird,
});
