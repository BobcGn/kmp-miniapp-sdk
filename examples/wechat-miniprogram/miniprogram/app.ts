import MiniAppSdk = require('./libs/kmp-miniapp-sdk');

// WeChat reports app lifecycle only to this registration, so the SDK cannot
// observe it on its own. Forwarding the hooks is what lets shared Kotlin code
// know whether the mini program is in front of the user. WeChat calls onLaunch
// and then onShow, so both hooks report the foreground state.
App({
  onLaunch() {
    MiniAppSdk.wechatAppOnLaunch();
  },
  onShow() {
    MiniAppSdk.wechatAppOnShow();
  },
  onHide() {
    MiniAppSdk.wechatAppOnHide();
  },
});
