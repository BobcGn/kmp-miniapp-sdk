import MiniAppSdk = require('../../libs/kmp-miniapp-sdk');

const mainTabRoute = '/pages/index/index';

interface TabTargetPageData {
  lifecycleState: string;
  pageRoute: string;
  cardDetails: string;
}

type TabTargetPage = MiniProgramPageInstance<TabTargetPageData>;

function showLifecycle(page: TabTargetPage): void {
  page.setData({
    lifecycleState: MiniAppSdk.wechatAppLifecycleState(),
    pageRoute: MiniAppSdk.wechatPageRoute() || '(none)',
  });
}

function onShow(this: TabTargetPage): void {
  MiniAppSdk.wechatPageOnShow(this.route);
  showLifecycle(this);
  // Switching tabs hides the page it leaves and shows this one; it does not unload
  // either. Seeing SHOWN here without a preceding UNLOADED on the other tab page is
  // the host's own tab semantics, not an SDK decision.
  console.log('[kmp-miniapp-sdk] tab target page: SHOWN', this.route);
}

function onHide(this: TabTargetPage): void {
  MiniAppSdk.wechatPageOnHide();
  console.log('[kmp-miniapp-sdk] tab target page: HIDDEN', this.route);
}

function onUnload(this: TabTargetPage): void {
  MiniAppSdk.wechatPageOnUnload(this.route);
  console.log('[kmp-miniapp-sdk] tab target page: UNLOADED', this.route);
}

Page<TabTargetPageData>({
  data: {
    lifecycleState: 'UNKNOWN',
    pageRoute: '(none)',
    cardDetails: `Switch back with the tab bar; the Main tab is ${mainTabRoute}.`,
  },
  onShow,
  onHide,
  onUnload,
});
