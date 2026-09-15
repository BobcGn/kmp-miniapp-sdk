'use strict';

const kotlinModule = require('./kmp-miniapp-sdk-kotlin.js');
const miniAppExports = kotlinModule.io.github.bobcgn.miniapp.export.MiniAppExports;

// Kotlin/JS collections cannot cross the @JsExport boundary because a JavaScript
// caller has no way to construct one, so the SDK carries header maps as
// alternating name and value entries. These two helpers convert representation
// only. They contain no SDK behavior, host access, or error mapping.
function flattenHeaders(headers) {
  const flat = [];
  if (headers) {
    for (const name of Object.keys(headers)) {
      flat.push(name, headers[name]);
    }
  }
  return flat;
}

function expandHeaders(flat) {
  const headers = {};
  for (let index = 0; index + 1 < flat.length; index += 2) {
    headers[flat[index]] = flat[index + 1];
  }
  return headers;
}

module.exports = {
  sdkVersion: function sdkVersion() {
    return miniAppExports.sdkVersion();
  },
  storageGet: function storageGet(key) {
    return miniAppExports.storageGet(key);
  },
  storageSet: function storageSet(key, value) {
    return miniAppExports.storageSet(key, value);
  },
  storageRemove: function storageRemove(key) {
    return miniAppExports.storageRemove(key);
  },
  wechatLogin: function wechatLogin() {
    return miniAppExports.wechatLogin();
  },
  networkRequest: function networkRequest(url, init) {
    const options = init || {};
    return miniAppExports
      .networkRequest(
        url,
        options.method === undefined ? 'GET' : options.method,
        flattenHeaders(options.headers),
        options.body === undefined ? null : options.body,
        options.timeoutMillis === undefined ? null : options.timeoutMillis,
      )
      .then(function (result) {
        return {
          statusCode: result.statusCode,
          headers: expandHeaders(result.headers),
          body: result.body,
        };
      });
  },
  wechatAppOnLaunch: function wechatAppOnLaunch() {
    return miniAppExports.wechatAppOnLaunch();
  },
  wechatAppOnShow: function wechatAppOnShow() {
    return miniAppExports.wechatAppOnShow();
  },
  wechatAppOnHide: function wechatAppOnHide() {
    return miniAppExports.wechatAppOnHide();
  },
  wechatAppLifecycleState: function wechatAppLifecycleState() {
    return miniAppExports.wechatAppLifecycleState();
  },
  wechatPageOnShow: function wechatPageOnShow(route) {
    return miniAppExports.wechatPageOnShow(route);
  },
  wechatPageOnHide: function wechatPageOnHide() {
    return miniAppExports.wechatPageOnHide();
  },
  wechatPageOnUnload: function wechatPageOnUnload(route) {
    return miniAppExports.wechatPageOnUnload(route);
  },
  wechatPageRoute: function wechatPageRoute() {
    return miniAppExports.wechatPageRoute();
  },
  wechatNavigateTo: function wechatNavigateTo(url) {
    return miniAppExports.wechatNavigateTo(url);
  },
  wechatRedirectTo: function wechatRedirectTo(url) {
    return miniAppExports.wechatRedirectTo(url);
  },
  wechatNavigateBack: function wechatNavigateBack(delta) {
    return miniAppExports.wechatNavigateBack(delta === undefined ? null : delta);
  },
  capabilitySupport: function capabilitySupport(capability) {
    return miniAppExports.capabilitySupport(capability);
  },
  requireCapability: function requireCapability(capability) {
    return miniAppExports.requireCapability(capability);
  },
  wechatRuntimeInfo: function wechatRuntimeInfo() {
    return miniAppExports.wechatRuntimeInfo();
  },
  wechatCanIUse: function wechatCanIUse(schema) {
    return miniAppExports.wechatCanIUse(schema);
  },
  permissionState: function permissionState(permission) {
    return miniAppExports.permissionState(permission);
  },
  requestPermission: function requestPermission(permission) {
    return miniAppExports.requestPermission(permission);
  },
  openPermissionSettings: function openPermissionSettings(permission) {
    return miniAppExports.openPermissionSettings(permission);
  },
};
