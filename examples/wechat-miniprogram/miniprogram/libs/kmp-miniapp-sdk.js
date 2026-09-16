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
  wechatCheckSession: function wechatCheckSession() {
    return miniAppExports.wechatCheckSession();
  },
  wechatGetClipboardText: function wechatGetClipboardText() {
    return miniAppExports.wechatGetClipboardText();
  },
  wechatSetClipboardText: function wechatSetClipboardText(value) {
    return miniAppExports.wechatSetClipboardText(value);
  },
  wechatVibrateShort: function wechatVibrateShort() {
    return miniAppExports.wechatVibrateShort();
  },
  wechatVibrateLong: function wechatVibrateLong() {
    return miniAppExports.wechatVibrateLong();
  },
  wechatUserDataPath: function wechatUserDataPath() {
    return miniAppExports.wechatUserDataPath();
  },
  wechatReadTextFile: function wechatReadTextFile(path) {
    return miniAppExports.wechatReadTextFile(path);
  },
  wechatWriteTextFile: function wechatWriteTextFile(path, content) {
    return miniAppExports.wechatWriteTextFile(path, content);
  },
  wechatFileExists: function wechatFileExists(path) {
    return miniAppExports.wechatFileExists(path);
  },
  wechatRemoveFile: function wechatRemoveFile(path) {
    return miniAppExports.wechatRemoveFile(path);
  },
  wechatGetCurrentLocation: function wechatGetCurrentLocation(coordinateSystem) {
    return miniAppExports
      .wechatGetCurrentLocation(coordinateSystem === undefined ? 'gcj02' : coordinateSystem)
      .then(function (position) {
        return {
          latitude: position.latitude,
          longitude: position.longitude,
          accuracyMeters: position.accuracyMeters,
          coordinateSystem: position.coordinateSystem,
        };
      });
  },
  wechatScanCode: function wechatScanCode(onlyFromCamera, scanTypes) {
    return miniAppExports
      .wechatScanCode(
        onlyFromCamera === undefined ? false : onlyFromCamera,
        scanTypes === undefined ? [] : scanTypes,
      )
      .then(function (result) {
        return {
          text: result.text,
          scanType: result.scanType,
          format: result.format,
          charSet: result.charSet,
          rawData: result.rawData,
          path: result.path,
        };
      });
  },
  wechatChooseMedia: function wechatChooseMedia(request) {
    const options = request || {};
    return miniAppExports
      .wechatChooseMedia(
        options.mediaTypes === undefined ? [] : options.mediaTypes,
        options.count === undefined ? 1 : options.count,
        options.sourceTypes === undefined ? [] : options.sourceTypes,
        options.maxDurationSeconds === undefined ? null : options.maxDurationSeconds,
        options.sizeTypes === undefined ? [] : options.sizeTypes,
        options.camera === undefined ? null : options.camera,
      )
      .then(function (files) {
        return files.map(function (file) {
          return {
            tempFilePath: file.tempFilePath,
            sizeBytes: file.sizeBytes,
            fileType: file.fileType,
            hostFileType: file.hostFileType,
            durationSeconds: file.durationSeconds,
            width: file.width,
            height: file.height,
            thumbTempFilePath: file.thumbTempFilePath,
          };
        });
      });
  },
  wechatRequestSubscribeMessage: function wechatRequestSubscribeMessage(templateIds) {
    return miniAppExports
      .wechatRequestSubscribeMessage(templateIds === undefined ? [] : templateIds)
      .then(function (results) {
        return results.map(function (result) {
          return {
            templateId: result.templateId,
            status: result.status,
            hostStatus: result.hostStatus,
          };
        });
      });
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
  privacyStatus: function privacyStatus() {
    return miniAppExports.privacyStatus();
  },
  requestPrivacyAuthorization: function requestPrivacyAuthorization() {
    return miniAppExports.requestPrivacyAuthorization();
  },
  requirePrivacySatisfied: function requirePrivacySatisfied() {
    return miniAppExports.requirePrivacySatisfied();
  },
};
