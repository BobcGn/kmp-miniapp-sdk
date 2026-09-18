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


// The network status and transfer exports hand back objects, and a JavaScript caller
// should see the documented plain shape rather than a Kotlin instance. These helpers
// convert representation only: they contain no SDK behavior, host access, or error
// mapping.
function toNetworkState(state) {
  return {
    isConnected: state.isConnected,
    networkType: state.networkType,
    hostNetworkType: state.hostNetworkType,
  };
}

function toTransferProgress(progress) {
  return {
    percent: progress.percent,
    bytesTransferred: progress.bytesTransferred,
    bytesExpected: progress.bytesExpected,
  };
}

function wrapTransfer(transfer, toResult) {
  const handle = {
    abort: function abort() {
      return transfer.abort();
    },
    progress: function progress() {
      const current = transfer.progress();
      return current === null || current === undefined ? null : toTransferProgress(current);
    },
    result: function result() {
      return transfer.result().then(toResult);
    },
  };
  // A getter rather than a snapshot, because whether a transfer can still be aborted
  // changes as it runs.
  Object.defineProperty(handle, 'abortable', {
    get: function abortable() {
      return transfer.abortable;
    },
    enumerable: true,
  });
  return handle;
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
  networkStatus: function networkStatus() {
    return miniAppExports.networkStatus().then(toNetworkState);
  },
  startNetworkStatusObservation: function startNetworkStatusObservation() {
    return miniAppExports.startNetworkStatusObservation();
  },
  stopNetworkStatusObservation: function stopNetworkStatusObservation() {
    return miniAppExports.stopNetworkStatusObservation().then(function (states) {
      return states.map(toNetworkState);
    });
  },
  networkStatusObservationFailure: function networkStatusObservationFailure() {
    return miniAppExports.networkStatusObservationFailure();
  },
  wechatUploadFile: function wechatUploadFile(request) {
    const options = request || {};
    const transfer = miniAppExports.wechatUploadFile(
      options.url === undefined ? '' : options.url,
      options.filePath === undefined ? '' : options.filePath,
      options.name === undefined ? 'file' : options.name,
      flattenHeaders(options.headers),
      flattenHeaders(options.formData),
      options.timeoutMillis === undefined ? null : options.timeoutMillis,
    );
    return wrapTransfer(transfer, function (result) {
      return { statusCode: result.statusCode, responseText: result.responseText };
    });
  },
  wechatDownloadFile: function wechatDownloadFile(request) {
    const options = request || {};
    const transfer = miniAppExports.wechatDownloadFile(
      options.url === undefined ? '' : options.url,
      flattenHeaders(options.headers),
      options.timeoutMillis === undefined ? null : options.timeoutMillis,
      options.filePath === undefined ? null : options.filePath,
    );
    return wrapTransfer(transfer, function (result) {
      return {
        statusCode: result.statusCode,
        tempFilePath: result.tempFilePath,
        filePath: result.filePath,
      };
    });
  },
  wechatRequestPayment: function wechatRequestPayment(request) {
    const options = request || {};
    return miniAppExports
      .wechatRequestPayment(
        options.timeStamp === undefined ? '' : options.timeStamp,
        options.nonceStr === undefined ? '' : options.nonceStr,
        options.package === undefined ? '' : options.package,
        options.signType === undefined ? '' : options.signType,
        options.paySign === undefined ? '' : options.paySign,
      )
      .then(function (outcome) {
        // Only the one field the SDK names crosses this boundary, and no payment parameter
        // is echoed back. A caller cannot read a resolved promise here as an order fact,
        // because there is no order fact in it to read.
        return { interactionCompleted: outcome.interactionCompleted };
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
  wechatSwitchTab: function wechatSwitchTab(url) {
    return miniAppExports.wechatSwitchTab(url);
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
