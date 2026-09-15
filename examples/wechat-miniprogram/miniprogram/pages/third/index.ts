import MiniAppSdk = require('../../libs/kmp-miniapp-sdk');

interface ThirdPageData {
  lifecycleState: string;
  pageRoute: string;
  navigationStatus: string;
  navigationDetails: string;
}

type ThirdPage = MiniProgramPageInstance<ThirdPageData>;

function showLifecycle(page: ThirdPage): void {
  page.setData({
    lifecycleState: MiniAppSdk.wechatAppLifecycleState(),
    pageRoute: MiniAppSdk.wechatPageRoute() || '(none)',
  });
}

function onShow(this: ThirdPage): void {
  MiniAppSdk.wechatPageOnShow(this.route);
  showLifecycle(this);
  console.log('[kmp-miniapp-sdk] third page: SHOWN', this.route);
}

function onHide(this: ThirdPage): void {
  MiniAppSdk.wechatPageOnHide();
}

function onUnload(this: ThirdPage): void {
  MiniAppSdk.wechatPageOnUnload(this.route);
  console.log('[kmp-miniapp-sdk] third page: UNLOADED', this.route);
}

// The second page was replaced by this one, so going back reveals the index page.
async function goBack(this: ThirdPage): Promise<void> {
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

Page<ThirdPageData>({
  data: {
    lifecycleState: 'UNKNOWN',
    pageRoute: '(none)',
    navigationStatus: 'READY',
    navigationDetails: 'Going back reveals the index page, because the second page was replaced.',
  },
  onShow,
  onHide,
  onUnload,
  goBack,
});
