(function (_, kotlin_kotlin, kotlin_org_jetbrains_kotlinx_kotlinx_coroutines_core) {
  'use strict';
  //region block: imports
  var imul = Math.imul;
  var protoOf = kotlin_kotlin.$_$.k3;
  var initMetadataForInterface = kotlin_kotlin.$_$.f3;
  var Unit_instance = kotlin_kotlin.$_$.j;
  var Companion_instance = kotlin_kotlin.$_$.i;
  var _Result___init__impl__xyqfz8 = kotlin_kotlin.$_$.c;
  var createFailure = kotlin_kotlin.$_$.b4;
  var CoroutineImpl = kotlin_kotlin.$_$.j2;
  var intercepted = kotlin_kotlin.$_$.v1;
  var CancellableContinuationImpl = kotlin_org_jetbrains_kotlinx_kotlinx_coroutines_core.$_$.c;
  var returnIfSuspended = kotlin_kotlin.$_$.l;
  var get_COROUTINE_SUSPENDED = kotlin_kotlin.$_$.t1;
  var initMetadataForCoroutine = kotlin_kotlin.$_$.e3;
  var getStringHashCode = kotlin_kotlin.$_$.a3;
  var initMetadataForClass = kotlin_kotlin.$_$.c3;
  var initMetadataForObject = kotlin_kotlin.$_$.h3;
  var toString = kotlin_kotlin.$_$.g4;
  var equals = kotlin_kotlin.$_$.x2;
  var Enum = kotlin_kotlin.$_$.t3;
  var VOID = kotlin_kotlin.$_$.a;
  var initMetadataForCompanion = kotlin_kotlin.$_$.d3;
  var enumEntries = kotlin_kotlin.$_$.l2;
  var emptyMap = kotlin_kotlin.$_$.k1;
  var captureStack = kotlin_kotlin.$_$.s2;
  var Exception = kotlin_kotlin.$_$.v3;
  var Exception_init_$Init$ = kotlin_kotlin.$_$.w;
  var THROW_CCE = kotlin_kotlin.$_$.y3;
  var isCharSequence = kotlin_kotlin.$_$.i3;
  var trim = kotlin_kotlin.$_$.s3;
  var toString_0 = kotlin_kotlin.$_$.l3;
  var _Char___init__impl__6a9atx = kotlin_kotlin.$_$.b;
  var charArrayOf = kotlin_kotlin.$_$.t2;
  var split = kotlin_kotlin.$_$.q3;
  var ArrayList_init_$Create$ = kotlin_kotlin.$_$.n;
  var toIntOrNull = kotlin_kotlin.$_$.r3;
  var last = kotlin_kotlin.$_$.m1;
  var get_lastIndex = kotlin_kotlin.$_$.l1;
  var compareTo = kotlin_kotlin.$_$.u2;
  var hashCode = kotlin_kotlin.$_$.b3;
  var await_0 = kotlin_kotlin.$_$.k;
  var noWhenBranchMatchedException = kotlin_kotlin.$_$.e4;
  var ArrayList_init_$Create$_0 = kotlin_kotlin.$_$.o;
  var listOf = kotlin_kotlin.$_$.o1;
  var addAll = kotlin_kotlin.$_$.h1;
  var copyToArray = kotlin_kotlin.$_$.i1;
  var promisify = kotlin_kotlin.$_$.w1;
  var IllegalArgumentException_init_$Create$ = kotlin_kotlin.$_$.x;
  var LinkedHashMap_init_$Create$ = kotlin_kotlin.$_$.q;
  var defineProp = kotlin_kotlin.$_$.w2;
  var Companion_instance_0 = kotlin_kotlin.$_$.h;
  var isBlank = kotlin_kotlin.$_$.p3;
  var to = kotlin_kotlin.$_$.h4;
  var mapOf = kotlin_kotlin.$_$.p1;
  var contains = kotlin_kotlin.$_$.o3;
  var constructCallableReference = kotlin_kotlin.$_$.v2;
  var FunctionAdapter = kotlin_kotlin.$_$.q2;
  var isInterface = kotlin_kotlin.$_$.j3;
  var CoroutineScope = kotlin_org_jetbrains_kotlinx_kotlinx_coroutines_core.$_$.e;
  var initMetadataForLambda = kotlin_kotlin.$_$.g3;
  var SupervisorJob = kotlin_org_jetbrains_kotlinx_kotlinx_coroutines_core.$_$.f;
  var Dispatchers_getInstance = kotlin_org_jetbrains_kotlinx_kotlinx_coroutines_core.$_$.a;
  var CoroutineScope_0 = kotlin_org_jetbrains_kotlinx_kotlinx_coroutines_core.$_$.d;
  var async = kotlin_org_jetbrains_kotlinx_kotlinx_coroutines_core.$_$.g;
  var getBooleanHashCode = kotlin_kotlin.$_$.y2;
  var MutableStateFlow = kotlin_org_jetbrains_kotlinx_kotlinx_coroutines_core.$_$.b;
  var emptyList = kotlin_kotlin.$_$.j1;
  var listOf_0 = kotlin_kotlin.$_$.n1;
  var mapOf_0 = kotlin_kotlin.$_$.q1;
  var Collection = kotlin_kotlin.$_$.f1;
  var KProperty1 = kotlin_kotlin.$_$.n3;
  var getPropertyCallableRef = kotlin_kotlin.$_$.z2;
  var lazy = kotlin_kotlin.$_$.d4;
  //endregion
  //region block: pre-declaration
  initMetadataForInterface(HostOperationAborter, 'HostOperationAborter');
  initMetadataForCoroutine($awaitHostCallbackCOROUTINE$, CoroutineImpl);
  initMetadataForClass(CapabilityKey, 'CapabilityKey');
  initMetadataForObject(Supported, 'Supported');
  initMetadataForObject(Unsupported, 'Unsupported');
  initMetadataForClass(VersionDependent, 'VersionDependent');
  initMetadataForClass(PermissionDependent, 'PermissionDependent');
  initMetadataForClass(MiniAppLifecycleState, 'MiniAppLifecycleState', VOID, Enum);
  initMetadataForCompanion(Companion);
  initMetadataForClass(HttpMethod, 'HttpMethod', VOID, Enum);
  initMetadataForClass(MiniAppHttpRequest, 'MiniAppHttpRequest');
  initMetadataForClass(MiniAppHttpResponse, 'MiniAppHttpResponse');
  initMetadataForCompanion(Companion_0);
  initMetadataForObject(NotRequested, 'NotRequested');
  initMetadataForObject(Granted, 'Granted');
  initMetadataForObject(Denied, 'Denied');
  initMetadataForCompanion(Companion_1);
  initMetadataForCompanion(Companion_2);
  initMetadataForClass(PermissionKey, 'PermissionKey');
  initMetadataForClass(PrivacyAuthorizationRequirement, 'PrivacyAuthorizationRequirement', VOID, Enum);
  initMetadataForClass(PrivacyStatus, 'PrivacyStatus');
  initMetadataForObject(Authorized, 'Authorized');
  initMetadataForObject(Refused, 'Refused');
  initMetadataForCompanion(Companion_3);
  initMetadataForCompanion(Companion_4);
  initMetadataForClass(MiniAppException, 'MiniAppException', VOID, Exception);
  initMetadataForClass(UnsupportedCapability, 'UnsupportedCapability', VOID, MiniAppException);
  initMetadataForClass(PermissionDenied, 'PermissionDenied', VOID, MiniAppException);
  initMetadataForClass(Timeout, 'Timeout', VOID, MiniAppException);
  initMetadataForClass(HostFailure, 'HostFailure', VOID, MiniAppException);
  initMetadataForClass(InvalidResponse, 'InvalidResponse', VOID, MiniAppException);
  initMetadataForClass(PrivacyAuthorizationRequired, 'PrivacyAuthorizationRequired', PrivacyAuthorizationRequired, MiniAppException);
  initMetadataForClass(InternalFailure, 'InternalFailure', VOID, MiniAppException);
  initMetadataForCompanion(Companion_5);
  initMetadataForClass(HostVersion, 'HostVersion');
  initMetadataForClass(JsCapabilitySupport, 'JsCapabilitySupport');
  initMetadataForClass(JsPrivacyStatus, 'JsPrivacyStatus');
  initMetadataForClass(JsRuntimeInfo, 'JsRuntimeInfo');
  initMetadataForCoroutine($storageGet$suspendBridgeCOROUTINE$, CoroutineImpl);
  initMetadataForCoroutine($storageSet$suspendBridgeCOROUTINE$, CoroutineImpl);
  initMetadataForCoroutine($storageRemove$suspendBridgeCOROUTINE$, CoroutineImpl);
  initMetadataForCoroutine($wechatLogin$suspendBridgeCOROUTINE$, CoroutineImpl);
  initMetadataForCoroutine($wechatCheckSessionCOROUTINE$, CoroutineImpl);
  initMetadataForCoroutine($wechatCheckSession$suspendBridgeCOROUTINE$, CoroutineImpl);
  initMetadataForCoroutine($networkRequestCOROUTINE$, CoroutineImpl);
  initMetadataForCoroutine($networkRequest$suspendBridgeCOROUTINE$, CoroutineImpl);
  initMetadataForCoroutine($wechatNavigateTo$suspendBridgeCOROUTINE$, CoroutineImpl);
  initMetadataForCoroutine($wechatRedirectTo$suspendBridgeCOROUTINE$, CoroutineImpl);
  initMetadataForCoroutine($wechatNavigateBack$suspendBridgeCOROUTINE$, CoroutineImpl);
  initMetadataForCoroutine($permissionStateCOROUTINE$, CoroutineImpl);
  initMetadataForCoroutine($permissionState$suspendBridgeCOROUTINE$, CoroutineImpl);
  initMetadataForCoroutine($requestPermissionCOROUTINE$, CoroutineImpl);
  initMetadataForCoroutine($requestPermission$suspendBridgeCOROUTINE$, CoroutineImpl);
  initMetadataForCoroutine($openPermissionSettingsCOROUTINE$, CoroutineImpl);
  initMetadataForCoroutine($openPermissionSettings$suspendBridgeCOROUTINE$, CoroutineImpl);
  initMetadataForCoroutine($privacyStatusCOROUTINE$, CoroutineImpl);
  initMetadataForCoroutine($privacyStatus$suspendBridgeCOROUTINE$, CoroutineImpl);
  initMetadataForCoroutine($requestPrivacyAuthorizationCOROUTINE$, CoroutineImpl);
  initMetadataForCoroutine($requestPrivacyAuthorization$suspendBridgeCOROUTINE$, CoroutineImpl);
  initMetadataForCoroutine($wechatGetClipboardText$suspendBridgeCOROUTINE$, CoroutineImpl);
  initMetadataForCoroutine($wechatSetClipboardText$suspendBridgeCOROUTINE$, CoroutineImpl);
  initMetadataForCoroutine($wechatVibrateShort$suspendBridgeCOROUTINE$, CoroutineImpl);
  initMetadataForCoroutine($wechatVibrateLong$suspendBridgeCOROUTINE$, CoroutineImpl);
  initMetadataForCoroutine($wechatReadTextFile$suspendBridgeCOROUTINE$, CoroutineImpl);
  initMetadataForCoroutine($wechatWriteTextFile$suspendBridgeCOROUTINE$, CoroutineImpl);
  initMetadataForCoroutine($wechatFileExists$suspendBridgeCOROUTINE$, CoroutineImpl);
  initMetadataForCoroutine($wechatRemoveFile$suspendBridgeCOROUTINE$, CoroutineImpl);
  initMetadataForCoroutine($requirePrivacySatisfied$suspendBridgeCOROUTINE$, CoroutineImpl);
  initMetadataForObject(MiniAppExports, 'MiniAppExports', VOID, VOID, VOID, [1, 2, 0, 5]);
  initMetadataForClass(MiniAppHttpResult, 'MiniAppHttpResult');
  initMetadataForObject(WeChatDeviceCapabilities, 'WeChatDeviceCapabilities');
  initMetadataForClass(WeChatLoginResult, 'WeChatLoginResult');
  initMetadataForCompanion(Companion_6);
  initMetadataForClass(WeChatSessionState, 'WeChatSessionState', VOID, Enum);
  initMetadataForClass(WechatPlatformApi, 'WechatPlatformApi');
  initMetadataForClass(WechatHost, 'WechatHost', WechatHost);
  initMetadataForClass(WechatAuth, 'WechatAuth', WechatAuth, VOID, VOID, [0]);
  initMetadataForObject(WxAuthHost, 'WxAuthHost');
  initMetadataForCoroutine($readTextCOROUTINE$, CoroutineImpl);
  initMetadataForClass(WechatClipboard, 'WechatClipboard', WechatClipboard, VOID, VOID, [0, 1]);
  initMetadataForObject(WxClipboardHost, 'WxClipboardHost');
  initMetadataForObject(Refused_0, 'Refused');
  initMetadataForClass(Failed, 'Failed');
  initMetadataForObject(NotFound, 'NotFound');
  initMetadataForClass(Failed_0, 'Failed');
  initMetadataForCoroutine($readTextCOROUTINE$_0, CoroutineImpl);
  initMetadataForCoroutine($existsCOROUTINE$, CoroutineImpl);
  initMetadataForClass(WechatFileSystem, 'WechatFileSystem', WechatFileSystem, VOID, VOID, [1, 2]);
  initMetadataForObject(WxFileSystemHost, 'WxFileSystemHost');
  initMetadataForClass(WechatHaptics, 'WechatHaptics', WechatHaptics, VOID, VOID, [0]);
  initMetadataForObject(WxHapticsHost, 'WxHapticsHost');
  initMetadataForClass(WechatNavigation, 'WechatNavigation', WechatNavigation, VOID, VOID, [1]);
  initMetadataForObject(WxNavigationHost, 'WxNavigationHost');
  initMetadataForClass(sam$io_github_bobcgn_miniapp_async_HostOperationAborter$0, 'sam$io_github_bobcgn_miniapp_async_HostOperationAborter$0', VOID, VOID, [HostOperationAborter, FunctionAdapter]);
  initMetadataForClass(WechatNetwork, 'WechatNetwork', WechatNetwork, VOID, VOID, [1]);
  initMetadataForObject(WxNetworkHost, 'WxNetworkHost');
  initMetadataForObject(WxPermissionHost, 'WxPermissionHost');
  initMetadataForObject(WechatPermissionScopes, 'WechatPermissionScopes');
  initMetadataForLambda(WechatPermissions$request$slambda, CoroutineImpl, VOID, [1]);
  initMetadataForCoroutine($openSettingsCOROUTINE$, CoroutineImpl);
  initMetadataForCoroutine($performRequestCOROUTINE$, CoroutineImpl);
  initMetadataForCoroutine($readStateCOROUTINE$, CoroutineImpl);
  initMetadataForClass(WechatPermissions, 'WechatPermissions', WechatPermissions, VOID, VOID, [1, 2]);
  initMetadataForLambda(WechatPrivacy$requestAuthorization$slambda, CoroutineImpl, VOID, [1]);
  initMetadataForCoroutine($statusCOROUTINE$, CoroutineImpl);
  initMetadataForCoroutine($requireSatisfiedCOROUTINE$, CoroutineImpl);
  initMetadataForClass(WechatPrivacy, 'WechatPrivacy', WechatPrivacy, VOID, VOID, [0]);
  initMetadataForObject(WxPrivacyHost, 'WxPrivacyHost');
  initMetadataForObject(WxRuntimeInfoHost, 'WxRuntimeInfoHost');
  initMetadataForCompanion(Companion_7);
  initMetadataForClass(WechatStorage, 'WechatStorage', WechatStorage, VOID, VOID, [1, 2]);
  initMetadataForObject(WxStorageHost, 'WxStorageHost');
  initMetadataForClass(Present, 'Present');
  initMetadataForObject(Unreadable, 'Unreadable');
  initMetadataForClass(Present_0, 'Present');
  initMetadataForObject(Unreadable_0, 'Unreadable');
  initMetadataForObject(Absent, 'Absent');
  initMetadataForClass(Decided, 'Decided');
  initMetadataForObject(Unreadable_1, 'Unreadable');
  initMetadataForClass(Required, 'Required');
  initMetadataForClass(NotRequired, 'NotRequired');
  initMetadataForObject(Unreadable_2, 'Unreadable');
  initMetadataForClass(WechatAppLifecycle, 'WechatAppLifecycle', WechatAppLifecycle);
  initMetadataForClass(WechatCapabilityRequirement, 'WechatCapabilityRequirement', WechatCapabilityRequirement);
  initMetadataForObject(WechatCapabilityCatalog, 'WechatCapabilityCatalog');
  initMetadataForClass(WechatCapabilityGate, 'WechatCapabilityGate');
  initMetadataForClass(WechatPageLifecycle, 'WechatPageLifecycle', WechatPageLifecycle);
  initMetadataForCompanion(Companion_8);
  initMetadataForClass(WechatRuntimeInfo, 'WechatRuntimeInfo');
  //endregion
  function HostOperationAborter() {
  }
  function awaitHostCallback(register, $completion) {
    var tmp = new $awaitHostCallbackCOROUTINE$(register, $completion);
    tmp.result_1 = Unit_instance;
    tmp.exception_1 = null;
    return tmp.doResume_5yljmg_k$();
  }
  function awaitHostCallback$abortOnce(aborter, abortInvoked) {
    var currentAborter = aborter._v;
    if (!abortInvoked._v && !(currentAborter == null)) {
      abortInvoked._v = true;
      currentAborter.abort_lahfmo_k$();
    }
  }
  function awaitHostCallback$lambda($terminal, $cancellationRequested, $aborter, $abortInvoked) {
    return function (it) {
      var tmp;
      if (!$terminal._v) {
        $terminal._v = true;
        $cancellationRequested._v = true;
        awaitHostCallback$abortOnce($aborter, $abortInvoked);
        tmp = Unit_instance;
      }
      return Unit_instance;
    };
  }
  function awaitHostCallback$lambda_0($terminal, $continuation) {
    return function (value) {
      var tmp;
      if ($terminal._v || !$continuation.get_isActive_quafmh_k$()) {
        return Unit_instance;
      }
      $terminal._v = true;
      // Inline function 'kotlin.coroutines.resume' call
      var this_0 = $continuation;
      // Inline function 'kotlin.Companion.success' call
      var tmp$ret$1 = _Result___init__impl__xyqfz8(value);
      this_0.resumeWith_dtxwbr_k$(tmp$ret$1);
      return Unit_instance;
    };
  }
  function awaitHostCallback$lambda_1($terminal, $continuation) {
    return function (error) {
      var tmp;
      if ($terminal._v || !$continuation.get_isActive_quafmh_k$()) {
        return Unit_instance;
      }
      $terminal._v = true;
      // Inline function 'kotlin.coroutines.resumeWithException' call
      var this_0 = $continuation;
      // Inline function 'kotlin.Companion.failure' call
      var tmp$ret$1 = _Result___init__impl__xyqfz8(createFailure(error));
      this_0.resumeWith_dtxwbr_k$(tmp$ret$1);
      return Unit_instance;
    };
  }
  function $awaitHostCallbackCOROUTINE$(register, resultContinuation) {
    CoroutineImpl.call(this, resultContinuation);
    this.register_1 = register;
  }
  protoOf($awaitHostCallbackCOROUTINE$).doResume_5yljmg_k$ = function () {
    var suspendResult = this.result_1;
    $sm: do
      try {
        var tmp = this.state_1;
        switch (tmp) {
          case 0:
            this.exceptionState_1 = 2;
            this.state_1 = 1;
            var cancellable = new CancellableContinuationImpl(intercepted(this), 1);
            cancellable.initCancellability_shqc60_k$();
            var terminal = {_v: false};
            var cancellationRequested = {_v: false};
            var aborter = {_v: null};
            var abortInvoked = {_v: false};
            cancellable.invokeOnCancellation_kffkqp_k$(awaitHostCallback$lambda(terminal, cancellationRequested, aborter, abortInvoked));
            try {
              var tmp_0 = awaitHostCallback$lambda_0(terminal, cancellable);
              aborter._v = this.register_1(tmp_0, awaitHostCallback$lambda_1(terminal, cancellable));
              if (cancellationRequested._v) {
                awaitHostCallback$abortOnce(aborter, abortInvoked);
              }
            } catch ($p) {
              if ($p instanceof Error) {
                var cause = $p;
                if (!terminal._v && cancellable.get_isActive_quafmh_k$()) {
                  terminal._v = true;
                  var exception = new InternalFailure('Host callback registration failed', cause);
                  cancellable.resumeWith_dtxwbr_k$(_Result___init__impl__xyqfz8(createFailure(exception)));
                }
              } else {
                throw $p;
              }
            }

            suspendResult = returnIfSuspended(cancellable.getResult_fck196_k$(), this);
            if (suspendResult === get_COROUTINE_SUSPENDED()) {
              return suspendResult;
            }

            continue $sm;
          case 1:
            return suspendResult;
          case 2:
            throw this.exception_1;
        }
      } catch ($p_0) {
        var e = $p_0;
        if (this.exceptionState_1 === 2) {
          throw e;
        } else {
          this.state_1 = this.exceptionState_1;
          this.exception_1 = e;
        }
      }
     while (true);
  };
  function CapabilityKey(value) {
    this.value_1 = value;
  }
  protoOf(CapabilityKey).toString = function () {
    return 'CapabilityKey(value=' + this.value_1 + ')';
  };
  protoOf(CapabilityKey).hashCode = function () {
    return getStringHashCode(this.value_1);
  };
  protoOf(CapabilityKey).equals = function (other) {
    if (this === other)
      return true;
    if (!(other instanceof CapabilityKey))
      return false;
    if (!(this.value_1 === other.value_1))
      return false;
    return true;
  };
  function Supported() {
  }
  protoOf(Supported).toString = function () {
    return 'Supported';
  };
  protoOf(Supported).hashCode = function () {
    return 45280091;
  };
  protoOf(Supported).equals = function (other) {
    if (this === other)
      return true;
    if (!(other instanceof Supported))
      return false;
    return true;
  };
  var Supported_instance;
  function Supported_getInstance() {
    return Supported_instance;
  }
  function Unsupported() {
  }
  protoOf(Unsupported).toString = function () {
    return 'Unsupported';
  };
  protoOf(Unsupported).hashCode = function () {
    return -1831059038;
  };
  protoOf(Unsupported).equals = function (other) {
    if (this === other)
      return true;
    if (!(other instanceof Unsupported))
      return false;
    return true;
  };
  var Unsupported_instance;
  function Unsupported_getInstance() {
    return Unsupported_instance;
  }
  function VersionDependent(requiredVersion, currentVersion) {
    this.requiredVersion_1 = requiredVersion;
    this.currentVersion_1 = currentVersion;
  }
  protoOf(VersionDependent).toString = function () {
    return 'VersionDependent(requiredVersion=' + this.requiredVersion_1.toString() + ', currentVersion=' + toString(this.currentVersion_1) + ')';
  };
  protoOf(VersionDependent).hashCode = function () {
    var result = this.requiredVersion_1.hashCode();
    result = imul(result, 31) + (this.currentVersion_1 == null ? 0 : this.currentVersion_1.hashCode()) | 0;
    return result;
  };
  protoOf(VersionDependent).equals = function (other) {
    if (this === other)
      return true;
    if (!(other instanceof VersionDependent))
      return false;
    if (!this.requiredVersion_1.equals(other.requiredVersion_1))
      return false;
    if (!equals(this.currentVersion_1, other.currentVersion_1))
      return false;
    return true;
  };
  function PermissionDependent() {
  }
  var static_init_called;
  function static_init() {
    if (static_init_called)
      return Unit_instance;
    static_init_called = true;
    MiniAppLifecycleState_FOREGROUND_instance = new MiniAppLifecycleState('FOREGROUND', 0);
    MiniAppLifecycleState_BACKGROUND_instance = new MiniAppLifecycleState('BACKGROUND', 1);
  }
  var MiniAppLifecycleState_FOREGROUND_instance;
  var MiniAppLifecycleState_BACKGROUND_instance;
  function MiniAppLifecycleState(name, ordinal) {
    Enum.call(this, name, ordinal);
  }
  function Companion() {
    Companion_instance_1 = this;
    this.Key_1 = new CapabilityKey('lifecycle');
  }
  var Companion_instance_1;
  function Companion_getInstance() {
    if (Companion_instance_1 == null)
      new Companion();
    return Companion_instance_1;
  }
  function MiniAppLifecycleState_FOREGROUND_getInstance() {
    static_init();
    return MiniAppLifecycleState_FOREGROUND_instance;
  }
  function MiniAppLifecycleState_BACKGROUND_getInstance() {
    static_init();
    return MiniAppLifecycleState_BACKGROUND_instance;
  }
  var static_init_called_0;
  function static_init_0() {
    if (static_init_called_0)
      return Unit_instance;
    static_init_called_0 = true;
    HttpMethod_GET_instance = new HttpMethod('GET', 0);
    HttpMethod_POST_instance = new HttpMethod('POST', 1);
    HttpMethod_PUT_instance = new HttpMethod('PUT', 2);
    HttpMethod_PATCH_instance = new HttpMethod('PATCH', 3);
    HttpMethod_DELETE_instance = new HttpMethod('DELETE', 4);
    HttpMethod_HEAD_instance = new HttpMethod('HEAD', 5);
  }
  var HttpMethod_GET_instance;
  var HttpMethod_POST_instance;
  var HttpMethod_PUT_instance;
  var HttpMethod_PATCH_instance;
  var HttpMethod_DELETE_instance;
  var HttpMethod_HEAD_instance;
  function values() {
    static_init_0();
    return [HttpMethod_GET_getInstance(), HttpMethod_POST_getInstance(), HttpMethod_PUT_getInstance(), HttpMethod_PATCH_getInstance(), HttpMethod_DELETE_getInstance(), HttpMethod_HEAD_getInstance()];
  }
  function get_entries() {
    static_init_0();
    if ($ENTRIES == null)
      $ENTRIES = enumEntries(values());
    return $ENTRIES;
  }
  var $ENTRIES;
  function HttpMethod(name, ordinal) {
    Enum.call(this, name, ordinal);
  }
  function MiniAppHttpRequest(url, method, headers, body, timeoutMillis) {
    method = method === VOID ? HttpMethod_GET_getInstance() : method;
    headers = headers === VOID ? emptyMap() : headers;
    body = body === VOID ? null : body;
    timeoutMillis = timeoutMillis === VOID ? null : timeoutMillis;
    this.url_1 = url;
    this.method_1 = method;
    this.headers_1 = headers;
    this.body_1 = body;
    this.timeoutMillis_1 = timeoutMillis;
  }
  function MiniAppHttpResponse(statusCode, headers, body) {
    this.statusCode_1 = statusCode;
    this.headers_1 = headers;
    this.body_1 = body;
  }
  function Companion_0() {
    Companion_instance_2 = this;
    this.Key_1 = new CapabilityKey('network');
  }
  var Companion_instance_2;
  function Companion_getInstance_0() {
    if (Companion_instance_2 == null)
      new Companion_0();
    return Companion_instance_2;
  }
  function HttpMethod_GET_getInstance() {
    static_init_0();
    return HttpMethod_GET_instance;
  }
  function HttpMethod_POST_getInstance() {
    static_init_0();
    return HttpMethod_POST_instance;
  }
  function HttpMethod_PUT_getInstance() {
    static_init_0();
    return HttpMethod_PUT_instance;
  }
  function HttpMethod_PATCH_getInstance() {
    static_init_0();
    return HttpMethod_PATCH_instance;
  }
  function HttpMethod_DELETE_getInstance() {
    static_init_0();
    return HttpMethod_DELETE_instance;
  }
  function HttpMethod_HEAD_getInstance() {
    static_init_0();
    return HttpMethod_HEAD_instance;
  }
  function NotRequested() {
  }
  protoOf(NotRequested).toString = function () {
    return 'NotRequested';
  };
  protoOf(NotRequested).hashCode = function () {
    return 1674602186;
  };
  protoOf(NotRequested).equals = function (other) {
    if (this === other)
      return true;
    if (!(other instanceof NotRequested))
      return false;
    return true;
  };
  var NotRequested_instance;
  function NotRequested_getInstance() {
    return NotRequested_instance;
  }
  function Granted() {
  }
  protoOf(Granted).toString = function () {
    return 'Granted';
  };
  protoOf(Granted).hashCode = function () {
    return 995131500;
  };
  protoOf(Granted).equals = function (other) {
    if (this === other)
      return true;
    if (!(other instanceof Granted))
      return false;
    return true;
  };
  var Granted_instance;
  function Granted_getInstance() {
    return Granted_instance;
  }
  function Denied() {
  }
  protoOf(Denied).toString = function () {
    return 'Denied';
  };
  protoOf(Denied).hashCode = function () {
    return -1035241526;
  };
  protoOf(Denied).equals = function (other) {
    if (this === other)
      return true;
    if (!(other instanceof Denied))
      return false;
    return true;
  };
  var Denied_instance;
  function Denied_getInstance() {
    return Denied_instance;
  }
  function Companion_1() {
    Companion_instance_3 = this;
    this.Key_1 = new CapabilityKey('permission');
  }
  var Companion_instance_3;
  function Companion_getInstance_1() {
    if (Companion_instance_3 == null)
      new Companion_1();
    return Companion_instance_3;
  }
  function Companion_2() {
    Companion_instance_4 = this;
    this.Microphone_1 = new PermissionKey('microphone');
  }
  var Companion_instance_4;
  function Companion_getInstance_2() {
    if (Companion_instance_4 == null)
      new Companion_2();
    return Companion_instance_4;
  }
  function PermissionKey(value) {
    Companion_getInstance_2();
    this.value_1 = value;
  }
  protoOf(PermissionKey).toString = function () {
    return 'PermissionKey(value=' + this.value_1 + ')';
  };
  protoOf(PermissionKey).hashCode = function () {
    return getStringHashCode(this.value_1);
  };
  protoOf(PermissionKey).equals = function (other) {
    if (this === other)
      return true;
    if (!(other instanceof PermissionKey))
      return false;
    if (!(this.value_1 === other.value_1))
      return false;
    return true;
  };
  var static_init_called_1;
  function static_init_1() {
    if (static_init_called_1)
      return Unit_instance;
    static_init_called_1 = true;
    PrivacyAuthorizationRequirement_REQUIRED_instance = new PrivacyAuthorizationRequirement('REQUIRED', 0);
    PrivacyAuthorizationRequirement_NOT_REQUIRED_instance = new PrivacyAuthorizationRequirement('NOT_REQUIRED', 1);
  }
  var PrivacyAuthorizationRequirement_REQUIRED_instance;
  var PrivacyAuthorizationRequirement_NOT_REQUIRED_instance;
  function PrivacyAuthorizationRequirement(name, ordinal) {
    Enum.call(this, name, ordinal);
  }
  function PrivacyStatus(requirement, contractName) {
    this.requirement_1 = requirement;
    this.contractName_1 = contractName;
  }
  function Authorized() {
  }
  protoOf(Authorized).toString = function () {
    return 'Authorized';
  };
  protoOf(Authorized).hashCode = function () {
    return -1102673974;
  };
  protoOf(Authorized).equals = function (other) {
    if (this === other)
      return true;
    if (!(other instanceof Authorized))
      return false;
    return true;
  };
  var Authorized_instance;
  function Authorized_getInstance() {
    return Authorized_instance;
  }
  function Refused() {
  }
  protoOf(Refused).toString = function () {
    return 'Refused';
  };
  protoOf(Refused).hashCode = function () {
    return 1085560577;
  };
  protoOf(Refused).equals = function (other) {
    if (this === other)
      return true;
    if (!(other instanceof Refused))
      return false;
    return true;
  };
  var Refused_instance;
  function Refused_getInstance() {
    return Refused_instance;
  }
  function Companion_3() {
    Companion_instance_5 = this;
    this.Key_1 = new CapabilityKey('privacy');
  }
  var Companion_instance_5;
  function Companion_getInstance_3() {
    if (Companion_instance_5 == null)
      new Companion_3();
    return Companion_instance_5;
  }
  function PrivacyAuthorizationRequirement_REQUIRED_getInstance() {
    static_init_1();
    return PrivacyAuthorizationRequirement_REQUIRED_instance;
  }
  function PrivacyAuthorizationRequirement_NOT_REQUIRED_getInstance() {
    static_init_1();
    return PrivacyAuthorizationRequirement_NOT_REQUIRED_instance;
  }
  function Companion_4() {
    Companion_instance_6 = this;
    this.Key_1 = new CapabilityKey('storage');
  }
  var Companion_instance_6;
  function Companion_getInstance_4() {
    if (Companion_instance_6 == null)
      new Companion_4();
    return Companion_instance_6;
  }
  function UnsupportedCapability(capability) {
    MiniAppException.call(this, 'Unsupported capability: ' + capability.value_1);
    captureStack(this, UnsupportedCapability);
    this.capability_1 = capability;
  }
  function PermissionDenied(permission, message, cause) {
    message = message === VOID ? 'Permission denied' : message;
    cause = cause === VOID ? null : cause;
    MiniAppException.call(this, message, cause);
    captureStack(this, PermissionDenied);
    this.permission_1 = permission;
  }
  function Timeout(operation, hostMessage, cause) {
    cause = cause === VOID ? null : cause;
    MiniAppException.call(this, operation + ' timed out: ' + hostMessage, cause);
    captureStack(this, Timeout);
    this.operation_1 = operation;
    this.hostMessage_1 = hostMessage;
  }
  function HostFailure(host, code, hostMessage, metadata, cause) {
    metadata = metadata === VOID ? emptyMap() : metadata;
    cause = cause === VOID ? null : cause;
    MiniAppException.call(this, host + ' host failure: ' + hostMessage, cause);
    captureStack(this, HostFailure);
    this.host_1 = host;
    this.code_1 = code;
    this.hostMessage_1 = hostMessage;
    this.metadata_1 = metadata;
  }
  function InvalidResponse(message, cause) {
    cause = cause === VOID ? null : cause;
    MiniAppException.call(this, message, cause);
    captureStack(this, InvalidResponse);
  }
  function PrivacyAuthorizationRequired(contractName) {
    contractName = contractName === VOID ? null : contractName;
    var tmp;
    if (contractName == null) {
      tmp = null;
    } else {
      // Inline function 'kotlin.let' call
      tmp = 'Privacy authorization required: ' + contractName;
    }
    var tmp1_elvis_lhs = tmp;
    MiniAppException.call(this, tmp1_elvis_lhs == null ? 'Privacy authorization required' : tmp1_elvis_lhs);
    captureStack(this, PrivacyAuthorizationRequired);
    this.contractName_1 = contractName;
  }
  function InternalFailure(message, cause) {
    cause = cause === VOID ? null : cause;
    MiniAppException.call(this, message, cause);
    captureStack(this, InternalFailure);
  }
  function MiniAppException(message, cause) {
    cause = cause === VOID ? null : cause;
    Exception_init_$Init$(message, cause, this);
    captureStack(this, MiniAppException);
  }
  function Companion_5() {
  }
  protoOf(Companion_5).parse_pc1q8p_k$ = function (raw) {
    // Inline function 'kotlin.text.trim' call
    var tmp$ret$0 = toString_0(trim(isCharSequence(raw) ? raw : THROW_CCE()));
    var parts = split(tmp$ret$0, charArrayOf([_Char___init__impl__6a9atx(46)]));
    if (parts.isEmpty_y1axqb_k$())
      return null;
    var parsed = ArrayList_init_$Create$(parts.get_size_woubt6_k$());
    var _iterator__ex2g4s = parts.iterator_jk1svi_k$();
    while (_iterator__ex2g4s.hasNext_bitz1p_k$()) {
      var part = _iterator__ex2g4s.next_20eer_k$();
      var tmp0_elvis_lhs = toIntOrNull(part);
      var tmp;
      if (tmp0_elvis_lhs == null) {
        return null;
      } else {
        tmp = tmp0_elvis_lhs;
      }
      var segment = tmp;
      if (segment < 0)
        return null;
      // Inline function 'kotlin.collections.plusAssign' call
      parsed.add_utx5q5_k$(segment);
    }
    while (parsed.get_size_woubt6_k$() > 1 && last(parsed) === 0) {
      parsed.removeAt_6niowx_k$(get_lastIndex(parsed));
    }
    // Inline function 'kotlin.text.trim' call
    var tmp$ret$2 = toString_0(trim(isCharSequence(raw) ? raw : THROW_CCE()));
    return new HostVersion(parsed, tmp$ret$2);
  };
  var Companion_instance_7;
  function Companion_getInstance_5() {
    return Companion_instance_7;
  }
  function HostVersion(segments, text) {
    this.segments_1 = segments;
    this.text_1 = text;
  }
  protoOf(HostVersion).compareTo_wrrkcm_k$ = function (other) {
    var tmp0 = this.segments_1.get_size_woubt6_k$();
    // Inline function 'kotlin.comparisons.maxOf' call
    var b = other.segments_1.get_size_woubt6_k$();
    var length = Math.max(tmp0, b);
    var inductionVariable = 0;
    if (inductionVariable < length)
      do {
        var index = inductionVariable;
        inductionVariable = inductionVariable + 1 | 0;
        // Inline function 'kotlin.collections.getOrElse' call
        var this_0 = this.segments_1;
        var tmp;
        if (0 <= index ? index < this_0.get_size_woubt6_k$() : false) {
          tmp = this_0.get_c1px32_k$(index);
        } else {
          tmp = 0;
        }
        var mine = tmp;
        // Inline function 'kotlin.collections.getOrElse' call
        var this_1 = other.segments_1;
        var tmp_0;
        if (0 <= index ? index < this_1.get_size_woubt6_k$() : false) {
          tmp_0 = this_1.get_c1px32_k$(index);
        } else {
          tmp_0 = 0;
        }
        var theirs = tmp_0;
        if (!(mine === theirs))
          return compareTo(mine, theirs);
      }
       while (inductionVariable < length);
    return 0;
  };
  protoOf(HostVersion).compareTo_hpufkf_k$ = function (other) {
    return this.compareTo_wrrkcm_k$(other instanceof HostVersion ? other : THROW_CCE());
  };
  protoOf(HostVersion).equals = function (other) {
    var tmp;
    if (this === other) {
      tmp = true;
    } else {
      var tmp_0;
      if (other instanceof HostVersion) {
        tmp_0 = equals(this.segments_1, other.segments_1);
      } else {
        tmp_0 = false;
      }
      tmp = tmp_0;
    }
    return tmp;
  };
  protoOf(HostVersion).hashCode = function () {
    return hashCode(this.segments_1);
  };
  protoOf(HostVersion).toString = function () {
    return this.text_1;
  };
  function requireSupported(_this__u8e3s4, capability) {
    if (!equals(_this__u8e3s4.capabilitySupport_p8flwh_k$(capability), Supported_instance)) {
      throw new UnsupportedCapability(capability);
    }
  }
  function JsCapabilitySupport(state, requiredVersion, currentVersion, permission) {
    this.state = state;
    this.requiredVersion = requiredVersion;
    this.currentVersion = currentVersion;
    this.permission = permission;
  }
  protoOf(JsCapabilitySupport).get_state_iypx7s_k$ = function () {
    return this.state;
  };
  protoOf(JsCapabilitySupport).get_requiredVersion_8krsts_k$ = function () {
    return this.requiredVersion;
  };
  protoOf(JsCapabilitySupport).get_currentVersion_cvqjoo_k$ = function () {
    return this.currentVersion;
  };
  protoOf(JsCapabilitySupport).get_permission_sqffe0_k$ = function () {
    return this.permission;
  };
  function JsPrivacyStatus(requirement, contractName) {
    this.requirement = requirement;
    this.contractName = contractName;
  }
  protoOf(JsPrivacyStatus).get_requirement_jwjwt6_k$ = function () {
    return this.requirement;
  };
  protoOf(JsPrivacyStatus).get_contractName_ingpa2_k$ = function () {
    return this.contractName;
  };
  function JsRuntimeInfo(baseLibraryVersion, platform, isDeveloperTools) {
    this.baseLibraryVersion = baseLibraryVersion;
    this.platform = platform;
    this.isDeveloperTools = isDeveloperTools;
  }
  protoOf(JsRuntimeInfo).get_baseLibraryVersion_i4tt1z_k$ = function () {
    return this.baseLibraryVersion;
  };
  protoOf(JsRuntimeInfo).get_platform_ssr7o_k$ = function () {
    return this.platform;
  };
  protoOf(JsRuntimeInfo).get_isDeveloperTools_3vcog4_k$ = function () {
    return this.isDeveloperTools;
  };
  function $storageGet$suspendBridgeCOROUTINE$(_this__u8e3s4, key, resultContinuation) {
    CoroutineImpl.call(this, resultContinuation);
    this._this__u8e3s4__1 = _this__u8e3s4;
    this.key_1 = key;
  }
  protoOf($storageGet$suspendBridgeCOROUTINE$).doResume_5yljmg_k$ = function () {
    var suspendResult = this.result_1;
    $sm: do
      try {
        var tmp = this.state_1;
        switch (tmp) {
          case 0:
            this.exceptionState_1 = 4;
            if (this._this__u8e3s4__1.storageGet === protoOf(MiniAppExports).storageGet) {
              this.state_1 = 2;
              suspendResult = this._this__u8e3s4__1.storageGet_o5km91_k$(this.key_1, this);
              if (suspendResult === get_COROUTINE_SUSPENDED()) {
                return suspendResult;
              }
              continue $sm;
            } else {
              this.state_1 = 1;
              suspendResult = await_0(this._this__u8e3s4__1.storageGet(this.key_1), this);
              if (suspendResult === get_COROUTINE_SUSPENDED()) {
                return suspendResult;
              }
              continue $sm;
            }

          case 1:
            this.WHEN_RESULT0__1 = suspendResult;
            this.state_1 = 3;
            continue $sm;
          case 2:
            this.WHEN_RESULT0__1 = suspendResult;
            this.state_1 = 3;
            continue $sm;
          case 3:
            return this.WHEN_RESULT0__1;
          case 4:
            throw this.exception_1;
        }
      } catch ($p) {
        var e = $p;
        if (this.exceptionState_1 === 4) {
          throw e;
        } else {
          this.state_1 = this.exceptionState_1;
          this.exception_1 = e;
        }
      }
     while (true);
  };
  function $storageSet$suspendBridgeCOROUTINE$(_this__u8e3s4, key, value, resultContinuation) {
    CoroutineImpl.call(this, resultContinuation);
    this._this__u8e3s4__1 = _this__u8e3s4;
    this.key_1 = key;
    this.value_1 = value;
  }
  protoOf($storageSet$suspendBridgeCOROUTINE$).doResume_5yljmg_k$ = function () {
    var suspendResult = this.result_1;
    $sm: do
      try {
        var tmp = this.state_1;
        switch (tmp) {
          case 0:
            this.exceptionState_1 = 4;
            if (this._this__u8e3s4__1.storageSet === protoOf(MiniAppExports).storageSet) {
              this.state_1 = 2;
              suspendResult = this._this__u8e3s4__1.storageSet_nsuyb4_k$(this.key_1, this.value_1, this);
              if (suspendResult === get_COROUTINE_SUSPENDED()) {
                return suspendResult;
              }
              continue $sm;
            } else {
              this.state_1 = 1;
              suspendResult = await_0(this._this__u8e3s4__1.storageSet(this.key_1, this.value_1), this);
              if (suspendResult === get_COROUTINE_SUSPENDED()) {
                return suspendResult;
              }
              continue $sm;
            }

          case 1:
            this.state_1 = 3;
            continue $sm;
          case 2:
            this.state_1 = 3;
            continue $sm;
          case 3:
            return Unit_instance;
          case 4:
            throw this.exception_1;
        }
      } catch ($p) {
        var e = $p;
        if (this.exceptionState_1 === 4) {
          throw e;
        } else {
          this.state_1 = this.exceptionState_1;
          this.exception_1 = e;
        }
      }
     while (true);
  };
  function $storageRemove$suspendBridgeCOROUTINE$(_this__u8e3s4, key, resultContinuation) {
    CoroutineImpl.call(this, resultContinuation);
    this._this__u8e3s4__1 = _this__u8e3s4;
    this.key_1 = key;
  }
  protoOf($storageRemove$suspendBridgeCOROUTINE$).doResume_5yljmg_k$ = function () {
    var suspendResult = this.result_1;
    $sm: do
      try {
        var tmp = this.state_1;
        switch (tmp) {
          case 0:
            this.exceptionState_1 = 4;
            if (this._this__u8e3s4__1.storageRemove === protoOf(MiniAppExports).storageRemove) {
              this.state_1 = 2;
              suspendResult = this._this__u8e3s4__1.storageRemove_auapvg_k$(this.key_1, this);
              if (suspendResult === get_COROUTINE_SUSPENDED()) {
                return suspendResult;
              }
              continue $sm;
            } else {
              this.state_1 = 1;
              suspendResult = await_0(this._this__u8e3s4__1.storageRemove(this.key_1), this);
              if (suspendResult === get_COROUTINE_SUSPENDED()) {
                return suspendResult;
              }
              continue $sm;
            }

          case 1:
            this.state_1 = 3;
            continue $sm;
          case 2:
            this.state_1 = 3;
            continue $sm;
          case 3:
            return Unit_instance;
          case 4:
            throw this.exception_1;
        }
      } catch ($p) {
        var e = $p;
        if (this.exceptionState_1 === 4) {
          throw e;
        } else {
          this.state_1 = this.exceptionState_1;
          this.exception_1 = e;
        }
      }
     while (true);
  };
  function $wechatLogin$suspendBridgeCOROUTINE$(_this__u8e3s4, resultContinuation) {
    CoroutineImpl.call(this, resultContinuation);
    this._this__u8e3s4__1 = _this__u8e3s4;
  }
  protoOf($wechatLogin$suspendBridgeCOROUTINE$).doResume_5yljmg_k$ = function () {
    var suspendResult = this.result_1;
    $sm: do
      try {
        var tmp = this.state_1;
        switch (tmp) {
          case 0:
            this.exceptionState_1 = 4;
            if (this._this__u8e3s4__1.wechatLogin === protoOf(MiniAppExports).wechatLogin) {
              this.state_1 = 2;
              suspendResult = this._this__u8e3s4__1.wechatLogin_s06qk6_k$(this);
              if (suspendResult === get_COROUTINE_SUSPENDED()) {
                return suspendResult;
              }
              continue $sm;
            } else {
              this.state_1 = 1;
              suspendResult = await_0(this._this__u8e3s4__1.wechatLogin(), this);
              if (suspendResult === get_COROUTINE_SUSPENDED()) {
                return suspendResult;
              }
              continue $sm;
            }

          case 1:
            this.WHEN_RESULT0__1 = suspendResult;
            this.state_1 = 3;
            continue $sm;
          case 2:
            this.WHEN_RESULT0__1 = suspendResult;
            this.state_1 = 3;
            continue $sm;
          case 3:
            return this.WHEN_RESULT0__1;
          case 4:
            throw this.exception_1;
        }
      } catch ($p) {
        var e = $p;
        if (this.exceptionState_1 === 4) {
          throw e;
        } else {
          this.state_1 = this.exceptionState_1;
          this.exception_1 = e;
        }
      }
     while (true);
  };
  function $wechatCheckSessionCOROUTINE$(_this__u8e3s4, resultContinuation) {
    CoroutineImpl.call(this, resultContinuation);
    this._this__u8e3s4__1 = _this__u8e3s4;
  }
  protoOf($wechatCheckSessionCOROUTINE$).doResume_5yljmg_k$ = function () {
    var suspendResult = this.result_1;
    $sm: do
      try {
        var tmp = this.state_1;
        switch (tmp) {
          case 0:
            this.exceptionState_1 = 2;
            this.state_1 = 1;
            suspendResult = this._this__u8e3s4__1.host_1.platform_1.auth_1.checkSession_drbyba_k$(this);
            if (suspendResult === get_COROUTINE_SUSPENDED()) {
              return suspendResult;
            }

            continue $sm;
          case 1:
            var tmp_0;
            switch (suspendResult.ordinal_1) {
              case 0:
                tmp_0 = 'Valid';
                break;
              case 1:
                tmp_0 = 'Invalid';
                break;
              default:
                noWhenBranchMatchedException();
                break;
            }

            return tmp_0;
          case 2:
            throw this.exception_1;
        }
      } catch ($p) {
        var e = $p;
        if (this.exceptionState_1 === 2) {
          throw e;
        } else {
          this.state_1 = this.exceptionState_1;
          this.exception_1 = e;
        }
      }
     while (true);
  };
  function $wechatCheckSession$suspendBridgeCOROUTINE$(_this__u8e3s4, resultContinuation) {
    CoroutineImpl.call(this, resultContinuation);
    this._this__u8e3s4__1 = _this__u8e3s4;
  }
  protoOf($wechatCheckSession$suspendBridgeCOROUTINE$).doResume_5yljmg_k$ = function () {
    var suspendResult = this.result_1;
    $sm: do
      try {
        var tmp = this.state_1;
        switch (tmp) {
          case 0:
            this.exceptionState_1 = 4;
            if (this._this__u8e3s4__1.wechatCheckSession === protoOf(MiniAppExports).wechatCheckSession) {
              this.state_1 = 2;
              suspendResult = this._this__u8e3s4__1.wechatCheckSession_eplnsf_k$(this);
              if (suspendResult === get_COROUTINE_SUSPENDED()) {
                return suspendResult;
              }
              continue $sm;
            } else {
              this.state_1 = 1;
              suspendResult = await_0(this._this__u8e3s4__1.wechatCheckSession(), this);
              if (suspendResult === get_COROUTINE_SUSPENDED()) {
                return suspendResult;
              }
              continue $sm;
            }

          case 1:
            this.WHEN_RESULT0__1 = suspendResult;
            this.state_1 = 3;
            continue $sm;
          case 2:
            this.WHEN_RESULT0__1 = suspendResult;
            this.state_1 = 3;
            continue $sm;
          case 3:
            return this.WHEN_RESULT0__1;
          case 4:
            throw this.exception_1;
        }
      } catch ($p) {
        var e = $p;
        if (this.exceptionState_1 === 4) {
          throw e;
        } else {
          this.state_1 = this.exceptionState_1;
          this.exception_1 = e;
        }
      }
     while (true);
  };
  function $networkRequestCOROUTINE$(_this__u8e3s4, url, method, headers, body, timeoutMillis, resultContinuation) {
    CoroutineImpl.call(this, resultContinuation);
    this._this__u8e3s4__1 = _this__u8e3s4;
    this.url_1 = url;
    this.method_1 = method;
    this.headers_1 = headers;
    this.body_1 = body;
    this.timeoutMillis_1 = timeoutMillis;
  }
  protoOf($networkRequestCOROUTINE$).doResume_5yljmg_k$ = function () {
    var suspendResult = this.result_1;
    $sm: do
      try {
        var tmp = this.state_1;
        switch (tmp) {
          case 0:
            this.exceptionState_1 = 2;
            this.state_1 = 1;
            suspendResult = this._this__u8e3s4__1.network_1.request_wvdz26_k$(new MiniAppHttpRequest(this.url_1, httpMethod(this.method_1), headerMap(this.headers_1), this.body_1, this.timeoutMillis_1), this);
            if (suspendResult === get_COROUTINE_SUSPENDED()) {
              return suspendResult;
            }

            continue $sm;
          case 1:
            var response = suspendResult;
            var tmp0 = response.headers_1;
            var destination = ArrayList_init_$Create$_0();
            var _iterator__ex2g4s = tmp0.get_entries_p20ztl_k$().iterator_jk1svi_k$();
            while (_iterator__ex2g4s.hasNext_bitz1p_k$()) {
              var element = _iterator__ex2g4s.next_20eer_k$();
              var name = element.get_key_18j28a_k$();
              var value = element.get_value_j01efc_k$();
              var list = listOf([name, value]);
              addAll(destination, list);
            }

            return new MiniAppHttpResult(response.statusCode_1, copyToArray(destination), response.body_1);
          case 2:
            throw this.exception_1;
        }
      } catch ($p) {
        var e = $p;
        if (this.exceptionState_1 === 2) {
          throw e;
        } else {
          this.state_1 = this.exceptionState_1;
          this.exception_1 = e;
        }
      }
     while (true);
  };
  function $networkRequest$suspendBridgeCOROUTINE$(_this__u8e3s4, url, method, headers, body, timeoutMillis, resultContinuation) {
    CoroutineImpl.call(this, resultContinuation);
    this._this__u8e3s4__1 = _this__u8e3s4;
    this.url_1 = url;
    this.method_1 = method;
    this.headers_1 = headers;
    this.body_1 = body;
    this.timeoutMillis_1 = timeoutMillis;
  }
  protoOf($networkRequest$suspendBridgeCOROUTINE$).doResume_5yljmg_k$ = function () {
    var suspendResult = this.result_1;
    $sm: do
      try {
        var tmp = this.state_1;
        switch (tmp) {
          case 0:
            this.exceptionState_1 = 4;
            if (this._this__u8e3s4__1.networkRequest === protoOf(MiniAppExports).networkRequest) {
              this.state_1 = 2;
              suspendResult = this._this__u8e3s4__1.networkRequest_4c3zq_k$(this.url_1, this.method_1, this.headers_1, this.body_1, this.timeoutMillis_1, this);
              if (suspendResult === get_COROUTINE_SUSPENDED()) {
                return suspendResult;
              }
              continue $sm;
            } else {
              this.state_1 = 1;
              suspendResult = await_0(this._this__u8e3s4__1.networkRequest(this.url_1, this.method_1, this.headers_1, this.body_1, this.timeoutMillis_1), this);
              if (suspendResult === get_COROUTINE_SUSPENDED()) {
                return suspendResult;
              }
              continue $sm;
            }

          case 1:
            this.WHEN_RESULT0__1 = suspendResult;
            this.state_1 = 3;
            continue $sm;
          case 2:
            this.WHEN_RESULT0__1 = suspendResult;
            this.state_1 = 3;
            continue $sm;
          case 3:
            return this.WHEN_RESULT0__1;
          case 4:
            throw this.exception_1;
        }
      } catch ($p) {
        var e = $p;
        if (this.exceptionState_1 === 4) {
          throw e;
        } else {
          this.state_1 = this.exceptionState_1;
          this.exception_1 = e;
        }
      }
     while (true);
  };
  function $wechatNavigateTo$suspendBridgeCOROUTINE$(_this__u8e3s4, url, resultContinuation) {
    CoroutineImpl.call(this, resultContinuation);
    this._this__u8e3s4__1 = _this__u8e3s4;
    this.url_1 = url;
  }
  protoOf($wechatNavigateTo$suspendBridgeCOROUTINE$).doResume_5yljmg_k$ = function () {
    var suspendResult = this.result_1;
    $sm: do
      try {
        var tmp = this.state_1;
        switch (tmp) {
          case 0:
            this.exceptionState_1 = 4;
            if (this._this__u8e3s4__1.wechatNavigateTo === protoOf(MiniAppExports).wechatNavigateTo) {
              this.state_1 = 2;
              suspendResult = this._this__u8e3s4__1.wechatNavigateTo_jnmyz5_k$(this.url_1, this);
              if (suspendResult === get_COROUTINE_SUSPENDED()) {
                return suspendResult;
              }
              continue $sm;
            } else {
              this.state_1 = 1;
              suspendResult = await_0(this._this__u8e3s4__1.wechatNavigateTo(this.url_1), this);
              if (suspendResult === get_COROUTINE_SUSPENDED()) {
                return suspendResult;
              }
              continue $sm;
            }

          case 1:
            this.state_1 = 3;
            continue $sm;
          case 2:
            this.state_1 = 3;
            continue $sm;
          case 3:
            return Unit_instance;
          case 4:
            throw this.exception_1;
        }
      } catch ($p) {
        var e = $p;
        if (this.exceptionState_1 === 4) {
          throw e;
        } else {
          this.state_1 = this.exceptionState_1;
          this.exception_1 = e;
        }
      }
     while (true);
  };
  function $wechatRedirectTo$suspendBridgeCOROUTINE$(_this__u8e3s4, url, resultContinuation) {
    CoroutineImpl.call(this, resultContinuation);
    this._this__u8e3s4__1 = _this__u8e3s4;
    this.url_1 = url;
  }
  protoOf($wechatRedirectTo$suspendBridgeCOROUTINE$).doResume_5yljmg_k$ = function () {
    var suspendResult = this.result_1;
    $sm: do
      try {
        var tmp = this.state_1;
        switch (tmp) {
          case 0:
            this.exceptionState_1 = 4;
            if (this._this__u8e3s4__1.wechatRedirectTo === protoOf(MiniAppExports).wechatRedirectTo) {
              this.state_1 = 2;
              suspendResult = this._this__u8e3s4__1.wechatRedirectTo_p6jja2_k$(this.url_1, this);
              if (suspendResult === get_COROUTINE_SUSPENDED()) {
                return suspendResult;
              }
              continue $sm;
            } else {
              this.state_1 = 1;
              suspendResult = await_0(this._this__u8e3s4__1.wechatRedirectTo(this.url_1), this);
              if (suspendResult === get_COROUTINE_SUSPENDED()) {
                return suspendResult;
              }
              continue $sm;
            }

          case 1:
            this.state_1 = 3;
            continue $sm;
          case 2:
            this.state_1 = 3;
            continue $sm;
          case 3:
            return Unit_instance;
          case 4:
            throw this.exception_1;
        }
      } catch ($p) {
        var e = $p;
        if (this.exceptionState_1 === 4) {
          throw e;
        } else {
          this.state_1 = this.exceptionState_1;
          this.exception_1 = e;
        }
      }
     while (true);
  };
  function $wechatNavigateBack$suspendBridgeCOROUTINE$(_this__u8e3s4, delta, resultContinuation) {
    CoroutineImpl.call(this, resultContinuation);
    this._this__u8e3s4__1 = _this__u8e3s4;
    this.delta_1 = delta;
  }
  protoOf($wechatNavigateBack$suspendBridgeCOROUTINE$).doResume_5yljmg_k$ = function () {
    var suspendResult = this.result_1;
    $sm: do
      try {
        var tmp = this.state_1;
        switch (tmp) {
          case 0:
            this.exceptionState_1 = 4;
            if (this._this__u8e3s4__1.wechatNavigateBack === protoOf(MiniAppExports).wechatNavigateBack) {
              this.state_1 = 2;
              suspendResult = this._this__u8e3s4__1.wechatNavigateBack_q8huws_k$(this.delta_1, this);
              if (suspendResult === get_COROUTINE_SUSPENDED()) {
                return suspendResult;
              }
              continue $sm;
            } else {
              this.state_1 = 1;
              suspendResult = await_0(this._this__u8e3s4__1.wechatNavigateBack(this.delta_1), this);
              if (suspendResult === get_COROUTINE_SUSPENDED()) {
                return suspendResult;
              }
              continue $sm;
            }

          case 1:
            this.state_1 = 3;
            continue $sm;
          case 2:
            this.state_1 = 3;
            continue $sm;
          case 3:
            return Unit_instance;
          case 4:
            throw this.exception_1;
        }
      } catch ($p) {
        var e = $p;
        if (this.exceptionState_1 === 4) {
          throw e;
        } else {
          this.state_1 = this.exceptionState_1;
          this.exception_1 = e;
        }
      }
     while (true);
  };
  function $permissionStateCOROUTINE$(_this__u8e3s4, permission, resultContinuation) {
    CoroutineImpl.call(this, resultContinuation);
    this._this__u8e3s4__1 = _this__u8e3s4;
    this.permission_1 = permission;
  }
  protoOf($permissionStateCOROUTINE$).doResume_5yljmg_k$ = function () {
    var suspendResult = this.result_1;
    $sm: do
      try {
        var tmp = this.state_1;
        switch (tmp) {
          case 0:
            this.exceptionState_1 = 2;
            this.state_1 = 1;
            suspendResult = this._this__u8e3s4__1.permissions_1.stateOf_asu77n_k$(new PermissionKey(this.permission_1), this);
            if (suspendResult === get_COROUTINE_SUSPENDED()) {
              return suspendResult;
            }

            continue $sm;
          case 1:
            var ARGUMENT = suspendResult;
            return toJsName(ARGUMENT);
          case 2:
            throw this.exception_1;
        }
      } catch ($p) {
        var e = $p;
        if (this.exceptionState_1 === 2) {
          throw e;
        } else {
          this.state_1 = this.exceptionState_1;
          this.exception_1 = e;
        }
      }
     while (true);
  };
  function $permissionState$suspendBridgeCOROUTINE$(_this__u8e3s4, permission, resultContinuation) {
    CoroutineImpl.call(this, resultContinuation);
    this._this__u8e3s4__1 = _this__u8e3s4;
    this.permission_1 = permission;
  }
  protoOf($permissionState$suspendBridgeCOROUTINE$).doResume_5yljmg_k$ = function () {
    var suspendResult = this.result_1;
    $sm: do
      try {
        var tmp = this.state_1;
        switch (tmp) {
          case 0:
            this.exceptionState_1 = 4;
            if (this._this__u8e3s4__1.permissionState === protoOf(MiniAppExports).permissionState) {
              this.state_1 = 2;
              suspendResult = this._this__u8e3s4__1.permissionState_1ob2hh_k$(this.permission_1, this);
              if (suspendResult === get_COROUTINE_SUSPENDED()) {
                return suspendResult;
              }
              continue $sm;
            } else {
              this.state_1 = 1;
              suspendResult = await_0(this._this__u8e3s4__1.permissionState(this.permission_1), this);
              if (suspendResult === get_COROUTINE_SUSPENDED()) {
                return suspendResult;
              }
              continue $sm;
            }

          case 1:
            this.WHEN_RESULT0__1 = suspendResult;
            this.state_1 = 3;
            continue $sm;
          case 2:
            this.WHEN_RESULT0__1 = suspendResult;
            this.state_1 = 3;
            continue $sm;
          case 3:
            return this.WHEN_RESULT0__1;
          case 4:
            throw this.exception_1;
        }
      } catch ($p) {
        var e = $p;
        if (this.exceptionState_1 === 4) {
          throw e;
        } else {
          this.state_1 = this.exceptionState_1;
          this.exception_1 = e;
        }
      }
     while (true);
  };
  function $requestPermissionCOROUTINE$(_this__u8e3s4, permission, resultContinuation) {
    CoroutineImpl.call(this, resultContinuation);
    this._this__u8e3s4__1 = _this__u8e3s4;
    this.permission_1 = permission;
  }
  protoOf($requestPermissionCOROUTINE$).doResume_5yljmg_k$ = function () {
    var suspendResult = this.result_1;
    $sm: do
      try {
        var tmp = this.state_1;
        switch (tmp) {
          case 0:
            this.exceptionState_1 = 2;
            this.state_1 = 1;
            suspendResult = this._this__u8e3s4__1.permissions_1.request_pifwc6_k$(new PermissionKey(this.permission_1), this);
            if (suspendResult === get_COROUTINE_SUSPENDED()) {
              return suspendResult;
            }

            continue $sm;
          case 1:
            var ARGUMENT = suspendResult;
            return toJsName(ARGUMENT);
          case 2:
            throw this.exception_1;
        }
      } catch ($p) {
        var e = $p;
        if (this.exceptionState_1 === 2) {
          throw e;
        } else {
          this.state_1 = this.exceptionState_1;
          this.exception_1 = e;
        }
      }
     while (true);
  };
  function $requestPermission$suspendBridgeCOROUTINE$(_this__u8e3s4, permission, resultContinuation) {
    CoroutineImpl.call(this, resultContinuation);
    this._this__u8e3s4__1 = _this__u8e3s4;
    this.permission_1 = permission;
  }
  protoOf($requestPermission$suspendBridgeCOROUTINE$).doResume_5yljmg_k$ = function () {
    var suspendResult = this.result_1;
    $sm: do
      try {
        var tmp = this.state_1;
        switch (tmp) {
          case 0:
            this.exceptionState_1 = 4;
            if (this._this__u8e3s4__1.requestPermission === protoOf(MiniAppExports).requestPermission) {
              this.state_1 = 2;
              suspendResult = this._this__u8e3s4__1.requestPermission_l79dw9_k$(this.permission_1, this);
              if (suspendResult === get_COROUTINE_SUSPENDED()) {
                return suspendResult;
              }
              continue $sm;
            } else {
              this.state_1 = 1;
              suspendResult = await_0(this._this__u8e3s4__1.requestPermission(this.permission_1), this);
              if (suspendResult === get_COROUTINE_SUSPENDED()) {
                return suspendResult;
              }
              continue $sm;
            }

          case 1:
            this.WHEN_RESULT0__1 = suspendResult;
            this.state_1 = 3;
            continue $sm;
          case 2:
            this.WHEN_RESULT0__1 = suspendResult;
            this.state_1 = 3;
            continue $sm;
          case 3:
            return this.WHEN_RESULT0__1;
          case 4:
            throw this.exception_1;
        }
      } catch ($p) {
        var e = $p;
        if (this.exceptionState_1 === 4) {
          throw e;
        } else {
          this.state_1 = this.exceptionState_1;
          this.exception_1 = e;
        }
      }
     while (true);
  };
  function $openPermissionSettingsCOROUTINE$(_this__u8e3s4, permission, resultContinuation) {
    CoroutineImpl.call(this, resultContinuation);
    this._this__u8e3s4__1 = _this__u8e3s4;
    this.permission_1 = permission;
  }
  protoOf($openPermissionSettingsCOROUTINE$).doResume_5yljmg_k$ = function () {
    var suspendResult = this.result_1;
    $sm: do
      try {
        var tmp = this.state_1;
        switch (tmp) {
          case 0:
            this.exceptionState_1 = 2;
            this.state_1 = 1;
            suspendResult = this._this__u8e3s4__1.permissions_1.openSettings_ozycxk_k$(new PermissionKey(this.permission_1), this);
            if (suspendResult === get_COROUTINE_SUSPENDED()) {
              return suspendResult;
            }

            continue $sm;
          case 1:
            var ARGUMENT = suspendResult;
            return toJsName(ARGUMENT);
          case 2:
            throw this.exception_1;
        }
      } catch ($p) {
        var e = $p;
        if (this.exceptionState_1 === 2) {
          throw e;
        } else {
          this.state_1 = this.exceptionState_1;
          this.exception_1 = e;
        }
      }
     while (true);
  };
  function $openPermissionSettings$suspendBridgeCOROUTINE$(_this__u8e3s4, permission, resultContinuation) {
    CoroutineImpl.call(this, resultContinuation);
    this._this__u8e3s4__1 = _this__u8e3s4;
    this.permission_1 = permission;
  }
  protoOf($openPermissionSettings$suspendBridgeCOROUTINE$).doResume_5yljmg_k$ = function () {
    var suspendResult = this.result_1;
    $sm: do
      try {
        var tmp = this.state_1;
        switch (tmp) {
          case 0:
            this.exceptionState_1 = 4;
            if (this._this__u8e3s4__1.openPermissionSettings === protoOf(MiniAppExports).openPermissionSettings) {
              this.state_1 = 2;
              suspendResult = this._this__u8e3s4__1.openPermissionSettings_5q1nx7_k$(this.permission_1, this);
              if (suspendResult === get_COROUTINE_SUSPENDED()) {
                return suspendResult;
              }
              continue $sm;
            } else {
              this.state_1 = 1;
              suspendResult = await_0(this._this__u8e3s4__1.openPermissionSettings(this.permission_1), this);
              if (suspendResult === get_COROUTINE_SUSPENDED()) {
                return suspendResult;
              }
              continue $sm;
            }

          case 1:
            this.WHEN_RESULT0__1 = suspendResult;
            this.state_1 = 3;
            continue $sm;
          case 2:
            this.WHEN_RESULT0__1 = suspendResult;
            this.state_1 = 3;
            continue $sm;
          case 3:
            return this.WHEN_RESULT0__1;
          case 4:
            throw this.exception_1;
        }
      } catch ($p) {
        var e = $p;
        if (this.exceptionState_1 === 4) {
          throw e;
        } else {
          this.state_1 = this.exceptionState_1;
          this.exception_1 = e;
        }
      }
     while (true);
  };
  function $privacyStatusCOROUTINE$(_this__u8e3s4, resultContinuation) {
    CoroutineImpl.call(this, resultContinuation);
    this._this__u8e3s4__1 = _this__u8e3s4;
  }
  protoOf($privacyStatusCOROUTINE$).doResume_5yljmg_k$ = function () {
    var suspendResult = this.result_1;
    $sm: do
      try {
        var tmp = this.state_1;
        switch (tmp) {
          case 0:
            this.exceptionState_1 = 2;
            this.state_1 = 1;
            suspendResult = this._this__u8e3s4__1.privacy_1.status_1hikgl_k$(this);
            if (suspendResult === get_COROUTINE_SUSPENDED()) {
              return suspendResult;
            }

            continue $sm;
          case 1:
            var ARGUMENT = suspendResult;
            return toJs_1(ARGUMENT);
          case 2:
            throw this.exception_1;
        }
      } catch ($p) {
        var e = $p;
        if (this.exceptionState_1 === 2) {
          throw e;
        } else {
          this.state_1 = this.exceptionState_1;
          this.exception_1 = e;
        }
      }
     while (true);
  };
  function $privacyStatus$suspendBridgeCOROUTINE$(_this__u8e3s4, resultContinuation) {
    CoroutineImpl.call(this, resultContinuation);
    this._this__u8e3s4__1 = _this__u8e3s4;
  }
  protoOf($privacyStatus$suspendBridgeCOROUTINE$).doResume_5yljmg_k$ = function () {
    var suspendResult = this.result_1;
    $sm: do
      try {
        var tmp = this.state_1;
        switch (tmp) {
          case 0:
            this.exceptionState_1 = 4;
            if (this._this__u8e3s4__1.privacyStatus === protoOf(MiniAppExports).privacyStatus) {
              this.state_1 = 2;
              suspendResult = this._this__u8e3s4__1.privacyStatus_2bjc0t_k$(this);
              if (suspendResult === get_COROUTINE_SUSPENDED()) {
                return suspendResult;
              }
              continue $sm;
            } else {
              this.state_1 = 1;
              suspendResult = await_0(this._this__u8e3s4__1.privacyStatus(), this);
              if (suspendResult === get_COROUTINE_SUSPENDED()) {
                return suspendResult;
              }
              continue $sm;
            }

          case 1:
            this.WHEN_RESULT0__1 = suspendResult;
            this.state_1 = 3;
            continue $sm;
          case 2:
            this.WHEN_RESULT0__1 = suspendResult;
            this.state_1 = 3;
            continue $sm;
          case 3:
            return this.WHEN_RESULT0__1;
          case 4:
            throw this.exception_1;
        }
      } catch ($p) {
        var e = $p;
        if (this.exceptionState_1 === 4) {
          throw e;
        } else {
          this.state_1 = this.exceptionState_1;
          this.exception_1 = e;
        }
      }
     while (true);
  };
  function $requestPrivacyAuthorizationCOROUTINE$(_this__u8e3s4, resultContinuation) {
    CoroutineImpl.call(this, resultContinuation);
    this._this__u8e3s4__1 = _this__u8e3s4;
  }
  protoOf($requestPrivacyAuthorizationCOROUTINE$).doResume_5yljmg_k$ = function () {
    var suspendResult = this.result_1;
    $sm: do
      try {
        var tmp = this.state_1;
        switch (tmp) {
          case 0:
            this.exceptionState_1 = 2;
            this.state_1 = 1;
            suspendResult = this._this__u8e3s4__1.privacy_1.requestAuthorization_zgnahm_k$(this);
            if (suspendResult === get_COROUTINE_SUSPENDED()) {
              return suspendResult;
            }

            continue $sm;
          case 1:
            var tmp0_subject = suspendResult;
            var tmp_0;
            if (tmp0_subject instanceof Authorized) {
              tmp_0 = 'Authorized';
            } else {
              if (tmp0_subject instanceof Refused) {
                tmp_0 = 'Refused';
              } else {
                noWhenBranchMatchedException();
              }
            }

            return tmp_0;
          case 2:
            throw this.exception_1;
        }
      } catch ($p) {
        var e = $p;
        if (this.exceptionState_1 === 2) {
          throw e;
        } else {
          this.state_1 = this.exceptionState_1;
          this.exception_1 = e;
        }
      }
     while (true);
  };
  function $requestPrivacyAuthorization$suspendBridgeCOROUTINE$(_this__u8e3s4, resultContinuation) {
    CoroutineImpl.call(this, resultContinuation);
    this._this__u8e3s4__1 = _this__u8e3s4;
  }
  protoOf($requestPrivacyAuthorization$suspendBridgeCOROUTINE$).doResume_5yljmg_k$ = function () {
    var suspendResult = this.result_1;
    $sm: do
      try {
        var tmp = this.state_1;
        switch (tmp) {
          case 0:
            this.exceptionState_1 = 4;
            if (this._this__u8e3s4__1.requestPrivacyAuthorization === protoOf(MiniAppExports).requestPrivacyAuthorization) {
              this.state_1 = 2;
              suspendResult = this._this__u8e3s4__1.requestPrivacyAuthorization_uj1b6r_k$(this);
              if (suspendResult === get_COROUTINE_SUSPENDED()) {
                return suspendResult;
              }
              continue $sm;
            } else {
              this.state_1 = 1;
              suspendResult = await_0(this._this__u8e3s4__1.requestPrivacyAuthorization(), this);
              if (suspendResult === get_COROUTINE_SUSPENDED()) {
                return suspendResult;
              }
              continue $sm;
            }

          case 1:
            this.WHEN_RESULT0__1 = suspendResult;
            this.state_1 = 3;
            continue $sm;
          case 2:
            this.WHEN_RESULT0__1 = suspendResult;
            this.state_1 = 3;
            continue $sm;
          case 3:
            return this.WHEN_RESULT0__1;
          case 4:
            throw this.exception_1;
        }
      } catch ($p) {
        var e = $p;
        if (this.exceptionState_1 === 4) {
          throw e;
        } else {
          this.state_1 = this.exceptionState_1;
          this.exception_1 = e;
        }
      }
     while (true);
  };
  function $wechatGetClipboardText$suspendBridgeCOROUTINE$(_this__u8e3s4, resultContinuation) {
    CoroutineImpl.call(this, resultContinuation);
    this._this__u8e3s4__1 = _this__u8e3s4;
  }
  protoOf($wechatGetClipboardText$suspendBridgeCOROUTINE$).doResume_5yljmg_k$ = function () {
    var suspendResult = this.result_1;
    $sm: do
      try {
        var tmp = this.state_1;
        switch (tmp) {
          case 0:
            this.exceptionState_1 = 4;
            if (this._this__u8e3s4__1.wechatGetClipboardText === protoOf(MiniAppExports).wechatGetClipboardText) {
              this.state_1 = 2;
              suspendResult = this._this__u8e3s4__1.wechatGetClipboardText_o789ww_k$(this);
              if (suspendResult === get_COROUTINE_SUSPENDED()) {
                return suspendResult;
              }
              continue $sm;
            } else {
              this.state_1 = 1;
              suspendResult = await_0(this._this__u8e3s4__1.wechatGetClipboardText(), this);
              if (suspendResult === get_COROUTINE_SUSPENDED()) {
                return suspendResult;
              }
              continue $sm;
            }

          case 1:
            this.WHEN_RESULT0__1 = suspendResult;
            this.state_1 = 3;
            continue $sm;
          case 2:
            this.WHEN_RESULT0__1 = suspendResult;
            this.state_1 = 3;
            continue $sm;
          case 3:
            return this.WHEN_RESULT0__1;
          case 4:
            throw this.exception_1;
        }
      } catch ($p) {
        var e = $p;
        if (this.exceptionState_1 === 4) {
          throw e;
        } else {
          this.state_1 = this.exceptionState_1;
          this.exception_1 = e;
        }
      }
     while (true);
  };
  function $wechatSetClipboardText$suspendBridgeCOROUTINE$(_this__u8e3s4, value, resultContinuation) {
    CoroutineImpl.call(this, resultContinuation);
    this._this__u8e3s4__1 = _this__u8e3s4;
    this.value_1 = value;
  }
  protoOf($wechatSetClipboardText$suspendBridgeCOROUTINE$).doResume_5yljmg_k$ = function () {
    var suspendResult = this.result_1;
    $sm: do
      try {
        var tmp = this.state_1;
        switch (tmp) {
          case 0:
            this.exceptionState_1 = 4;
            if (this._this__u8e3s4__1.wechatSetClipboardText === protoOf(MiniAppExports).wechatSetClipboardText) {
              this.state_1 = 2;
              suspendResult = this._this__u8e3s4__1.wechatSetClipboardText_sebon0_k$(this.value_1, this);
              if (suspendResult === get_COROUTINE_SUSPENDED()) {
                return suspendResult;
              }
              continue $sm;
            } else {
              this.state_1 = 1;
              suspendResult = await_0(this._this__u8e3s4__1.wechatSetClipboardText(this.value_1), this);
              if (suspendResult === get_COROUTINE_SUSPENDED()) {
                return suspendResult;
              }
              continue $sm;
            }

          case 1:
            this.state_1 = 3;
            continue $sm;
          case 2:
            this.state_1 = 3;
            continue $sm;
          case 3:
            return Unit_instance;
          case 4:
            throw this.exception_1;
        }
      } catch ($p) {
        var e = $p;
        if (this.exceptionState_1 === 4) {
          throw e;
        } else {
          this.state_1 = this.exceptionState_1;
          this.exception_1 = e;
        }
      }
     while (true);
  };
  function $wechatVibrateShort$suspendBridgeCOROUTINE$(_this__u8e3s4, resultContinuation) {
    CoroutineImpl.call(this, resultContinuation);
    this._this__u8e3s4__1 = _this__u8e3s4;
  }
  protoOf($wechatVibrateShort$suspendBridgeCOROUTINE$).doResume_5yljmg_k$ = function () {
    var suspendResult = this.result_1;
    $sm: do
      try {
        var tmp = this.state_1;
        switch (tmp) {
          case 0:
            this.exceptionState_1 = 4;
            if (this._this__u8e3s4__1.wechatVibrateShort === protoOf(MiniAppExports).wechatVibrateShort) {
              this.state_1 = 2;
              suspendResult = this._this__u8e3s4__1.wechatVibrateShort_xz7yuc_k$(this);
              if (suspendResult === get_COROUTINE_SUSPENDED()) {
                return suspendResult;
              }
              continue $sm;
            } else {
              this.state_1 = 1;
              suspendResult = await_0(this._this__u8e3s4__1.wechatVibrateShort(), this);
              if (suspendResult === get_COROUTINE_SUSPENDED()) {
                return suspendResult;
              }
              continue $sm;
            }

          case 1:
            this.state_1 = 3;
            continue $sm;
          case 2:
            this.state_1 = 3;
            continue $sm;
          case 3:
            return Unit_instance;
          case 4:
            throw this.exception_1;
        }
      } catch ($p) {
        var e = $p;
        if (this.exceptionState_1 === 4) {
          throw e;
        } else {
          this.state_1 = this.exceptionState_1;
          this.exception_1 = e;
        }
      }
     while (true);
  };
  function $wechatVibrateLong$suspendBridgeCOROUTINE$(_this__u8e3s4, resultContinuation) {
    CoroutineImpl.call(this, resultContinuation);
    this._this__u8e3s4__1 = _this__u8e3s4;
  }
  protoOf($wechatVibrateLong$suspendBridgeCOROUTINE$).doResume_5yljmg_k$ = function () {
    var suspendResult = this.result_1;
    $sm: do
      try {
        var tmp = this.state_1;
        switch (tmp) {
          case 0:
            this.exceptionState_1 = 4;
            if (this._this__u8e3s4__1.wechatVibrateLong === protoOf(MiniAppExports).wechatVibrateLong) {
              this.state_1 = 2;
              suspendResult = this._this__u8e3s4__1.wechatVibrateLong_6hhu6m_k$(this);
              if (suspendResult === get_COROUTINE_SUSPENDED()) {
                return suspendResult;
              }
              continue $sm;
            } else {
              this.state_1 = 1;
              suspendResult = await_0(this._this__u8e3s4__1.wechatVibrateLong(), this);
              if (suspendResult === get_COROUTINE_SUSPENDED()) {
                return suspendResult;
              }
              continue $sm;
            }

          case 1:
            this.state_1 = 3;
            continue $sm;
          case 2:
            this.state_1 = 3;
            continue $sm;
          case 3:
            return Unit_instance;
          case 4:
            throw this.exception_1;
        }
      } catch ($p) {
        var e = $p;
        if (this.exceptionState_1 === 4) {
          throw e;
        } else {
          this.state_1 = this.exceptionState_1;
          this.exception_1 = e;
        }
      }
     while (true);
  };
  function $wechatReadTextFile$suspendBridgeCOROUTINE$(_this__u8e3s4, path, resultContinuation) {
    CoroutineImpl.call(this, resultContinuation);
    this._this__u8e3s4__1 = _this__u8e3s4;
    this.path_1 = path;
  }
  protoOf($wechatReadTextFile$suspendBridgeCOROUTINE$).doResume_5yljmg_k$ = function () {
    var suspendResult = this.result_1;
    $sm: do
      try {
        var tmp = this.state_1;
        switch (tmp) {
          case 0:
            this.exceptionState_1 = 4;
            if (this._this__u8e3s4__1.wechatReadTextFile === protoOf(MiniAppExports).wechatReadTextFile) {
              this.state_1 = 2;
              suspendResult = this._this__u8e3s4__1.wechatReadTextFile_vs3pua_k$(this.path_1, this);
              if (suspendResult === get_COROUTINE_SUSPENDED()) {
                return suspendResult;
              }
              continue $sm;
            } else {
              this.state_1 = 1;
              suspendResult = await_0(this._this__u8e3s4__1.wechatReadTextFile(this.path_1), this);
              if (suspendResult === get_COROUTINE_SUSPENDED()) {
                return suspendResult;
              }
              continue $sm;
            }

          case 1:
            this.WHEN_RESULT0__1 = suspendResult;
            this.state_1 = 3;
            continue $sm;
          case 2:
            this.WHEN_RESULT0__1 = suspendResult;
            this.state_1 = 3;
            continue $sm;
          case 3:
            return this.WHEN_RESULT0__1;
          case 4:
            throw this.exception_1;
        }
      } catch ($p) {
        var e = $p;
        if (this.exceptionState_1 === 4) {
          throw e;
        } else {
          this.state_1 = this.exceptionState_1;
          this.exception_1 = e;
        }
      }
     while (true);
  };
  function $wechatWriteTextFile$suspendBridgeCOROUTINE$(_this__u8e3s4, path, content, resultContinuation) {
    CoroutineImpl.call(this, resultContinuation);
    this._this__u8e3s4__1 = _this__u8e3s4;
    this.path_1 = path;
    this.content_1 = content;
  }
  protoOf($wechatWriteTextFile$suspendBridgeCOROUTINE$).doResume_5yljmg_k$ = function () {
    var suspendResult = this.result_1;
    $sm: do
      try {
        var tmp = this.state_1;
        switch (tmp) {
          case 0:
            this.exceptionState_1 = 4;
            if (this._this__u8e3s4__1.wechatWriteTextFile === protoOf(MiniAppExports).wechatWriteTextFile) {
              this.state_1 = 2;
              suspendResult = this._this__u8e3s4__1.wechatWriteTextFile_8kv3fv_k$(this.path_1, this.content_1, this);
              if (suspendResult === get_COROUTINE_SUSPENDED()) {
                return suspendResult;
              }
              continue $sm;
            } else {
              this.state_1 = 1;
              suspendResult = await_0(this._this__u8e3s4__1.wechatWriteTextFile(this.path_1, this.content_1), this);
              if (suspendResult === get_COROUTINE_SUSPENDED()) {
                return suspendResult;
              }
              continue $sm;
            }

          case 1:
            this.state_1 = 3;
            continue $sm;
          case 2:
            this.state_1 = 3;
            continue $sm;
          case 3:
            return Unit_instance;
          case 4:
            throw this.exception_1;
        }
      } catch ($p) {
        var e = $p;
        if (this.exceptionState_1 === 4) {
          throw e;
        } else {
          this.state_1 = this.exceptionState_1;
          this.exception_1 = e;
        }
      }
     while (true);
  };
  function $wechatFileExists$suspendBridgeCOROUTINE$(_this__u8e3s4, path, resultContinuation) {
    CoroutineImpl.call(this, resultContinuation);
    this._this__u8e3s4__1 = _this__u8e3s4;
    this.path_1 = path;
  }
  protoOf($wechatFileExists$suspendBridgeCOROUTINE$).doResume_5yljmg_k$ = function () {
    var suspendResult = this.result_1;
    $sm: do
      try {
        var tmp = this.state_1;
        switch (tmp) {
          case 0:
            this.exceptionState_1 = 4;
            if (this._this__u8e3s4__1.wechatFileExists === protoOf(MiniAppExports).wechatFileExists) {
              this.state_1 = 2;
              suspendResult = this._this__u8e3s4__1.wechatFileExists_b0dmd8_k$(this.path_1, this);
              if (suspendResult === get_COROUTINE_SUSPENDED()) {
                return suspendResult;
              }
              continue $sm;
            } else {
              this.state_1 = 1;
              suspendResult = await_0(this._this__u8e3s4__1.wechatFileExists(this.path_1), this);
              if (suspendResult === get_COROUTINE_SUSPENDED()) {
                return suspendResult;
              }
              continue $sm;
            }

          case 1:
            this.WHEN_RESULT0__1 = suspendResult;
            this.state_1 = 3;
            continue $sm;
          case 2:
            this.WHEN_RESULT0__1 = suspendResult;
            this.state_1 = 3;
            continue $sm;
          case 3:
            return this.WHEN_RESULT0__1;
          case 4:
            throw this.exception_1;
        }
      } catch ($p) {
        var e = $p;
        if (this.exceptionState_1 === 4) {
          throw e;
        } else {
          this.state_1 = this.exceptionState_1;
          this.exception_1 = e;
        }
      }
     while (true);
  };
  function $wechatRemoveFile$suspendBridgeCOROUTINE$(_this__u8e3s4, path, resultContinuation) {
    CoroutineImpl.call(this, resultContinuation);
    this._this__u8e3s4__1 = _this__u8e3s4;
    this.path_1 = path;
  }
  protoOf($wechatRemoveFile$suspendBridgeCOROUTINE$).doResume_5yljmg_k$ = function () {
    var suspendResult = this.result_1;
    $sm: do
      try {
        var tmp = this.state_1;
        switch (tmp) {
          case 0:
            this.exceptionState_1 = 4;
            if (this._this__u8e3s4__1.wechatRemoveFile === protoOf(MiniAppExports).wechatRemoveFile) {
              this.state_1 = 2;
              suspendResult = this._this__u8e3s4__1.wechatRemoveFile_8wuoxv_k$(this.path_1, this);
              if (suspendResult === get_COROUTINE_SUSPENDED()) {
                return suspendResult;
              }
              continue $sm;
            } else {
              this.state_1 = 1;
              suspendResult = await_0(this._this__u8e3s4__1.wechatRemoveFile(this.path_1), this);
              if (suspendResult === get_COROUTINE_SUSPENDED()) {
                return suspendResult;
              }
              continue $sm;
            }

          case 1:
            this.state_1 = 3;
            continue $sm;
          case 2:
            this.state_1 = 3;
            continue $sm;
          case 3:
            return Unit_instance;
          case 4:
            throw this.exception_1;
        }
      } catch ($p) {
        var e = $p;
        if (this.exceptionState_1 === 4) {
          throw e;
        } else {
          this.state_1 = this.exceptionState_1;
          this.exception_1 = e;
        }
      }
     while (true);
  };
  function $requirePrivacySatisfied$suspendBridgeCOROUTINE$(_this__u8e3s4, resultContinuation) {
    CoroutineImpl.call(this, resultContinuation);
    this._this__u8e3s4__1 = _this__u8e3s4;
  }
  protoOf($requirePrivacySatisfied$suspendBridgeCOROUTINE$).doResume_5yljmg_k$ = function () {
    var suspendResult = this.result_1;
    $sm: do
      try {
        var tmp = this.state_1;
        switch (tmp) {
          case 0:
            this.exceptionState_1 = 4;
            if (this._this__u8e3s4__1.requirePrivacySatisfied === protoOf(MiniAppExports).requirePrivacySatisfied) {
              this.state_1 = 2;
              suspendResult = this._this__u8e3s4__1.requirePrivacySatisfied_m2jemw_k$(this);
              if (suspendResult === get_COROUTINE_SUSPENDED()) {
                return suspendResult;
              }
              continue $sm;
            } else {
              this.state_1 = 1;
              suspendResult = await_0(this._this__u8e3s4__1.requirePrivacySatisfied(), this);
              if (suspendResult === get_COROUTINE_SUSPENDED()) {
                return suspendResult;
              }
              continue $sm;
            }

          case 1:
            this.state_1 = 3;
            continue $sm;
          case 2:
            this.state_1 = 3;
            continue $sm;
          case 3:
            return Unit_instance;
          case 4:
            throw this.exception_1;
        }
      } catch ($p) {
        var e = $p;
        if (this.exceptionState_1 === 4) {
          throw e;
        } else {
          this.state_1 = this.exceptionState_1;
          this.exception_1 = e;
        }
      }
     while (true);
  };
  function MiniAppExports() {
    MiniAppExports_instance = this;
    this.host_1 = new WechatHost();
    this.storage_1 = this.host_1.storage_1;
    this.network_1 = this.host_1.network_1;
    this.permissions_1 = this.host_1.permissions_1;
    this.privacy_1 = this.host_1.privacy_1;
  }
  protoOf(MiniAppExports).sdkVersion = function () {
    return '0.1.0-SNAPSHOT';
  };
  protoOf(MiniAppExports).storageGet_o5km91_k$ = function (key, $completion) {
    return this.storage_1.get_3l5a7q_k$(key, $completion);
  };
  protoOf(MiniAppExports).storageGet = function (key) {
    var tmp = this;
    return promisify(function ($completion) {
      return tmp.storageGet_o5km91_k$(key, $completion);
    });
  };
  protoOf(MiniAppExports).storageGet$suspendBridge_6ozruk_k$ = function (key, $completion) {
    var tmp = new $storageGet$suspendBridgeCOROUTINE$(this, key, $completion);
    tmp.result_1 = Unit_instance;
    tmp.exception_1 = null;
    return tmp.doResume_5yljmg_k$();
  };
  protoOf(MiniAppExports).storageSet_nsuyb4_k$ = function (key, value, $completion) {
    return this.storage_1.set_qwzljf_k$(key, value, $completion);
  };
  protoOf(MiniAppExports).storageSet = function (key, value) {
    var tmp = this;
    return promisify(function ($completion) {
      return tmp.storageSet_nsuyb4_k$(key, value, $completion);
    });
  };
  protoOf(MiniAppExports).storageSet$suspendBridge_ayk8dr_k$ = function (key, value, $completion) {
    var tmp = new $storageSet$suspendBridgeCOROUTINE$(this, key, value, $completion);
    tmp.result_1 = Unit_instance;
    tmp.exception_1 = null;
    return tmp.doResume_5yljmg_k$();
  };
  protoOf(MiniAppExports).storageRemove_auapvg_k$ = function (key, $completion) {
    return this.storage_1.remove_gwf6lb_k$(key, $completion);
  };
  protoOf(MiniAppExports).storageRemove = function (key) {
    var tmp = this;
    return promisify(function ($completion) {
      return tmp.storageRemove_auapvg_k$(key, $completion);
    });
  };
  protoOf(MiniAppExports).storageRemove$suspendBridge_cp23i5_k$ = function (key, $completion) {
    var tmp = new $storageRemove$suspendBridgeCOROUTINE$(this, key, $completion);
    tmp.result_1 = Unit_instance;
    tmp.exception_1 = null;
    return tmp.doResume_5yljmg_k$();
  };
  protoOf(MiniAppExports).wechatLogin_s06qk6_k$ = function ($completion) {
    return this.host_1.platform_1.auth_1.login_hvnjm8_k$($completion);
  };
  protoOf(MiniAppExports).wechatLogin = function () {
    var tmp = this;
    return promisify(function ($completion) {
      return tmp.wechatLogin_s06qk6_k$($completion);
    });
  };
  protoOf(MiniAppExports).wechatLogin$suspendBridge_iydznv_k$ = function ($completion) {
    var tmp = new $wechatLogin$suspendBridgeCOROUTINE$(this, $completion);
    tmp.result_1 = Unit_instance;
    tmp.exception_1 = null;
    return tmp.doResume_5yljmg_k$();
  };
  protoOf(MiniAppExports).wechatCheckSession_eplnsf_k$ = function ($completion) {
    var tmp = new $wechatCheckSessionCOROUTINE$(this, $completion);
    tmp.result_1 = Unit_instance;
    tmp.exception_1 = null;
    return tmp.doResume_5yljmg_k$();
  };
  protoOf(MiniAppExports).wechatCheckSession = function () {
    var tmp = this;
    return promisify(function ($completion) {
      return tmp.wechatCheckSession_eplnsf_k$($completion);
    });
  };
  protoOf(MiniAppExports).wechatCheckSession$suspendBridge_kthmku_k$ = function ($completion) {
    var tmp = new $wechatCheckSession$suspendBridgeCOROUTINE$(this, $completion);
    tmp.result_1 = Unit_instance;
    tmp.exception_1 = null;
    return tmp.doResume_5yljmg_k$();
  };
  protoOf(MiniAppExports).networkRequest_4c3zq_k$ = function (url, method, headers, body, timeoutMillis, $completion) {
    var tmp = new $networkRequestCOROUTINE$(this, url, method, headers, body, timeoutMillis, $completion);
    tmp.result_1 = Unit_instance;
    tmp.exception_1 = null;
    return tmp.doResume_5yljmg_k$();
  };
  protoOf(MiniAppExports).networkRequest = function (url, method, headers, body, timeoutMillis) {
    var tmp = this;
    return promisify(function ($completion) {
      return tmp.networkRequest_4c3zq_k$(url, method, headers, body, timeoutMillis, $completion);
    });
  };
  protoOf(MiniAppExports).networkRequest$suspendBridge_7v2lz_k$ = function (url, method, headers, body, timeoutMillis, $completion) {
    var tmp = new $networkRequest$suspendBridgeCOROUTINE$(this, url, method, headers, body, timeoutMillis, $completion);
    tmp.result_1 = Unit_instance;
    tmp.exception_1 = null;
    return tmp.doResume_5yljmg_k$();
  };
  protoOf(MiniAppExports).wechatAppOnLaunch = function () {
    return this.host_1.platform_1.appLifecycle_1.appLaunched_2kde65_k$();
  };
  protoOf(MiniAppExports).wechatAppOnShow = function () {
    return this.host_1.platform_1.appLifecycle_1.appShown_f14600_k$();
  };
  protoOf(MiniAppExports).wechatAppOnHide = function () {
    return this.host_1.platform_1.appLifecycle_1.appHidden_uce53v_k$();
  };
  protoOf(MiniAppExports).wechatAppLifecycleState = function () {
    return this.host_1.lifecycle_1.get_state_iypx7s_k$().name_1;
  };
  protoOf(MiniAppExports).wechatPageOnShow = function (route) {
    return this.host_1.platform_1.pageLifecycle_1.pageShown_ox1yl4_k$(route);
  };
  protoOf(MiniAppExports).wechatPageOnHide = function () {
    return this.host_1.platform_1.pageLifecycle_1.pageHidden_de0n61_k$();
  };
  protoOf(MiniAppExports).wechatPageOnUnload = function (route) {
    return this.host_1.platform_1.pageLifecycle_1.pageUnloaded_2xrz7h_k$(route);
  };
  protoOf(MiniAppExports).wechatPageRoute = function () {
    return this.host_1.platform_1.pageLifecycle_1.get_currentRoute_smvwyh_k$();
  };
  protoOf(MiniAppExports).wechatNavigateTo_jnmyz5_k$ = function (url, $completion) {
    return this.host_1.platform_1.navigation_1.navigateTo_na3mc7_k$(url, $completion);
  };
  protoOf(MiniAppExports).wechatNavigateTo = function (url) {
    var tmp = this;
    return promisify(function ($completion) {
      return tmp.wechatNavigateTo_jnmyz5_k$(url, $completion);
    });
  };
  protoOf(MiniAppExports).wechatNavigateTo$suspendBridge_wf579s_k$ = function (url, $completion) {
    var tmp = new $wechatNavigateTo$suspendBridgeCOROUTINE$(this, url, $completion);
    tmp.result_1 = Unit_instance;
    tmp.exception_1 = null;
    return tmp.doResume_5yljmg_k$();
  };
  protoOf(MiniAppExports).wechatRedirectTo_p6jja2_k$ = function (url, $completion) {
    return this.host_1.platform_1.navigation_1.redirectTo_lk2vx0_k$(url, $completion);
  };
  protoOf(MiniAppExports).wechatRedirectTo = function (url) {
    var tmp = this;
    return promisify(function ($completion) {
      return tmp.wechatRedirectTo_p6jja2_k$(url, $completion);
    });
  };
  protoOf(MiniAppExports).wechatRedirectTo$suspendBridge_slcwpn_k$ = function (url, $completion) {
    var tmp = new $wechatRedirectTo$suspendBridgeCOROUTINE$(this, url, $completion);
    tmp.result_1 = Unit_instance;
    tmp.exception_1 = null;
    return tmp.doResume_5yljmg_k$();
  };
  protoOf(MiniAppExports).wechatNavigateBack_q8huws_k$ = function (delta, $completion) {
    return this.host_1.platform_1.navigation_1.navigateBack_hskp56_k$(delta, $completion);
  };
  protoOf(MiniAppExports).wechatNavigateBack = function (delta) {
    var tmp = this;
    return promisify(function ($completion) {
      return tmp.wechatNavigateBack_q8huws_k$(delta, $completion);
    });
  };
  protoOf(MiniAppExports).wechatNavigateBack$suspendBridge_hn0w4d_k$ = function (delta, $completion) {
    var tmp = new $wechatNavigateBack$suspendBridgeCOROUTINE$(this, delta, $completion);
    tmp.result_1 = Unit_instance;
    tmp.exception_1 = null;
    return tmp.doResume_5yljmg_k$();
  };
  protoOf(MiniAppExports).capabilitySupport = function (capability) {
    return toJs(this.host_1.capabilitySupport_p8flwh_k$(new CapabilityKey(capability)));
  };
  protoOf(MiniAppExports).requireCapability = function (capability) {
    return requireSupported(this.host_1, new CapabilityKey(capability));
  };
  protoOf(MiniAppExports).wechatRuntimeInfo = function () {
    return toJs_0(this.host_1.platform_1.runtimeInfo_1);
  };
  protoOf(MiniAppExports).wechatCanIUse = function (schema) {
    return this.host_1.platform_1.runtimeInfo_1.canIUse_xcpuhg_k$(schema);
  };
  protoOf(MiniAppExports).permissionState_1ob2hh_k$ = function (permission, $completion) {
    var tmp = new $permissionStateCOROUTINE$(this, permission, $completion);
    tmp.result_1 = Unit_instance;
    tmp.exception_1 = null;
    return tmp.doResume_5yljmg_k$();
  };
  protoOf(MiniAppExports).permissionState = function (permission) {
    var tmp = this;
    return promisify(function ($completion) {
      return tmp.permissionState_1ob2hh_k$(permission, $completion);
    });
  };
  protoOf(MiniAppExports).permissionState$suspendBridge_720wlw_k$ = function (permission, $completion) {
    var tmp = new $permissionState$suspendBridgeCOROUTINE$(this, permission, $completion);
    tmp.result_1 = Unit_instance;
    tmp.exception_1 = null;
    return tmp.doResume_5yljmg_k$();
  };
  protoOf(MiniAppExports).requestPermission_l79dw9_k$ = function (permission, $completion) {
    var tmp = new $requestPermissionCOROUTINE$(this, permission, $completion);
    tmp.result_1 = Unit_instance;
    tmp.exception_1 = null;
    return tmp.doResume_5yljmg_k$();
  };
  protoOf(MiniAppExports).requestPermission = function (permission) {
    var tmp = this;
    return promisify(function ($completion) {
      return tmp.requestPermission_l79dw9_k$(permission, $completion);
    });
  };
  protoOf(MiniAppExports).requestPermission$suspendBridge_vkl7bs_k$ = function (permission, $completion) {
    var tmp = new $requestPermission$suspendBridgeCOROUTINE$(this, permission, $completion);
    tmp.result_1 = Unit_instance;
    tmp.exception_1 = null;
    return tmp.doResume_5yljmg_k$();
  };
  protoOf(MiniAppExports).openPermissionSettings_5q1nx7_k$ = function (permission, $completion) {
    var tmp = new $openPermissionSettingsCOROUTINE$(this, permission, $completion);
    tmp.result_1 = Unit_instance;
    tmp.exception_1 = null;
    return tmp.doResume_5yljmg_k$();
  };
  protoOf(MiniAppExports).openPermissionSettings = function (permission) {
    var tmp = this;
    return promisify(function ($completion) {
      return tmp.openPermissionSettings_5q1nx7_k$(permission, $completion);
    });
  };
  protoOf(MiniAppExports).openPermissionSettings$suspendBridge_9xn59i_k$ = function (permission, $completion) {
    var tmp = new $openPermissionSettings$suspendBridgeCOROUTINE$(this, permission, $completion);
    tmp.result_1 = Unit_instance;
    tmp.exception_1 = null;
    return tmp.doResume_5yljmg_k$();
  };
  protoOf(MiniAppExports).privacyStatus_2bjc0t_k$ = function ($completion) {
    var tmp = new $privacyStatusCOROUTINE$(this, $completion);
    tmp.result_1 = Unit_instance;
    tmp.exception_1 = null;
    return tmp.doResume_5yljmg_k$();
  };
  protoOf(MiniAppExports).privacyStatus = function () {
    var tmp = this;
    return promisify(function ($completion) {
      return tmp.privacyStatus_2bjc0t_k$($completion);
    });
  };
  protoOf(MiniAppExports).privacyStatus$suspendBridge_9ulgp0_k$ = function ($completion) {
    var tmp = new $privacyStatus$suspendBridgeCOROUTINE$(this, $completion);
    tmp.result_1 = Unit_instance;
    tmp.exception_1 = null;
    return tmp.doResume_5yljmg_k$();
  };
  protoOf(MiniAppExports).requestPrivacyAuthorization_uj1b6r_k$ = function ($completion) {
    var tmp = new $requestPrivacyAuthorizationCOROUTINE$(this, $completion);
    tmp.result_1 = Unit_instance;
    tmp.exception_1 = null;
    return tmp.doResume_5yljmg_k$();
  };
  protoOf(MiniAppExports).requestPrivacyAuthorization = function () {
    var tmp = this;
    return promisify(function ($completion) {
      return tmp.requestPrivacyAuthorization_uj1b6r_k$($completion);
    });
  };
  protoOf(MiniAppExports).requestPrivacyAuthorization$suspendBridge_jnulky_k$ = function ($completion) {
    var tmp = new $requestPrivacyAuthorization$suspendBridgeCOROUTINE$(this, $completion);
    tmp.result_1 = Unit_instance;
    tmp.exception_1 = null;
    return tmp.doResume_5yljmg_k$();
  };
  protoOf(MiniAppExports).wechatGetClipboardText_o789ww_k$ = function ($completion) {
    return this.host_1.platform_1.clipboard_1.readText_iovdk0_k$($completion);
  };
  protoOf(MiniAppExports).wechatGetClipboardText = function () {
    var tmp = this;
    return promisify(function ($completion) {
      return tmp.wechatGetClipboardText_o789ww_k$($completion);
    });
  };
  protoOf(MiniAppExports).wechatGetClipboardText$suspendBridge_swn2fl_k$ = function ($completion) {
    var tmp = new $wechatGetClipboardText$suspendBridgeCOROUTINE$(this, $completion);
    tmp.result_1 = Unit_instance;
    tmp.exception_1 = null;
    return tmp.doResume_5yljmg_k$();
  };
  protoOf(MiniAppExports).wechatSetClipboardText_sebon0_k$ = function (value, $completion) {
    return this.host_1.platform_1.clipboard_1.writeText_hlinex_k$(value, $completion);
  };
  protoOf(MiniAppExports).wechatSetClipboardText = function (value) {
    var tmp = this;
    return promisify(function ($completion) {
      return tmp.wechatSetClipboardText_sebon0_k$(value, $completion);
    });
  };
  protoOf(MiniAppExports).wechatSetClipboardText$suspendBridge_592b7p_k$ = function (value, $completion) {
    var tmp = new $wechatSetClipboardText$suspendBridgeCOROUTINE$(this, value, $completion);
    tmp.result_1 = Unit_instance;
    tmp.exception_1 = null;
    return tmp.doResume_5yljmg_k$();
  };
  protoOf(MiniAppExports).wechatVibrateShort_xz7yuc_k$ = function ($completion) {
    return this.host_1.platform_1.haptics_1.vibrateShort_ict1au_k$($completion);
  };
  protoOf(MiniAppExports).wechatVibrateShort = function () {
    var tmp = this;
    return promisify(function ($completion) {
      return tmp.wechatVibrateShort_xz7yuc_k$($completion);
    });
  };
  protoOf(MiniAppExports).wechatVibrateShort$suspendBridge_snp0jn_k$ = function ($completion) {
    var tmp = new $wechatVibrateShort$suspendBridgeCOROUTINE$(this, $completion);
    tmp.result_1 = Unit_instance;
    tmp.exception_1 = null;
    return tmp.doResume_5yljmg_k$();
  };
  protoOf(MiniAppExports).wechatVibrateLong_6hhu6m_k$ = function ($completion) {
    return this.host_1.platform_1.haptics_1.vibrateLong_7v53xo_k$($completion);
  };
  protoOf(MiniAppExports).wechatVibrateLong = function () {
    var tmp = this;
    return promisify(function ($completion) {
      return tmp.wechatVibrateLong_6hhu6m_k$($completion);
    });
  };
  protoOf(MiniAppExports).wechatVibrateLong$suspendBridge_phnsr5_k$ = function ($completion) {
    var tmp = new $wechatVibrateLong$suspendBridgeCOROUTINE$(this, $completion);
    tmp.result_1 = Unit_instance;
    tmp.exception_1 = null;
    return tmp.doResume_5yljmg_k$();
  };
  protoOf(MiniAppExports).wechatUserDataPath = function () {
    return this.host_1.platform_1.fileSystem_1.userDataPath_gvvkd6_k$();
  };
  protoOf(MiniAppExports).wechatReadTextFile_vs3pua_k$ = function (path, $completion) {
    return this.host_1.platform_1.fileSystem_1.readText_vgeqrw_k$(path, $completion);
  };
  protoOf(MiniAppExports).wechatReadTextFile = function (path) {
    var tmp = this;
    return promisify(function ($completion) {
      return tmp.wechatReadTextFile_vs3pua_k$(path, $completion);
    });
  };
  protoOf(MiniAppExports).wechatReadTextFile$suspendBridge_gjx567_k$ = function (path, $completion) {
    var tmp = new $wechatReadTextFile$suspendBridgeCOROUTINE$(this, path, $completion);
    tmp.result_1 = Unit_instance;
    tmp.exception_1 = null;
    return tmp.doResume_5yljmg_k$();
  };
  protoOf(MiniAppExports).wechatWriteTextFile_8kv3fv_k$ = function (path, content, $completion) {
    return this.host_1.platform_1.fileSystem_1.writeText_bjaevp_k$(path, content, $completion);
  };
  protoOf(MiniAppExports).wechatWriteTextFile = function (path, content) {
    var tmp = this;
    return promisify(function ($completion) {
      return tmp.wechatWriteTextFile_8kv3fv_k$(path, content, $completion);
    });
  };
  protoOf(MiniAppExports).wechatWriteTextFile$suspendBridge_eymeus_k$ = function (path, content, $completion) {
    var tmp = new $wechatWriteTextFile$suspendBridgeCOROUTINE$(this, path, content, $completion);
    tmp.result_1 = Unit_instance;
    tmp.exception_1 = null;
    return tmp.doResume_5yljmg_k$();
  };
  protoOf(MiniAppExports).wechatFileExists_b0dmd8_k$ = function (path, $completion) {
    return this.host_1.platform_1.fileSystem_1.exists_77rlgi_k$(path, $completion);
  };
  protoOf(MiniAppExports).wechatFileExists = function (path) {
    var tmp = this;
    return promisify(function ($completion) {
      return tmp.wechatFileExists_b0dmd8_k$(path, $completion);
    });
  };
  protoOf(MiniAppExports).wechatFileExists$suspendBridge_6ncevv_k$ = function (path, $completion) {
    var tmp = new $wechatFileExists$suspendBridgeCOROUTINE$(this, path, $completion);
    tmp.result_1 = Unit_instance;
    tmp.exception_1 = null;
    return tmp.doResume_5yljmg_k$();
  };
  protoOf(MiniAppExports).wechatRemoveFile_8wuoxv_k$ = function (path, $completion) {
    return this.host_1.platform_1.fileSystem_1.remove_gwf6lb_k$(path, $completion);
  };
  protoOf(MiniAppExports).wechatRemoveFile = function (path) {
    var tmp = this;
    return promisify(function ($completion) {
      return tmp.wechatRemoveFile_8wuoxv_k$(path, $completion);
    });
  };
  protoOf(MiniAppExports).wechatRemoveFile$suspendBridge_b7ncck_k$ = function (path, $completion) {
    var tmp = new $wechatRemoveFile$suspendBridgeCOROUTINE$(this, path, $completion);
    tmp.result_1 = Unit_instance;
    tmp.exception_1 = null;
    return tmp.doResume_5yljmg_k$();
  };
  protoOf(MiniAppExports).requirePrivacySatisfied_m2jemw_k$ = function ($completion) {
    return this.privacy_1.requireSatisfied_farkra_k$($completion);
  };
  protoOf(MiniAppExports).requirePrivacySatisfied = function () {
    var tmp = this;
    return promisify(function ($completion) {
      return tmp.requirePrivacySatisfied_m2jemw_k$($completion);
    });
  };
  protoOf(MiniAppExports).requirePrivacySatisfied$suspendBridge_cykfsp_k$ = function ($completion) {
    var tmp = new $requirePrivacySatisfied$suspendBridgeCOROUTINE$(this, $completion);
    tmp.result_1 = Unit_instance;
    tmp.exception_1 = null;
    return tmp.doResume_5yljmg_k$();
  };
  var MiniAppExports_instance;
  function MiniAppExports_getInstance() {
    if (MiniAppExports_instance == null)
      new MiniAppExports();
    return MiniAppExports_instance;
  }
  function toJs(_this__u8e3s4) {
    var tmp;
    if (_this__u8e3s4 instanceof Supported) {
      tmp = new JsCapabilitySupport('Supported', null, null, null);
    } else {
      if (_this__u8e3s4 instanceof Unsupported) {
        tmp = new JsCapabilitySupport('Unsupported', null, null, null);
      } else {
        if (_this__u8e3s4 instanceof VersionDependent) {
          var tmp_0 = _this__u8e3s4.requiredVersion_1.toString();
          var tmp1_safe_receiver = _this__u8e3s4.currentVersion_1;
          tmp = new JsCapabilitySupport('VersionDependent', tmp_0, tmp1_safe_receiver == null ? null : tmp1_safe_receiver.toString(), null);
        } else {
          if (_this__u8e3s4 instanceof PermissionDependent) {
            tmp = new JsCapabilitySupport('PermissionDependent', null, null, _this__u8e3s4.permission_1);
          } else {
            noWhenBranchMatchedException();
          }
        }
      }
    }
    return tmp;
  }
  function toJs_0(_this__u8e3s4) {
    var tmp0_safe_receiver = _this__u8e3s4.get_baseLibraryVersion_i4tt1z_k$();
    return new JsRuntimeInfo(tmp0_safe_receiver == null ? null : tmp0_safe_receiver.toString(), _this__u8e3s4.get_platform_ssr7o_k$(), _this__u8e3s4.get_isDeveloperTools_3vcog4_k$());
  }
  function toJs_1(_this__u8e3s4) {
    var tmp;
    switch (_this__u8e3s4.requirement_1.ordinal_1) {
      case 0:
        tmp = 'REQUIRED';
        break;
      case 1:
        tmp = 'NOT_REQUIRED';
        break;
      default:
        noWhenBranchMatchedException();
        break;
    }
    return new JsPrivacyStatus(tmp, _this__u8e3s4.contractName_1);
  }
  function toJsName(_this__u8e3s4) {
    var tmp;
    if (_this__u8e3s4 instanceof NotRequested) {
      tmp = 'NotRequested';
    } else {
      if (_this__u8e3s4 instanceof Granted) {
        tmp = 'Granted';
      } else {
        if (_this__u8e3s4 instanceof Denied) {
          tmp = 'Denied';
        } else {
          noWhenBranchMatchedException();
        }
      }
    }
    return tmp;
  }
  function headerMap(headers) {
    // Inline function 'kotlin.require' call
    if (!((headers.length % 2 | 0) === 0)) {
      var message = 'headers must contain alternating name and value entries';
      throw IllegalArgumentException_init_$Create$(toString_0(message));
    }
    // Inline function 'kotlin.collections.mutableMapOf' call
    var result = LinkedHashMap_init_$Create$();
    var index = 0;
    while (index < headers.length) {
      var tmp2 = headers[index];
      // Inline function 'kotlin.collections.set' call
      var value = headers[index + 1 | 0];
      result.put_4fpzoq_k$(tmp2, value);
      index = index + 2 | 0;
    }
    return result;
  }
  function httpMethod(method) {
    // Inline function 'kotlin.text.uppercase' call
    // Inline function 'kotlin.js.asDynamic' call
    var normalized = method.toUpperCase();
    var tmp0 = get_entries();
    var tmp$ret$2;
    $l$block: {
      // Inline function 'kotlin.collections.firstOrNull' call
      var _iterator__ex2g4s = tmp0.iterator_jk1svi_k$();
      while (_iterator__ex2g4s.hasNext_bitz1p_k$()) {
        var element = _iterator__ex2g4s.next_20eer_k$();
        if (element.name_1 === normalized) {
          tmp$ret$2 = element;
          break $l$block;
        }
      }
      tmp$ret$2 = null;
    }
    var tmp0_elvis_lhs = tmp$ret$2;
    var tmp;
    if (tmp0_elvis_lhs == null) {
      throw IllegalArgumentException_init_$Create$("Unsupported HTTP method: '" + method + "'");
    } else {
      tmp = tmp0_elvis_lhs;
    }
    return tmp;
  }
  function MiniAppHttpResult(statusCode, headers, body) {
    this.statusCode = statusCode;
    this.headers = headers;
    this.body = body;
  }
  protoOf(MiniAppHttpResult).get_statusCode_g2w4u0_k$ = function () {
    return this.statusCode;
  };
  protoOf(MiniAppHttpResult).get_headers_ef25jx_k$ = function () {
    return this.headers;
  };
  protoOf(MiniAppHttpResult).get_body_wojkyz_k$ = function () {
    return this.body;
  };
  function WeChatDeviceCapabilities() {
    WeChatDeviceCapabilities_instance = this;
    this.ClipboardRead_1 = new CapabilityKey('wechat.clipboard-read');
    this.ClipboardWrite_1 = new CapabilityKey('wechat.clipboard-write');
    this.VibrateShort_1 = new CapabilityKey('wechat.vibrate-short');
    this.VibrateLong_1 = new CapabilityKey('wechat.vibrate-long');
    this.FileSystemRead_1 = new CapabilityKey('wechat.filesystem-read');
    this.FileSystemWrite_1 = new CapabilityKey('wechat.filesystem-write');
    this.FileSystemAccess_1 = new CapabilityKey('wechat.filesystem-access');
    this.FileSystemRemove_1 = new CapabilityKey('wechat.filesystem-remove');
    this.FileSystemSandboxPath_1 = new CapabilityKey('wechat.filesystem-sandbox-path');
  }
  var WeChatDeviceCapabilities_instance;
  function WeChatDeviceCapabilities_getInstance() {
    if (WeChatDeviceCapabilities_instance == null)
      new WeChatDeviceCapabilities();
    return WeChatDeviceCapabilities_instance;
  }
  function WeChatLoginResult(code) {
    this.code = code;
  }
  protoOf(WeChatLoginResult).get_code_wok7xy_k$ = function () {
    return this.code;
  };
  var static_init_called_2;
  function static_init_2() {
    if (static_init_called_2)
      return Unit_instance;
    static_init_called_2 = true;
    WeChatSessionState_VALID_instance = new WeChatSessionState('VALID', 0);
    WeChatSessionState_INVALID_instance = new WeChatSessionState('INVALID', 1);
    if (Companion_instance_8 == null) {
      Companion_instance_0;
      new Companion_6();
    }
  }
  var WeChatSessionState_VALID_instance;
  var WeChatSessionState_INVALID_instance;
  function Companion_6() {
    Companion_instance_8 = this;
    this.Key_1 = new CapabilityKey('wechat.check-session');
  }
  var Companion_instance_8;
  function Companion_getInstance_6() {
    static_init_2();
    return Companion_instance_8;
  }
  function WeChatSessionState(name, ordinal) {
    Enum.call(this, name, ordinal);
  }
  function WeChatSessionState_VALID_getInstance() {
    static_init_2();
    return WeChatSessionState_VALID_instance;
  }
  function WeChatSessionState_INVALID_getInstance() {
    static_init_2();
    return WeChatSessionState_INVALID_instance;
  }
  function WechatPlatformApi(auth, navigation, appLifecycle, pageLifecycle, runtimeInfo, clipboard, haptics, fileSystem) {
    this.auth_1 = auth;
    this.navigation_1 = navigation;
    this.appLifecycle_1 = appLifecycle;
    this.pageLifecycle_1 = pageLifecycle;
    this.runtimeInfo_1 = runtimeInfo;
    this.clipboard_1 = clipboard;
    this.haptics_1 = haptics;
    this.fileSystem_1 = fileSystem;
  }
  function WechatHost(storageHost, authHost, networkHost, navigationHost, runtimeInfoHost, permissionHost, privacyHost, clipboardHost, hapticsHost, fileSystemHost) {
    storageHost = storageHost === VOID ? WxStorageHost_instance : storageHost;
    authHost = authHost === VOID ? WxAuthHost_instance : authHost;
    networkHost = networkHost === VOID ? WxNetworkHost_instance : networkHost;
    navigationHost = navigationHost === VOID ? WxNavigationHost_instance : navigationHost;
    runtimeInfoHost = runtimeInfoHost === VOID ? WxRuntimeInfoHost_instance : runtimeInfoHost;
    permissionHost = permissionHost === VOID ? WxPermissionHost_instance : permissionHost;
    privacyHost = privacyHost === VOID ? WxPrivacyHost_instance : privacyHost;
    clipboardHost = clipboardHost === VOID ? WxClipboardHost_instance : clipboardHost;
    hapticsHost = hapticsHost === VOID ? WxHapticsHost_instance : hapticsHost;
    fileSystemHost = fileSystemHost === VOID ? WxFileSystemHost_instance : fileSystemHost;
    this.appLifecycle_1 = new WechatAppLifecycle();
    this.runtimeInfo_1 = new WechatRuntimeInfo(runtimeInfoHost);
    this.capabilityGate_1 = new WechatCapabilityGate(this.runtimeInfo_1, runtimeInfoHost);
    this.platform_1 = new WechatPlatformApi(new WechatAuth(authHost), new WechatNavigation(navigationHost), this.appLifecycle_1, new WechatPageLifecycle(), this.runtimeInfo_1, new WechatClipboard(clipboardHost), new WechatHaptics(hapticsHost), new WechatFileSystem(fileSystemHost));
    this.storage_1 = new WechatStorage(storageHost);
    this.network_1 = new WechatNetwork(networkHost);
    this.lifecycle_1 = this.appLifecycle_1;
    this.permissions_1 = new WechatPermissions(permissionHost);
    this.privacy_1 = new WechatPrivacy(privacyHost);
  }
  protoOf(WechatHost).capabilitySupport_p8flwh_k$ = function (capability) {
    return this.capabilityGate_1.supportFor_tmznfg_k$(capability);
  };
  function WechatAuth$login$lambda$lambda($failure, $success) {
    return function (result) {
      var tmp;
      if (isBlank(result.code)) {
        tmp = $failure(new InvalidResponse('WeChat login succeeded without a usable code'));
      } else {
        tmp = $success(new WeChatLoginResult(result.code));
      }
      return Unit_instance;
    };
  }
  function WechatAuth$login$lambda$lambda_0($failure) {
    return function (result) {
      var tmp0_safe_receiver = result.errno;
      $failure(mapWechatHostFailure('login', result, tmp0_safe_receiver == null ? null : tmp0_safe_receiver.toString()));
      return Unit_instance;
    };
  }
  function WechatAuth$login$lambda(this$0) {
    return function (success, failure) {
      var tmp = WechatAuth$login$lambda$lambda(failure, success);
      this$0.host_1.login_mku97k_k$(tmp, WechatAuth$login$lambda$lambda_0(failure));
      return null;
    };
  }
  function WechatAuth$checkSession$lambda$lambda($success) {
    return function () {
      $success(WeChatSessionState_VALID_getInstance());
      return Unit_instance;
    };
  }
  function WechatAuth$checkSession$lambda$lambda_0($success) {
    return function (it) {
      $success(WeChatSessionState_INVALID_getInstance());
      return Unit_instance;
    };
  }
  function WechatAuth$checkSession$lambda(this$0) {
    return function (success, failure) {
      var tmp = WechatAuth$checkSession$lambda$lambda(success);
      this$0.host_1.checkSession_v3h6rr_k$(tmp, WechatAuth$checkSession$lambda$lambda_0(success));
      return null;
    };
  }
  function WechatAuth(host) {
    host = host === VOID ? WxAuthHost_instance : host;
    this.host_1 = host;
  }
  protoOf(WechatAuth).login_hvnjm8_k$ = function ($completion) {
    return awaitHostCallback(WechatAuth$login$lambda(this), $completion);
  };
  protoOf(WechatAuth).checkSession_drbyba_k$ = function ($completion) {
    if (!this.host_1.isSessionCheckSupported_ka7vpq_k$()) {
      throw new UnsupportedCapability(Companion_getInstance_6().Key_1);
    }
    return awaitHostCallback(WechatAuth$checkSession$lambda(this), $completion);
  };
  function WxAuthHost$checkSession$lambda($success) {
    return function (it) {
      $success();
      return Unit_instance;
    };
  }
  function WxAuthHost() {
  }
  protoOf(WxAuthHost).login_mku97k_k$ = function (success, failure) {
    var options = wxLoginOptions();
    options.success = success;
    options.fail = failure;
    wx.login(options);
  };
  protoOf(WxAuthHost).isSessionCheckSupported_ka7vpq_k$ = function () {
    return hasWxCheckSession();
  };
  protoOf(WxAuthHost).checkSession_v3h6rr_k$ = function (success, failure) {
    var options = wxCheckSessionOptions();
    options.success = WxAuthHost$checkSession$lambda(success);
    options.fail = failure;
    wx.checkSession(options);
  };
  var WxAuthHost_instance;
  function WxAuthHost_getInstance() {
    return WxAuthHost_instance;
  }
  function requireReadSupported($this) {
    if (!$this.host_1.isReadSupported_3yqram_k$()) {
      throw new UnsupportedCapability(WeChatDeviceCapabilities_getInstance().ClipboardRead_1);
    }
  }
  function requireWriteSupported($this) {
    if (!$this.host_1.isWriteSupported_mpjazd_k$()) {
      throw new UnsupportedCapability(WeChatDeviceCapabilities_getInstance().ClipboardWrite_1);
    }
  }
  function WechatClipboard$readText$lambda$lambda($success) {
    return function (result) {
      $success(wxClipboardText(result));
      return Unit_instance;
    };
  }
  function WechatClipboard$readText$lambda$lambda_0($failure) {
    return function (result) {
      $failure(mapWechatHostFailure('getClipboardData', result));
      return Unit_instance;
    };
  }
  function WechatClipboard$readText$lambda(this$0) {
    return function (success, failure) {
      var tmp = WechatClipboard$readText$lambda$lambda(success);
      this$0.host_1.read_e6b6c_k$(tmp, WechatClipboard$readText$lambda$lambda_0(failure));
      return null;
    };
  }
  function WechatClipboard$writeText$lambda$lambda($success) {
    return function () {
      $success(Unit_instance);
      return Unit_instance;
    };
  }
  function WechatClipboard$writeText$lambda$lambda_0($failure) {
    return function (result) {
      $failure(mapWechatHostFailure('setClipboardData', result));
      return Unit_instance;
    };
  }
  function WechatClipboard$writeText$lambda(this$0, $data) {
    return function (success, failure) {
      var tmp = WechatClipboard$writeText$lambda$lambda(success);
      this$0.host_1.write_yfy6my_k$($data, tmp, WechatClipboard$writeText$lambda$lambda_0(failure));
      return null;
    };
  }
  function $readTextCOROUTINE$(_this__u8e3s4, resultContinuation) {
    CoroutineImpl.call(this, resultContinuation);
    this._this__u8e3s4__1 = _this__u8e3s4;
  }
  protoOf($readTextCOROUTINE$).doResume_5yljmg_k$ = function () {
    var suspendResult = this.result_1;
    $sm: do
      try {
        var tmp = this.state_1;
        switch (tmp) {
          case 0:
            this.exceptionState_1 = 2;
            requireReadSupported(this._this__u8e3s4__1);
            this.state_1 = 1;
            suspendResult = awaitHostCallback(WechatClipboard$readText$lambda(this._this__u8e3s4__1), this);
            if (suspendResult === get_COROUTINE_SUSPENDED()) {
              return suspendResult;
            }

            continue $sm;
          case 1:
            var text = suspendResult;
            var tmp_0;
            if (text instanceof Present) {
              tmp_0 = text.text_1;
            } else {
              if (equals(text, Unreadable_instance)) {
                throw new InvalidResponse('The WeChat host answered getClipboardData with a value the SDK cannot read');
              } else {
                noWhenBranchMatchedException();
              }
            }

            return tmp_0;
          case 2:
            throw this.exception_1;
        }
      } catch ($p) {
        var e = $p;
        if (this.exceptionState_1 === 2) {
          throw e;
        } else {
          this.state_1 = this.exceptionState_1;
          this.exception_1 = e;
        }
      }
     while (true);
  };
  function WechatClipboard(host) {
    host = host === VOID ? WxClipboardHost_instance : host;
    this.host_1 = host;
  }
  protoOf(WechatClipboard).readText_iovdk0_k$ = function ($completion) {
    var tmp = new $readTextCOROUTINE$(this, $completion);
    tmp.result_1 = Unit_instance;
    tmp.exception_1 = null;
    return tmp.doResume_5yljmg_k$();
  };
  protoOf(WechatClipboard).writeText_hlinex_k$ = function (data, $completion) {
    requireWriteSupported(this);
    return awaitHostCallback(WechatClipboard$writeText$lambda(this, data), $completion);
  };
  function WxClipboardHost$write$lambda($success) {
    return function (it) {
      $success();
      return Unit_instance;
    };
  }
  function WxClipboardHost() {
  }
  protoOf(WxClipboardHost).isReadSupported_3yqram_k$ = function () {
    return hasWxGetClipboardData();
  };
  protoOf(WxClipboardHost).isWriteSupported_mpjazd_k$ = function () {
    return hasWxSetClipboardData();
  };
  protoOf(WxClipboardHost).read_e6b6c_k$ = function (success, failure) {
    var options = wxGetClipboardDataOptions();
    options.success = success;
    options.fail = failure;
    wx.getClipboardData(options);
  };
  protoOf(WxClipboardHost).write_yfy6my_k$ = function (data, success, failure) {
    var options = wxSetClipboardDataOptions(data);
    options.success = WxClipboardHost$write$lambda(success);
    options.fail = failure;
    wx.setClipboardData(options);
  };
  var WxClipboardHost_instance;
  function WxClipboardHost_getInstance() {
    return WxClipboardHost_instance;
  }
  function mapWechatHostFailure(operation, result, code) {
    code = code === VOID ? null : code;
    return new HostFailure('wechat', code, result.errMsg, mapOf(to('operation', operation)));
  }
  function mapWechatRequestFailure(result) {
    var tmp;
    if (contains(result.errMsg, 'timeout', true)) {
      tmp = new Timeout('request', result.errMsg);
    } else {
      var tmp0_safe_receiver = result.errno;
      tmp = mapWechatHostFailure('request', result, tmp0_safe_receiver == null ? null : tmp0_safe_receiver.toString());
    }
    return tmp;
  }
  function mapWechatAuthorizeFailure(permission, result) {
    var tmp;
    if (contains(result.errMsg, 'auth deny', true)) {
      tmp = new PermissionDenied(permission.value_1, result.errMsg);
    } else {
      tmp = mapWechatHostFailure('authorize', result);
    }
    return tmp;
  }
  function Refused_0() {
  }
  protoOf(Refused_0).toString = function () {
    return 'Refused';
  };
  protoOf(Refused_0).hashCode = function () {
    return 1104928719;
  };
  protoOf(Refused_0).equals = function (other) {
    if (this === other)
      return true;
    if (!(other instanceof Refused_0))
      return false;
    return true;
  };
  var Refused_instance_0;
  function Refused_getInstance_0() {
    return Refused_instance_0;
  }
  function Failed(error) {
    this.error_1 = error;
  }
  protoOf(Failed).toString = function () {
    return 'Failed(error=' + this.error_1.toString() + ')';
  };
  protoOf(Failed).hashCode = function () {
    return hashCode(this.error_1);
  };
  protoOf(Failed).equals = function (other) {
    if (this === other)
      return true;
    if (!(other instanceof Failed))
      return false;
    if (!equals(this.error_1, other.error_1))
      return false;
    return true;
  };
  function mapWechatPrivacyAuthorizeFailure(result) {
    var tmp;
    if (contains(result.errMsg, 'privacy permission is not authorized', true)) {
      tmp = Refused_instance_0;
    } else {
      tmp = new Failed(mapWechatHostFailure('requirePrivacyAuthorize', result));
    }
    return tmp;
  }
  function NotFound() {
  }
  protoOf(NotFound).toString = function () {
    return 'NotFound';
  };
  protoOf(NotFound).hashCode = function () {
    return 1443261914;
  };
  protoOf(NotFound).equals = function (other) {
    if (this === other)
      return true;
    if (!(other instanceof NotFound))
      return false;
    return true;
  };
  var NotFound_instance;
  function NotFound_getInstance() {
    return NotFound_instance;
  }
  function Failed_0(error) {
    this.error_1 = error;
  }
  protoOf(Failed_0).toString = function () {
    return 'Failed(error=' + this.error_1.toString() + ')';
  };
  protoOf(Failed_0).hashCode = function () {
    return hashCode(this.error_1);
  };
  protoOf(Failed_0).equals = function (other) {
    if (this === other)
      return true;
    if (!(other instanceof Failed_0))
      return false;
    if (!equals(this.error_1, other.error_1))
      return false;
    return true;
  };
  function mapWechatFileSystemFailure(operation, result) {
    var tmp;
    if (contains(result.errMsg, 'no such file or directory', true)) {
      tmp = NotFound_instance;
    } else {
      tmp = new Failed_0(mapWechatHostFailure(operation, result));
    }
    return tmp;
  }
  function requireSupported_0($this, capability, isSupported) {
    if (!isSupported()) {
      throw new UnsupportedCapability(capability);
    }
  }
  function WechatFileSystem$readText$lambda$lambda($success) {
    return function (result) {
      $success(wxFileText(result));
      return Unit_instance;
    };
  }
  function WechatFileSystem$readText$lambda$lambda_0($failure) {
    return function (result) {
      $failure(mapWechatHostFailure('readFile', result));
      return Unit_instance;
    };
  }
  function WechatFileSystem$readText$lambda(this$0, $filePath) {
    return function (success, failure) {
      var tmp = WechatFileSystem$readText$lambda$lambda(success);
      this$0.host_1.read_2am2gi_k$($filePath, tmp, WechatFileSystem$readText$lambda$lambda_0(failure));
      return null;
    };
  }
  function WechatFileSystem$writeText$lambda$lambda($success) {
    return function () {
      $success(Unit_instance);
      return Unit_instance;
    };
  }
  function WechatFileSystem$writeText$lambda$lambda_0($failure) {
    return function (result) {
      $failure(mapWechatHostFailure('writeFile', result));
      return Unit_instance;
    };
  }
  function WechatFileSystem$writeText$lambda(this$0, $filePath, $content) {
    return function (success, failure) {
      var tmp = WechatFileSystem$writeText$lambda$lambda(success);
      this$0.host_1.write_g9f1ca_k$($filePath, $content, tmp, WechatFileSystem$writeText$lambda$lambda_0(failure));
      return null;
    };
  }
  function WechatFileSystem$exists$lambda$lambda($success) {
    return function () {
      $success(true);
      return Unit_instance;
    };
  }
  function WechatFileSystem$exists$lambda$lambda_0($success, $failure) {
    return function (result) {
      var classified = mapWechatFileSystemFailure('access', result);
      var tmp;
      if (equals(classified, NotFound_instance)) {
        tmp = $success(false);
      } else {
        if (classified instanceof Failed_0) {
          tmp = $failure(classified.error_1);
        } else {
          noWhenBranchMatchedException();
        }
      }
      return Unit_instance;
    };
  }
  function WechatFileSystem$exists$lambda(this$0, $path) {
    return function (success, failure) {
      var tmp = WechatFileSystem$exists$lambda$lambda(success);
      this$0.host_1.access_kyhvvl_k$($path, tmp, WechatFileSystem$exists$lambda$lambda_0(success, failure));
      return null;
    };
  }
  function WechatFileSystem$remove$lambda$lambda($success) {
    return function () {
      $success(Unit_instance);
      return Unit_instance;
    };
  }
  function WechatFileSystem$remove$lambda$lambda_0($failure) {
    return function (result) {
      $failure(mapWechatHostFailure('unlink', result));
      return Unit_instance;
    };
  }
  function WechatFileSystem$remove$lambda(this$0, $filePath) {
    return function (success, failure) {
      var tmp = WechatFileSystem$remove$lambda$lambda(success);
      this$0.host_1.unlink_oo1n2_k$($filePath, tmp, WechatFileSystem$remove$lambda$lambda_0(failure));
      return null;
    };
  }
  function $readTextCOROUTINE$_0(_this__u8e3s4, filePath, resultContinuation) {
    CoroutineImpl.call(this, resultContinuation);
    this._this__u8e3s4__1 = _this__u8e3s4;
    this.filePath_1 = filePath;
  }
  protoOf($readTextCOROUTINE$_0).doResume_5yljmg_k$ = function () {
    var suspendResult = this.result_1;
    $sm: do
      try {
        var tmp = this.state_1;
        switch (tmp) {
          case 0:
            this.exceptionState_1 = 2;
            var tmp_0 = WeChatDeviceCapabilities_getInstance().FileSystemRead_1;
            requireSupported_0(this._this__u8e3s4__1, tmp_0, WechatFileSystemHost$isReadSupported$ref(this._this__u8e3s4__1.host_1));
            this.state_1 = 1;
            suspendResult = awaitHostCallback(WechatFileSystem$readText$lambda(this._this__u8e3s4__1, this.filePath_1), this);
            if (suspendResult === get_COROUTINE_SUSPENDED()) {
              return suspendResult;
            }

            continue $sm;
          case 1:
            var text = suspendResult;
            var tmp_1;
            if (text instanceof Present_0) {
              tmp_1 = text.text_1;
            } else {
              if (equals(text, Unreadable_instance_0)) {
                throw new InvalidResponse('The WeChat host answered readFile with a value the SDK cannot read as text');
              } else {
                noWhenBranchMatchedException();
              }
            }

            return tmp_1;
          case 2:
            throw this.exception_1;
        }
      } catch ($p) {
        var e = $p;
        if (this.exceptionState_1 === 2) {
          throw e;
        } else {
          this.state_1 = this.exceptionState_1;
          this.exception_1 = e;
        }
      }
     while (true);
  };
  function $existsCOROUTINE$(_this__u8e3s4, path, resultContinuation) {
    CoroutineImpl.call(this, resultContinuation);
    this._this__u8e3s4__1 = _this__u8e3s4;
    this.path_1 = path;
  }
  protoOf($existsCOROUTINE$).doResume_5yljmg_k$ = function () {
    var suspendResult = this.result_1;
    $sm: do
      try {
        var tmp = this.state_1;
        switch (tmp) {
          case 0:
            this.exceptionState_1 = 2;
            var tmp_0 = WeChatDeviceCapabilities_getInstance().FileSystemAccess_1;
            requireSupported_0(this._this__u8e3s4__1, tmp_0, WechatFileSystemHost$isAccessSupported$ref(this._this__u8e3s4__1.host_1));
            this.state_1 = 1;
            suspendResult = awaitHostCallback(WechatFileSystem$exists$lambda(this._this__u8e3s4__1, this.path_1), this);
            if (suspendResult === get_COROUTINE_SUSPENDED()) {
              return suspendResult;
            }

            continue $sm;
          case 1:
            var outcome = suspendResult;
            return outcome;
          case 2:
            throw this.exception_1;
        }
      } catch ($p) {
        var e = $p;
        if (this.exceptionState_1 === 2) {
          throw e;
        } else {
          this.state_1 = this.exceptionState_1;
          this.exception_1 = e;
        }
      }
     while (true);
  };
  function WechatFileSystem(host) {
    host = host === VOID ? WxFileSystemHost_instance : host;
    this.host_1 = host;
  }
  protoOf(WechatFileSystem).userDataPath_gvvkd6_k$ = function () {
    var tmp0_elvis_lhs = this.host_1.userDataPath_gvvkd6_k$();
    var tmp;
    if (tmp0_elvis_lhs == null) {
      throw new UnsupportedCapability(WeChatDeviceCapabilities_getInstance().FileSystemSandboxPath_1);
    } else {
      tmp = tmp0_elvis_lhs;
    }
    return tmp;
  };
  protoOf(WechatFileSystem).readText_vgeqrw_k$ = function (filePath, $completion) {
    var tmp = new $readTextCOROUTINE$_0(this, filePath, $completion);
    tmp.result_1 = Unit_instance;
    tmp.exception_1 = null;
    return tmp.doResume_5yljmg_k$();
  };
  protoOf(WechatFileSystem).writeText_bjaevp_k$ = function (filePath, content, $completion) {
    var tmp = WeChatDeviceCapabilities_getInstance().FileSystemWrite_1;
    requireSupported_0(this, tmp, WechatFileSystemHost$isWriteSupported$ref(this.host_1));
    return awaitHostCallback(WechatFileSystem$writeText$lambda(this, filePath, content), $completion);
  };
  protoOf(WechatFileSystem).exists_77rlgi_k$ = function (path, $completion) {
    var tmp = new $existsCOROUTINE$(this, path, $completion);
    tmp.result_1 = Unit_instance;
    tmp.exception_1 = null;
    return tmp.doResume_5yljmg_k$();
  };
  protoOf(WechatFileSystem).remove_gwf6lb_k$ = function (filePath, $completion) {
    var tmp = WeChatDeviceCapabilities_getInstance().FileSystemRemove_1;
    requireSupported_0(this, tmp, WechatFileSystemHost$isRemoveSupported$ref(this.host_1));
    return awaitHostCallback(WechatFileSystem$remove$lambda(this, filePath), $completion);
  };
  function WxFileSystemHost$write$lambda($success) {
    return function (it) {
      $success();
      return Unit_instance;
    };
  }
  function WxFileSystemHost$access$lambda($success) {
    return function (it) {
      $success();
      return Unit_instance;
    };
  }
  function WxFileSystemHost$unlink$lambda($success) {
    return function (it) {
      $success();
      return Unit_instance;
    };
  }
  function WxFileSystemHost() {
  }
  protoOf(WxFileSystemHost).isReadSupported_3yqram_k$ = function () {
    return hasWxFileSystemMethod('readFile');
  };
  protoOf(WxFileSystemHost).isWriteSupported_mpjazd_k$ = function () {
    return hasWxFileSystemMethod('writeFile');
  };
  protoOf(WxFileSystemHost).isAccessSupported_4lja3k_k$ = function () {
    return hasWxFileSystemMethod('access');
  };
  protoOf(WxFileSystemHost).isRemoveSupported_ngivwg_k$ = function () {
    return hasWxFileSystemMethod('unlink');
  };
  protoOf(WxFileSystemHost).userDataPath_gvvkd6_k$ = function () {
    if (!hasWxUserDataPath())
      return null;
    var tmp0_safe_receiver = wx.env;
    var path = tmp0_safe_receiver == null ? null : tmp0_safe_receiver.USER_DATA_PATH;
    return (!(path == null) ? typeof path === 'string' : false) ? path : null;
  };
  protoOf(WxFileSystemHost).read_2am2gi_k$ = function (filePath, success, failure) {
    var options = wxReadFileOptions(filePath);
    options.success = success;
    options.fail = failure;
    var tmp0_safe_receiver = wx.getFileSystemManager().readFile;
    if (tmp0_safe_receiver == null)
      null;
    else
      tmp0_safe_receiver(options);
  };
  protoOf(WxFileSystemHost).write_g9f1ca_k$ = function (filePath, data, success, failure) {
    var options = wxWriteFileOptions(filePath, data);
    options.success = WxFileSystemHost$write$lambda(success);
    options.fail = failure;
    var tmp0_safe_receiver = wx.getFileSystemManager().writeFile;
    if (tmp0_safe_receiver == null)
      null;
    else
      tmp0_safe_receiver(options);
  };
  protoOf(WxFileSystemHost).access_kyhvvl_k$ = function (path, success, failure) {
    var options = wxAccessOptions(path);
    options.success = WxFileSystemHost$access$lambda(success);
    options.fail = failure;
    var tmp0_safe_receiver = wx.getFileSystemManager().access;
    if (tmp0_safe_receiver == null)
      null;
    else
      tmp0_safe_receiver(options);
  };
  protoOf(WxFileSystemHost).unlink_oo1n2_k$ = function (filePath, success, failure) {
    var options = wxUnlinkOptions(filePath);
    options.success = WxFileSystemHost$unlink$lambda(success);
    options.fail = failure;
    var tmp0_safe_receiver = wx.getFileSystemManager().unlink;
    if (tmp0_safe_receiver == null)
      null;
    else
      tmp0_safe_receiver(options);
  };
  var WxFileSystemHost_instance;
  function WxFileSystemHost_getInstance() {
    return WxFileSystemHost_instance;
  }
  function WechatFileSystemHost$isReadSupported$ref(p0) {
    return constructCallableReference(function () {
      return p0.isReadSupported_3yqram_k$();
    }, 0, 0, 1, 'isReadSupported', [p0]);
  }
  function WechatFileSystemHost$isWriteSupported$ref(p0) {
    return constructCallableReference(function () {
      return p0.isWriteSupported_mpjazd_k$();
    }, 0, 0, 2, 'isWriteSupported', [p0]);
  }
  function WechatFileSystemHost$isAccessSupported$ref(p0) {
    return constructCallableReference(function () {
      return p0.isAccessSupported_4lja3k_k$();
    }, 0, 0, 3, 'isAccessSupported', [p0]);
  }
  function WechatFileSystemHost$isRemoveSupported$ref(p0) {
    return constructCallableReference(function () {
      return p0.isRemoveSupported_ngivwg_k$();
    }, 0, 0, 4, 'isRemoveSupported', [p0]);
  }
  function requireShortSupported($this) {
    if (!$this.host_1.isShortSupported_dw0eck_k$()) {
      throw new UnsupportedCapability(WeChatDeviceCapabilities_getInstance().VibrateShort_1);
    }
  }
  function requireLongSupported($this) {
    if (!$this.host_1.isLongSupported_dhs5mw_k$()) {
      throw new UnsupportedCapability(WeChatDeviceCapabilities_getInstance().VibrateLong_1);
    }
  }
  function WechatHaptics$vibrateShort$lambda$lambda($success) {
    return function () {
      $success(Unit_instance);
      return Unit_instance;
    };
  }
  function WechatHaptics$vibrateShort$lambda$lambda_0($failure) {
    return function (result) {
      $failure(mapWechatHostFailure('vibrateShort', result));
      return Unit_instance;
    };
  }
  function WechatHaptics$vibrateShort$lambda(this$0) {
    return function (success, failure) {
      var tmp = WechatHaptics$vibrateShort$lambda$lambda(success);
      this$0.host_1.vibrateShort_mp2zw8_k$(tmp, WechatHaptics$vibrateShort$lambda$lambda_0(failure));
      return null;
    };
  }
  function WechatHaptics$vibrateLong$lambda$lambda($success) {
    return function () {
      $success(Unit_instance);
      return Unit_instance;
    };
  }
  function WechatHaptics$vibrateLong$lambda$lambda_0($failure) {
    return function (result) {
      $failure(mapWechatHostFailure('vibrateLong', result));
      return Unit_instance;
    };
  }
  function WechatHaptics$vibrateLong$lambda(this$0) {
    return function (success, failure) {
      var tmp = WechatHaptics$vibrateLong$lambda$lambda(success);
      this$0.host_1.vibrateLong_4cnbcm_k$(tmp, WechatHaptics$vibrateLong$lambda$lambda_0(failure));
      return null;
    };
  }
  function WechatHaptics(host) {
    host = host === VOID ? WxHapticsHost_instance : host;
    this.host_1 = host;
  }
  protoOf(WechatHaptics).vibrateShort_ict1au_k$ = function ($completion) {
    requireShortSupported(this);
    return awaitHostCallback(WechatHaptics$vibrateShort$lambda(this), $completion);
  };
  protoOf(WechatHaptics).vibrateLong_7v53xo_k$ = function ($completion) {
    requireLongSupported(this);
    return awaitHostCallback(WechatHaptics$vibrateLong$lambda(this), $completion);
  };
  function WxHapticsHost$vibrateShort$lambda($success) {
    return function (it) {
      $success();
      return Unit_instance;
    };
  }
  function WxHapticsHost$vibrateLong$lambda($success) {
    return function (it) {
      $success();
      return Unit_instance;
    };
  }
  function WxHapticsHost() {
  }
  protoOf(WxHapticsHost).isShortSupported_dw0eck_k$ = function () {
    return hasWxVibrateShort();
  };
  protoOf(WxHapticsHost).isLongSupported_dhs5mw_k$ = function () {
    return hasWxVibrateLong();
  };
  protoOf(WxHapticsHost).vibrateShort_mp2zw8_k$ = function (success, failure) {
    var options = wxVibrateOptions();
    options.success = WxHapticsHost$vibrateShort$lambda(success);
    options.fail = failure;
    wx.vibrateShort(options);
  };
  protoOf(WxHapticsHost).vibrateLong_4cnbcm_k$ = function (success, failure) {
    var options = wxVibrateOptions();
    options.success = WxHapticsHost$vibrateLong$lambda(success);
    options.fail = failure;
    wx.vibrateLong(options);
  };
  var WxHapticsHost_instance;
  function WxHapticsHost_getInstance() {
    return WxHapticsHost_instance;
  }
  function WechatNavigation$navigateTo$lambda$lambda($success) {
    return function () {
      $success(Unit_instance);
      return Unit_instance;
    };
  }
  function WechatNavigation$navigateTo$lambda$lambda_0($failure) {
    return function (result) {
      $failure(mapWechatHostFailure('navigateTo', result));
      return Unit_instance;
    };
  }
  function WechatNavigation$navigateTo$lambda(this$0, $url) {
    return function (success, failure) {
      var tmp = WechatNavigation$navigateTo$lambda$lambda(success);
      this$0.host_1.navigateTo_4j3v0n_k$($url, tmp, WechatNavigation$navigateTo$lambda$lambda_0(failure));
      return null;
    };
  }
  function WechatNavigation$redirectTo$lambda$lambda($success) {
    return function () {
      $success(Unit_instance);
      return Unit_instance;
    };
  }
  function WechatNavigation$redirectTo$lambda$lambda_0($failure) {
    return function (result) {
      $failure(mapWechatHostFailure('redirectTo', result));
      return Unit_instance;
    };
  }
  function WechatNavigation$redirectTo$lambda(this$0, $url) {
    return function (success, failure) {
      var tmp = WechatNavigation$redirectTo$lambda$lambda(success);
      this$0.host_1.redirectTo_jwivri_k$($url, tmp, WechatNavigation$redirectTo$lambda$lambda_0(failure));
      return null;
    };
  }
  function WechatNavigation$navigateBack$lambda$lambda($success) {
    return function () {
      $success(Unit_instance);
      return Unit_instance;
    };
  }
  function WechatNavigation$navigateBack$lambda$lambda_0($failure) {
    return function (result) {
      $failure(mapWechatHostFailure('navigateBack', result));
      return Unit_instance;
    };
  }
  function WechatNavigation$navigateBack$lambda(this$0, $delta) {
    return function (success, failure) {
      var tmp = WechatNavigation$navigateBack$lambda$lambda(success);
      this$0.host_1.navigateBack_7xozxk_k$($delta, tmp, WechatNavigation$navigateBack$lambda$lambda_0(failure));
      return null;
    };
  }
  function WechatNavigation(host) {
    host = host === VOID ? WxNavigationHost_instance : host;
    this.host_1 = host;
  }
  protoOf(WechatNavigation).navigateTo_na3mc7_k$ = function (url, $completion) {
    return awaitHostCallback(WechatNavigation$navigateTo$lambda(this, url), $completion);
  };
  protoOf(WechatNavigation).redirectTo_lk2vx0_k$ = function (url, $completion) {
    return awaitHostCallback(WechatNavigation$redirectTo$lambda(this, url), $completion);
  };
  protoOf(WechatNavigation).navigateBack_hskp56_k$ = function (delta, $completion) {
    return awaitHostCallback(WechatNavigation$navigateBack$lambda(this, delta), $completion);
  };
  function WxNavigationHost$navigateTo$lambda($success) {
    return function (it) {
      $success();
      return Unit_instance;
    };
  }
  function WxNavigationHost$redirectTo$lambda($success) {
    return function (it) {
      $success();
      return Unit_instance;
    };
  }
  function WxNavigationHost$navigateBack$lambda($success) {
    return function (it) {
      $success();
      return Unit_instance;
    };
  }
  function WxNavigationHost() {
  }
  protoOf(WxNavigationHost).navigateTo_4j3v0n_k$ = function (url, success, failure) {
    var options = wxNavigateToOptions(url);
    options.success = WxNavigationHost$navigateTo$lambda(success);
    options.fail = failure;
    wx.navigateTo(options);
  };
  protoOf(WxNavigationHost).redirectTo_jwivri_k$ = function (url, success, failure) {
    var options = wxRedirectToOptions(url);
    options.success = WxNavigationHost$redirectTo$lambda(success);
    options.fail = failure;
    wx.redirectTo(options);
  };
  protoOf(WxNavigationHost).navigateBack_7xozxk_k$ = function (delta, success, failure) {
    var options = wxNavigateBackOptions(delta);
    options.success = WxNavigationHost$navigateBack$lambda(success);
    options.fail = failure;
    wx.navigateBack(options);
  };
  var WxNavigationHost_instance;
  function WxNavigationHost_getInstance() {
    return WxNavigationHost_instance;
  }
  function sam$io_github_bobcgn_miniapp_async_HostOperationAborter$0(function_0) {
    this.function_1 = function_0;
  }
  protoOf(sam$io_github_bobcgn_miniapp_async_HostOperationAborter$0).abort_lahfmo_k$ = function () {
    return this.function_1();
  };
  protoOf(sam$io_github_bobcgn_miniapp_async_HostOperationAborter$0).getFunctionDelegate_jtodtf_k$ = function () {
    return this.function_1;
  };
  protoOf(sam$io_github_bobcgn_miniapp_async_HostOperationAborter$0).equals = function (other) {
    var tmp;
    if (!(other == null) ? isInterface(other, HostOperationAborter) : false) {
      var tmp_0;
      if (!(other == null) ? isInterface(other, FunctionAdapter) : false) {
        tmp_0 = equals(this.getFunctionDelegate_jtodtf_k$(), other.getFunctionDelegate_jtodtf_k$());
      } else {
        tmp_0 = false;
      }
      tmp = tmp_0;
    } else {
      tmp = false;
    }
    return tmp;
  };
  protoOf(sam$io_github_bobcgn_miniapp_async_HostOperationAborter$0).hashCode = function () {
    return hashCode(this.getFunctionDelegate_jtodtf_k$());
  };
  function WechatNetwork$request$lambda$lambda($success, $failure, $request) {
    return function (result) {
      var body = result.data;
      var tmp;
      if (!(body == null) ? typeof body === 'string' : false) {
        tmp = $success(new MiniAppHttpResponse(result.statusCode, wxResponseHeaders(result.header), body));
      } else {
        tmp = $failure(new InvalidResponse("WeChat request returned a non-text body for '" + $request.url_1 + "'"));
      }
      return Unit_instance;
    };
  }
  function WechatNetwork$request$lambda$lambda_0($failure) {
    return function (result) {
      $failure(mapWechatRequestFailure(result));
      return Unit_instance;
    };
  }
  function WechatNetwork$request$lambda$lambda_1($task) {
    return function () {
      $task.abort();
      return Unit_instance;
    };
  }
  function WechatNetwork$request$lambda(this$0, $request) {
    return function (success, failure) {
      var tmp = WechatNetwork$request$lambda$lambda(success, failure, $request);
      var task = this$0.host_1.request_pyvxzi_k$($request.url_1, $request.method_1.name_1, $request.headers_1, $request.body_1, $request.timeoutMillis_1, tmp, WechatNetwork$request$lambda$lambda_0(failure));
      var tmp_0;
      if (task == null) {
        tmp_0 = null;
      } else {
        var tmp_1 = WechatNetwork$request$lambda$lambda_1(task);
        tmp_0 = new sam$io_github_bobcgn_miniapp_async_HostOperationAborter$0(tmp_1);
      }
      return tmp_0;
    };
  }
  function WechatNetwork(host) {
    host = host === VOID ? WxNetworkHost_instance : host;
    this.host_1 = host;
  }
  protoOf(WechatNetwork).request_wvdz26_k$ = function (request, $completion) {
    return awaitHostCallback(WechatNetwork$request$lambda(this, request), $completion);
  };
  function WxNetworkHost() {
  }
  protoOf(WxNetworkHost).request_pyvxzi_k$ = function (url, method, headers, body, timeoutMillis, success, failure) {
    var options = wxRequestOptions(url, method, body, timeoutMillis);
    // Inline function 'kotlin.collections.isNotEmpty' call
    if (!headers.isEmpty_y1axqb_k$()) {
      options.header = wxRequestHeader(headers);
    }
    options.success = success;
    options.fail = failure;
    return wx.request(options);
  };
  var WxNetworkHost_instance;
  function WxNetworkHost_getInstance() {
    return WxNetworkHost_instance;
  }
  function WxPermissionHost$authorize$lambda($success) {
    return function (it) {
      $success();
      return Unit_instance;
    };
  }
  function WxPermissionHost() {
  }
  protoOf(WxPermissionHost).getSetting_f4mz5s_k$ = function (success, failure) {
    var options = wxGetSettingOptions();
    options.success = success;
    options.fail = failure;
    wx.getSetting(options);
  };
  protoOf(WxPermissionHost).authorize_2q2wkc_k$ = function (scope, success, failure) {
    var options = wxAuthorizeOptions(scope);
    options.success = WxPermissionHost$authorize$lambda(success);
    options.fail = failure;
    wx.authorize(options);
  };
  protoOf(WxPermissionHost).openSetting_8871zo_k$ = function (success, failure) {
    var options = wxOpenSettingOptions();
    options.success = success;
    options.fail = failure;
    wx.openSetting(options);
  };
  var WxPermissionHost_instance;
  function WxPermissionHost_getInstance() {
    return WxPermissionHost_instance;
  }
  function WechatPermissionScopes() {
    WechatPermissionScopes_instance = this;
    this.scopes_1 = mapOf(to(Companion_getInstance_2().Microphone_1, 'scope.record'));
  }
  protoOf(WechatPermissionScopes).scopeFor_g1473i_k$ = function (permission) {
    var tmp0_elvis_lhs = this.scopes_1.get_wei43m_k$(permission);
    var tmp;
    if (tmp0_elvis_lhs == null) {
      throw IllegalArgumentException_init_$Create$("The WeChat adapter has no scope mapping for permission '" + permission.value_1 + "'");
    } else {
      tmp = tmp0_elvis_lhs;
    }
    return tmp;
  };
  var WechatPermissionScopes_instance;
  function WechatPermissionScopes_getInstance() {
    if (WechatPermissionScopes_instance == null)
      new WechatPermissionScopes();
    return WechatPermissionScopes_instance;
  }
  function WechatPermissionScopes$scopeFor$ref(p0) {
    return constructCallableReference(function (p0_0) {
      return p0.scopeFor_g1473i_k$(p0_0);
    }, 1, 0, 5, 'scopeFor', [p0]);
  }
  function performRequest($this, permission, scope, $completion) {
    var tmp = new $performRequestCOROUTINE$($this, permission, scope, $completion);
    tmp.result_1 = Unit_instance;
    tmp.exception_1 = null;
    return tmp.doResume_5yljmg_k$();
  }
  function readState($this, permission, scope, $completion) {
    var tmp = new $readStateCOROUTINE$($this, permission, scope, $completion);
    tmp.result_1 = Unit_instance;
    tmp.exception_1 = null;
    return tmp.doResume_5yljmg_k$();
  }
  function stateFromEntry($this, entry) {
    var tmp;
    if (equals(entry, Absent_instance)) {
      tmp = NotRequested_instance;
    } else {
      if (equals(entry, Unreadable_instance_1)) {
        tmp = null;
      } else {
        if (entry instanceof Decided) {
          var tmp_0;
          if (entry.granted_1) {
            tmp_0 = Granted_instance;
          } else {
            tmp_0 = Denied_instance;
          }
          tmp = tmp_0;
        } else {
          noWhenBranchMatchedException();
        }
      }
    }
    return tmp;
  }
  function WechatPermissions$request$slambda(this$0, $permission, $scope, resultContinuation) {
    this.this$0__1 = this$0;
    this.$permission_1 = $permission;
    this.$scope_1 = $scope;
    CoroutineImpl.call(this, resultContinuation);
  }
  protoOf(WechatPermissions$request$slambda).invoke_svv044_k$ = function ($this$async, $completion) {
    var tmp = this.create_rcuf4x_k$($this$async, $completion);
    tmp.result_1 = Unit_instance;
    tmp.exception_1 = null;
    return tmp.doResume_5yljmg_k$();
  };
  protoOf(WechatPermissions$request$slambda).invoke_qns8j1_k$ = function (p1, $completion) {
    return this.invoke_svv044_k$((!(p1 == null) ? isInterface(p1, CoroutineScope) : false) ? p1 : THROW_CCE(), $completion);
  };
  protoOf(WechatPermissions$request$slambda).doResume_5yljmg_k$ = function () {
    var suspendResult = this.result_1;
    $sm: do
      try {
        var tmp = this.state_1;
        switch (tmp) {
          case 0:
            this.exceptionState_1 = 2;
            this.state_1 = 1;
            suspendResult = performRequest(this.this$0__1, this.$permission_1, this.$scope_1, this);
            if (suspendResult === get_COROUTINE_SUSPENDED()) {
              return suspendResult;
            }

            continue $sm;
          case 1:
            return suspendResult;
          case 2:
            throw this.exception_1;
        }
      } catch ($p) {
        var e = $p;
        if (this.exceptionState_1 === 2) {
          throw e;
        } else {
          this.state_1 = this.exceptionState_1;
          this.exception_1 = e;
        }
      }
     while (true);
  };
  protoOf(WechatPermissions$request$slambda).create_rcuf4x_k$ = function ($this$async, completion) {
    var i = new WechatPermissions$request$slambda(this.this$0__1, this.$permission_1, this.$scope_1, completion);
    i.$this$async_1 = $this$async;
    return i;
  };
  function WechatPermissions$request$slambda_0(this$0, $permission, $scope, resultContinuation) {
    var i = new WechatPermissions$request$slambda(this$0, $permission, $scope, resultContinuation);
    return constructCallableReference(function ($this$async, $completion) {
      return i.invoke_svv044_k$($this$async, $completion);
    }, 1);
  }
  function WechatPermissions$request$lambda(this$0, $permission) {
    return function (it) {
      this$0.inFlight_1.remove_gppy8k_k$($permission);
      return Unit_instance;
    };
  }
  function WechatPermissions$openSettings$lambda$lambda($success, $scope) {
    return function (result) {
      $success(wxScopeEntry(result.authSetting, $scope));
      return Unit_instance;
    };
  }
  function WechatPermissions$openSettings$lambda$lambda_0($failure) {
    return function (result) {
      $failure(mapWechatHostFailure('openSetting', result));
      return Unit_instance;
    };
  }
  function WechatPermissions$openSettings$lambda(this$0, $scope) {
    return function (success, failure) {
      var tmp = WechatPermissions$openSettings$lambda$lambda(success, $scope);
      this$0.host_1.openSetting_8871zo_k$(tmp, WechatPermissions$openSettings$lambda$lambda_0(failure));
      return null;
    };
  }
  function WechatPermissions$performRequest$lambda$lambda($success) {
    return function () {
      $success(Unit_instance);
      return Unit_instance;
    };
  }
  function WechatPermissions$performRequest$lambda$lambda_0($failure, $permission) {
    return function (result) {
      $failure(mapWechatAuthorizeFailure($permission, result));
      return Unit_instance;
    };
  }
  function WechatPermissions$performRequest$lambda(this$0, $scope, $permission) {
    return function (success, failure) {
      var tmp = WechatPermissions$performRequest$lambda$lambda(success);
      this$0.host_1.authorize_2q2wkc_k$($scope, tmp, WechatPermissions$performRequest$lambda$lambda_0(failure, $permission));
      return null;
    };
  }
  function WechatPermissions$readState$lambda$lambda($success, $scope) {
    return function (result) {
      $success(wxScopeEntry(result.authSetting, $scope));
      return Unit_instance;
    };
  }
  function WechatPermissions$readState$lambda$lambda_0($failure) {
    return function (result) {
      $failure(mapWechatHostFailure('getSetting', result));
      return Unit_instance;
    };
  }
  function WechatPermissions$readState$lambda(this$0, $scope) {
    return function (success, failure) {
      var tmp = WechatPermissions$readState$lambda$lambda(success, $scope);
      this$0.host_1.getSetting_f4mz5s_k$(tmp, WechatPermissions$readState$lambda$lambda_0(failure));
      return null;
    };
  }
  function $openSettingsCOROUTINE$(_this__u8e3s4, permission, resultContinuation) {
    CoroutineImpl.call(this, resultContinuation);
    this._this__u8e3s4__1 = _this__u8e3s4;
    this.permission_1 = permission;
  }
  protoOf($openSettingsCOROUTINE$).doResume_5yljmg_k$ = function () {
    var suspendResult = this.result_1;
    $sm: do
      try {
        var tmp = this.state_1;
        switch (tmp) {
          case 0:
            this.exceptionState_1 = 4;
            this.scope1__1 = this._this__u8e3s4__1.scopeOf_1(this.permission_1);
            this.state_1 = 1;
            suspendResult = awaitHostCallback(WechatPermissions$openSettings$lambda(this._this__u8e3s4__1, this.scope1__1), this);
            if (suspendResult === get_COROUTINE_SUSPENDED()) {
              return suspendResult;
            }

            continue $sm;
          case 1:
            var reported = suspendResult;
            var tmp0_elvis_lhs = stateFromEntry(this._this__u8e3s4__1, reported);
            if (tmp0_elvis_lhs == null) {
              this.state_1 = 2;
              suspendResult = readState(this._this__u8e3s4__1, this.permission_1, this.scope1__1, this);
              if (suspendResult === get_COROUTINE_SUSPENDED()) {
                return suspendResult;
              }
              continue $sm;
            } else {
              this.WHEN_RESULT0__1 = tmp0_elvis_lhs;
              this.state_1 = 3;
              continue $sm;
            }

          case 2:
            this.WHEN_RESULT0__1 = suspendResult;
            this.state_1 = 3;
            continue $sm;
          case 3:
            return this.WHEN_RESULT0__1;
          case 4:
            throw this.exception_1;
        }
      } catch ($p) {
        var e = $p;
        if (this.exceptionState_1 === 4) {
          throw e;
        } else {
          this.state_1 = this.exceptionState_1;
          this.exception_1 = e;
        }
      }
     while (true);
  };
  function $performRequestCOROUTINE$(_this__u8e3s4, permission, scope, resultContinuation) {
    CoroutineImpl.call(this, resultContinuation);
    this._this__u8e3s4__1 = _this__u8e3s4;
    this.permission_1 = permission;
    this.scope_1 = scope;
  }
  protoOf($performRequestCOROUTINE$).doResume_5yljmg_k$ = function () {
    var suspendResult = this.result_1;
    $sm: do
      try {
        var tmp = this.state_1;
        switch (tmp) {
          case 0:
            this.exceptionState_1 = 3;
            this.state_1 = 1;
            suspendResult = readState(this._this__u8e3s4__1, this.permission_1, this.scope_1, this);
            if (suspendResult === get_COROUTINE_SUSPENDED()) {
              return suspendResult;
            }

            continue $sm;
          case 1:
            var current = suspendResult;
            if (equals(current, Granted_instance))
              return current;
            if (equals(current, Denied_instance)) {
              throw new PermissionDenied(this.permission_1.value_1, "The host has already refused permission '" + this.permission_1.value_1 + "'");
            }

            this.state_1 = 2;
            suspendResult = awaitHostCallback(WechatPermissions$performRequest$lambda(this._this__u8e3s4__1, this.scope_1, this.permission_1), this);
            if (suspendResult === get_COROUTINE_SUSPENDED()) {
              return suspendResult;
            }

            continue $sm;
          case 2:
            return Granted_instance;
          case 3:
            throw this.exception_1;
        }
      } catch ($p) {
        var e = $p;
        if (this.exceptionState_1 === 3) {
          throw e;
        } else {
          this.state_1 = this.exceptionState_1;
          this.exception_1 = e;
        }
      }
     while (true);
  };
  function $readStateCOROUTINE$(_this__u8e3s4, permission, scope, resultContinuation) {
    CoroutineImpl.call(this, resultContinuation);
    this._this__u8e3s4__1 = _this__u8e3s4;
    this.permission_1 = permission;
    this.scope_1 = scope;
  }
  protoOf($readStateCOROUTINE$).doResume_5yljmg_k$ = function () {
    var suspendResult = this.result_1;
    $sm: do
      try {
        var tmp = this.state_1;
        switch (tmp) {
          case 0:
            this.exceptionState_1 = 2;
            this.state_1 = 1;
            suspendResult = awaitHostCallback(WechatPermissions$readState$lambda(this._this__u8e3s4__1, this.scope_1), this);
            if (suspendResult === get_COROUTINE_SUSPENDED()) {
              return suspendResult;
            }

            continue $sm;
          case 1:
            var entry = suspendResult;
            var tmp0_elvis_lhs = stateFromEntry(this._this__u8e3s4__1, entry);
            var tmp_0;
            if (tmp0_elvis_lhs == null) {
              throw new InvalidResponse('The WeChat host answered getSetting with a value the SDK cannot read ' + ("for permission '" + this.permission_1.value_1 + "'"));
            } else {
              tmp_0 = tmp0_elvis_lhs;
            }

            return tmp_0;
          case 2:
            throw this.exception_1;
        }
      } catch ($p) {
        var e = $p;
        if (this.exceptionState_1 === 2) {
          throw e;
        } else {
          this.state_1 = this.exceptionState_1;
          this.exception_1 = e;
        }
      }
     while (true);
  };
  function WechatPermissions(host, scopeOf, hostCalls) {
    host = host === VOID ? WxPermissionHost_instance : host;
    var tmp;
    if (scopeOf === VOID) {
      tmp = WechatPermissionScopes$scopeFor$ref(WechatPermissionScopes_getInstance());
    } else {
      tmp = scopeOf;
    }
    scopeOf = tmp;
    hostCalls = hostCalls === VOID ? CoroutineScope_0(SupervisorJob().plus_s13ygv_k$(Dispatchers_getInstance().Default_1)) : hostCalls;
    this.host_1 = host;
    this.scopeOf_1 = scopeOf;
    this.hostCalls_1 = hostCalls;
    var tmp_0 = this;
    // Inline function 'kotlin.collections.mutableMapOf' call
    tmp_0.inFlight_1 = LinkedHashMap_init_$Create$();
  }
  protoOf(WechatPermissions).stateOf_asu77n_k$ = function (permission, $completion) {
    return readState(this, permission, this.scopeOf_1(permission), $completion);
  };
  protoOf(WechatPermissions).request_pifwc6_k$ = function (permission, $completion) {
    var scope = this.scopeOf_1(permission);
    var tmp0_elvis_lhs = this.inFlight_1.get_wei43m_k$(permission);
    var tmp;
    if (tmp0_elvis_lhs == null) {
      // Inline function 'kotlin.also' call
      var this_0 = async(this.hostCalls_1, VOID, VOID, WechatPermissions$request$slambda_0(this, permission, scope, null));
      // Inline function 'kotlin.collections.set' call
      this.inFlight_1.put_4fpzoq_k$(permission, this_0);
      this_0.invokeOnCompletion_n6cffu_k$(WechatPermissions$request$lambda(this, permission));
      tmp = this_0;
    } else {
      tmp = tmp0_elvis_lhs;
    }
    var pending = tmp;
    return pending.await_4rdzbx_k$($completion);
  };
  protoOf(WechatPermissions).openSettings_ozycxk_k$ = function (permission, $completion) {
    var tmp = new $openSettingsCOROUTINE$(this, permission, $completion);
    tmp.result_1 = Unit_instance;
    tmp.exception_1 = null;
    return tmp.doResume_5yljmg_k$();
  };
  function performAuthorization($this, $completion) {
    return awaitHostCallback(WechatPrivacy$performAuthorization$lambda($this), $completion);
  }
  function requireSupported_1($this) {
    if (!$this.host_1.isSupported_j5t6ec_k$()) {
      throw new UnsupportedCapability(Companion_getInstance_3().Key_1);
    }
  }
  function WechatPrivacy$status$lambda$lambda($success) {
    return function (result) {
      $success(wxPrivacyRequirement(result));
      return Unit_instance;
    };
  }
  function WechatPrivacy$status$lambda$lambda_0($failure) {
    return function (result) {
      $failure(mapWechatHostFailure('getPrivacySetting', result));
      return Unit_instance;
    };
  }
  function WechatPrivacy$status$lambda(this$0) {
    return function (success, failure) {
      var tmp = WechatPrivacy$status$lambda$lambda(success);
      this$0.host_1.getPrivacySetting_6wl9z0_k$(tmp, WechatPrivacy$status$lambda$lambda_0(failure));
      return null;
    };
  }
  function WechatPrivacy$requestAuthorization$slambda(this$0, resultContinuation) {
    this.this$0__1 = this$0;
    CoroutineImpl.call(this, resultContinuation);
  }
  protoOf(WechatPrivacy$requestAuthorization$slambda).invoke_y5gbn2_k$ = function ($this$async, $completion) {
    var tmp = this.create_rcuf4x_k$($this$async, $completion);
    tmp.result_1 = Unit_instance;
    tmp.exception_1 = null;
    return tmp.doResume_5yljmg_k$();
  };
  protoOf(WechatPrivacy$requestAuthorization$slambda).invoke_qns8j1_k$ = function (p1, $completion) {
    return this.invoke_y5gbn2_k$((!(p1 == null) ? isInterface(p1, CoroutineScope) : false) ? p1 : THROW_CCE(), $completion);
  };
  protoOf(WechatPrivacy$requestAuthorization$slambda).doResume_5yljmg_k$ = function () {
    var suspendResult = this.result_1;
    $sm: do
      try {
        var tmp = this.state_1;
        switch (tmp) {
          case 0:
            this.exceptionState_1 = 2;
            this.state_1 = 1;
            suspendResult = performAuthorization(this.this$0__1, this);
            if (suspendResult === get_COROUTINE_SUSPENDED()) {
              return suspendResult;
            }

            continue $sm;
          case 1:
            return suspendResult;
          case 2:
            throw this.exception_1;
        }
      } catch ($p) {
        var e = $p;
        if (this.exceptionState_1 === 2) {
          throw e;
        } else {
          this.state_1 = this.exceptionState_1;
          this.exception_1 = e;
        }
      }
     while (true);
  };
  protoOf(WechatPrivacy$requestAuthorization$slambda).create_rcuf4x_k$ = function ($this$async, completion) {
    var i = new WechatPrivacy$requestAuthorization$slambda(this.this$0__1, completion);
    i.$this$async_1 = $this$async;
    return i;
  };
  function WechatPrivacy$requestAuthorization$slambda_0(this$0, resultContinuation) {
    var i = new WechatPrivacy$requestAuthorization$slambda(this$0, resultContinuation);
    return constructCallableReference(function ($this$async, $completion) {
      return i.invoke_y5gbn2_k$($this$async, $completion);
    }, 1);
  }
  function WechatPrivacy$requestAuthorization$lambda(this$0) {
    return function (it) {
      this$0.inFlight_1 = null;
      return Unit_instance;
    };
  }
  function WechatPrivacy$performAuthorization$lambda$lambda($success) {
    return function () {
      $success(Authorized_instance);
      return Unit_instance;
    };
  }
  function WechatPrivacy$performAuthorization$lambda$lambda_0($success, $failure) {
    return function (result) {
      var classified = mapWechatPrivacyAuthorizeFailure(result);
      var tmp;
      if (equals(classified, Refused_instance_0)) {
        tmp = $success(Refused_instance);
      } else {
        if (classified instanceof Failed) {
          tmp = $failure(classified.error_1);
        } else {
          noWhenBranchMatchedException();
        }
      }
      return Unit_instance;
    };
  }
  function WechatPrivacy$performAuthorization$lambda(this$0) {
    return function (success, failure) {
      var tmp = WechatPrivacy$performAuthorization$lambda$lambda(success);
      this$0.host_1.requirePrivacyAuthorize_u38fn3_k$(tmp, WechatPrivacy$performAuthorization$lambda$lambda_0(success, failure));
      return null;
    };
  }
  function $statusCOROUTINE$(_this__u8e3s4, resultContinuation) {
    CoroutineImpl.call(this, resultContinuation);
    this._this__u8e3s4__1 = _this__u8e3s4;
  }
  protoOf($statusCOROUTINE$).doResume_5yljmg_k$ = function () {
    var suspendResult = this.result_1;
    $sm: do
      try {
        var tmp = this.state_1;
        switch (tmp) {
          case 0:
            this.exceptionState_1 = 2;
            requireSupported_1(this._this__u8e3s4__1);
            this.state_1 = 1;
            suspendResult = awaitHostCallback(WechatPrivacy$status$lambda(this._this__u8e3s4__1), this);
            if (suspendResult === get_COROUTINE_SUSPENDED()) {
              return suspendResult;
            }

            continue $sm;
          case 1:
            var requirement = suspendResult;
            var tmp_0;
            if (requirement instanceof Required) {
              tmp_0 = new PrivacyStatus(PrivacyAuthorizationRequirement_REQUIRED_getInstance(), requirement.contractName_1);
            } else {
              if (requirement instanceof NotRequired) {
                tmp_0 = new PrivacyStatus(PrivacyAuthorizationRequirement_NOT_REQUIRED_getInstance(), requirement.contractName_1);
              } else {
                if (equals(requirement, Unreadable_instance_2)) {
                  throw new InvalidResponse('The WeChat host answered getPrivacySetting with a value the SDK cannot read');
                } else {
                  noWhenBranchMatchedException();
                }
              }
            }

            return tmp_0;
          case 2:
            throw this.exception_1;
        }
      } catch ($p) {
        var e = $p;
        if (this.exceptionState_1 === 2) {
          throw e;
        } else {
          this.state_1 = this.exceptionState_1;
          this.exception_1 = e;
        }
      }
     while (true);
  };
  function $requireSatisfiedCOROUTINE$(_this__u8e3s4, resultContinuation) {
    CoroutineImpl.call(this, resultContinuation);
    this._this__u8e3s4__1 = _this__u8e3s4;
  }
  protoOf($requireSatisfiedCOROUTINE$).doResume_5yljmg_k$ = function () {
    var suspendResult = this.result_1;
    $sm: do
      try {
        var tmp = this.state_1;
        switch (tmp) {
          case 0:
            this.exceptionState_1 = 2;
            this.state_1 = 1;
            suspendResult = this._this__u8e3s4__1.status_1hikgl_k$(this);
            if (suspendResult === get_COROUTINE_SUSPENDED()) {
              return suspendResult;
            }

            continue $sm;
          case 1:
            var current = suspendResult;
            if (current.requirement_1.equals(PrivacyAuthorizationRequirement_REQUIRED_getInstance())) {
              throw new PrivacyAuthorizationRequired(current.contractName_1);
            }

            return Unit_instance;
          case 2:
            throw this.exception_1;
        }
      } catch ($p) {
        var e = $p;
        if (this.exceptionState_1 === 2) {
          throw e;
        } else {
          this.state_1 = this.exceptionState_1;
          this.exception_1 = e;
        }
      }
     while (true);
  };
  function WechatPrivacy(host, hostCalls) {
    host = host === VOID ? WxPrivacyHost_instance : host;
    hostCalls = hostCalls === VOID ? CoroutineScope_0(SupervisorJob().plus_s13ygv_k$(Dispatchers_getInstance().Default_1)) : hostCalls;
    this.host_1 = host;
    this.hostCalls_1 = hostCalls;
    this.inFlight_1 = null;
  }
  protoOf(WechatPrivacy).status_1hikgl_k$ = function ($completion) {
    var tmp = new $statusCOROUTINE$(this, $completion);
    tmp.result_1 = Unit_instance;
    tmp.exception_1 = null;
    return tmp.doResume_5yljmg_k$();
  };
  protoOf(WechatPrivacy).requestAuthorization_zgnahm_k$ = function ($completion) {
    requireSupported_1(this);
    var tmp0_elvis_lhs = this.inFlight_1;
    var tmp;
    if (tmp0_elvis_lhs == null) {
      // Inline function 'kotlin.also' call
      var this_0 = async(this.hostCalls_1, VOID, VOID, WechatPrivacy$requestAuthorization$slambda_0(this, null));
      this.inFlight_1 = this_0;
      this_0.invokeOnCompletion_n6cffu_k$(WechatPrivacy$requestAuthorization$lambda(this));
      tmp = this_0;
    } else {
      tmp = tmp0_elvis_lhs;
    }
    var pending = tmp;
    return pending.await_4rdzbx_k$($completion);
  };
  protoOf(WechatPrivacy).requireSatisfied_farkra_k$ = function ($completion) {
    var tmp = new $requireSatisfiedCOROUTINE$(this, $completion);
    tmp.result_1 = Unit_instance;
    tmp.exception_1 = null;
    return tmp.doResume_5yljmg_k$();
  };
  function WxPrivacyHost$requirePrivacyAuthorize$lambda($success) {
    return function (it) {
      $success();
      return Unit_instance;
    };
  }
  function WxPrivacyHost() {
  }
  protoOf(WxPrivacyHost).isSupported_j5t6ec_k$ = function () {
    return hasWxGetPrivacySetting() && hasWxRequirePrivacyAuthorize();
  };
  protoOf(WxPrivacyHost).getPrivacySetting_6wl9z0_k$ = function (success, failure) {
    var options = wxGetPrivacySettingOptions();
    options.success = success;
    options.fail = failure;
    wx.getPrivacySetting(options);
  };
  protoOf(WxPrivacyHost).requirePrivacyAuthorize_u38fn3_k$ = function (success, failure) {
    var options = wxRequirePrivacyAuthorizeOptions();
    options.success = WxPrivacyHost$requirePrivacyAuthorize$lambda(success);
    options.fail = failure;
    wx.requirePrivacyAuthorize(options);
  };
  var WxPrivacyHost_instance;
  function WxPrivacyHost_getInstance() {
    return WxPrivacyHost_instance;
  }
  function WxRuntimeInfoHost() {
  }
  protoOf(WxRuntimeInfoHost).baseLibraryVersion_8f7rqq_k$ = function () {
    if (hasWxGetAppBaseInfo()) {
      var version = wx.getAppBaseInfo().SDKVersion;
      // Inline function 'kotlin.text.isNullOrBlank' call
      if (!(version == null || isBlank(version)))
        return version;
    }
    if (hasWxGetSystemInfoSync()) {
      var version_0 = wx.getSystemInfoSync().SDKVersion;
      // Inline function 'kotlin.text.isNullOrBlank' call
      if (!(version_0 == null || isBlank(version_0)))
        return version_0;
    }
    return null;
  };
  protoOf(WxRuntimeInfoHost).platform_v04zg3_k$ = function () {
    if (hasWxGetDeviceInfo()) {
      var value = wx.getDeviceInfo().platform;
      // Inline function 'kotlin.text.isNullOrBlank' call
      if (!(value == null || isBlank(value)))
        return value;
    }
    if (hasWxGetSystemInfoSync()) {
      var value_0 = wx.getSystemInfoSync().platform;
      // Inline function 'kotlin.text.isNullOrBlank' call
      if (!(value_0 == null || isBlank(value_0)))
        return value_0;
    }
    return null;
  };
  protoOf(WxRuntimeInfoHost).canIUse_xcpuhg_k$ = function (schema) {
    return hasWxCanIUse() && wx.canIUse(schema);
  };
  protoOf(WxRuntimeInfoHost).hasFileSystemMethod_e0uzkc_k$ = function (method) {
    return hasWxFileSystemMethod(method);
  };
  protoOf(WxRuntimeInfoHost).hasUserDataPath_z1qkq4_k$ = function () {
    return hasWxUserDataPath();
  };
  var WxRuntimeInfoHost_instance;
  function WxRuntimeInfoHost_getInstance() {
    return WxRuntimeInfoHost_instance;
  }
  function Companion_7() {
    this.MISSING_KEY_ERROR_1 = 'getStorage:fail data not found';
  }
  var Companion_instance_9;
  function Companion_getInstance_7() {
    return Companion_instance_9;
  }
  function WechatStorage$get$lambda$lambda($success, $failure, $key) {
    return function (value) {
      var tmp;
      if (!(value == null) ? typeof value === 'string' : false) {
        tmp = $success(value);
      } else {
        tmp = $failure(new InvalidResponse("WeChat storage returned a non-string value for key '" + $key + "'"));
      }
      return Unit_instance;
    };
  }
  function WechatStorage$get$lambda$lambda_0($success, $failure) {
    return function (result) {
      var tmp;
      if (result.errMsg === 'getStorage:fail data not found') {
        tmp = $success(null);
      } else {
        tmp = $failure(mapWechatHostFailure('getStorage', result));
      }
      return Unit_instance;
    };
  }
  function WechatStorage$get$lambda(this$0, $key) {
    return function (success, failure) {
      var tmp = WechatStorage$get$lambda$lambda(success, failure, $key);
      this$0.host_1.get_bli6e2_k$($key, tmp, WechatStorage$get$lambda$lambda_0(success, failure));
      return null;
    };
  }
  function WechatStorage$set$lambda$lambda($success) {
    return function () {
      $success(Unit_instance);
      return Unit_instance;
    };
  }
  function WechatStorage$set$lambda$lambda_0($failure) {
    return function (result) {
      $failure(mapWechatHostFailure('setStorage', result));
      return Unit_instance;
    };
  }
  function WechatStorage$set$lambda(this$0, $key, $value) {
    return function (success, failure) {
      var tmp = WechatStorage$set$lambda$lambda(success);
      this$0.host_1.set_q8fvf7_k$($key, $value, tmp, WechatStorage$set$lambda$lambda_0(failure));
      return null;
    };
  }
  function WechatStorage$remove$lambda$lambda($success) {
    return function () {
      $success(Unit_instance);
      return Unit_instance;
    };
  }
  function WechatStorage$remove$lambda$lambda_0($failure) {
    return function (result) {
      $failure(mapWechatHostFailure('removeStorage', result));
      return Unit_instance;
    };
  }
  function WechatStorage$remove$lambda(this$0, $key) {
    return function (success, failure) {
      var tmp = WechatStorage$remove$lambda$lambda(success);
      this$0.host_1.remove_2794e7_k$($key, tmp, WechatStorage$remove$lambda$lambda_0(failure));
      return null;
    };
  }
  function WechatStorage(host) {
    host = host === VOID ? WxStorageHost_instance : host;
    this.host_1 = host;
  }
  protoOf(WechatStorage).get_3l5a7q_k$ = function (key, $completion) {
    return awaitHostCallback(WechatStorage$get$lambda(this, key), $completion);
  };
  protoOf(WechatStorage).set_qwzljf_k$ = function (key, value, $completion) {
    return awaitHostCallback(WechatStorage$set$lambda(this, key, value), $completion);
  };
  protoOf(WechatStorage).remove_gwf6lb_k$ = function (key, $completion) {
    return awaitHostCallback(WechatStorage$remove$lambda(this, key), $completion);
  };
  function WxStorageHost$get$lambda($success) {
    return function (result) {
      $success(result.data);
      return Unit_instance;
    };
  }
  function WxStorageHost$set$lambda($success) {
    return function (it) {
      $success();
      return Unit_instance;
    };
  }
  function WxStorageHost$remove$lambda($success) {
    return function (it) {
      $success();
      return Unit_instance;
    };
  }
  function WxStorageHost() {
  }
  protoOf(WxStorageHost).get_bli6e2_k$ = function (key, success, failure) {
    var options = wxGetStorageOptions(key);
    options.success = WxStorageHost$get$lambda(success);
    options.fail = failure;
    wx.getStorage(options);
  };
  protoOf(WxStorageHost).set_q8fvf7_k$ = function (key, value, success, failure) {
    var options = wxSetStorageOptions(key, value);
    options.success = WxStorageHost$set$lambda(success);
    options.fail = failure;
    wx.setStorage(options);
  };
  protoOf(WxStorageHost).remove_2794e7_k$ = function (key, success, failure) {
    var options = wxRemoveStorageOptions(key);
    options.success = WxStorageHost$remove$lambda(success);
    options.fail = failure;
    wx.removeStorage(options);
  };
  var WxStorageHost_instance;
  function WxStorageHost_getInstance() {
    return WxStorageHost_instance;
  }
  function hasWxCheckSession() {
    return typeof wx !== 'undefined' && typeof wx.checkSession === 'function';
  }
  function wxCheckSessionOptions() {
    var options = {};
    return options;
  }
  function Present(text) {
    this.text_1 = text;
  }
  protoOf(Present).toString = function () {
    return 'Present(text=' + this.text_1 + ')';
  };
  protoOf(Present).hashCode = function () {
    return getStringHashCode(this.text_1);
  };
  protoOf(Present).equals = function (other) {
    if (this === other)
      return true;
    if (!(other instanceof Present))
      return false;
    if (!(this.text_1 === other.text_1))
      return false;
    return true;
  };
  function Unreadable() {
  }
  protoOf(Unreadable).toString = function () {
    return 'Unreadable';
  };
  protoOf(Unreadable).hashCode = function () {
    return -1461035294;
  };
  protoOf(Unreadable).equals = function (other) {
    if (this === other)
      return true;
    if (!(other instanceof Unreadable))
      return false;
    return true;
  };
  var Unreadable_instance;
  function Unreadable_getInstance() {
    return Unreadable_instance;
  }
  function wxClipboardText(result) {
    var data = result.data;
    var tmp;
    if (data == null || typeof data === 'undefined') {
      tmp = Unreadable_instance;
    } else {
      if (typeof data === 'string') {
        tmp = new Present(data);
      } else {
        tmp = Unreadable_instance;
      }
    }
    return tmp;
  }
  function hasWxGetClipboardData() {
    return typeof wx !== 'undefined' && typeof wx.getClipboardData === 'function';
  }
  function hasWxSetClipboardData() {
    return typeof wx !== 'undefined' && typeof wx.setClipboardData === 'function';
  }
  function wxGetClipboardDataOptions() {
    var options = {};
    return options;
  }
  function wxSetClipboardDataOptions(data) {
    var options = {};
    options.data = data;
    return options;
  }
  function Present_0(text) {
    this.text_1 = text;
  }
  protoOf(Present_0).toString = function () {
    return 'Present(text=' + this.text_1 + ')';
  };
  protoOf(Present_0).hashCode = function () {
    return getStringHashCode(this.text_1);
  };
  protoOf(Present_0).equals = function (other) {
    if (this === other)
      return true;
    if (!(other instanceof Present_0))
      return false;
    if (!(this.text_1 === other.text_1))
      return false;
    return true;
  };
  function Unreadable_0() {
  }
  protoOf(Unreadable_0).toString = function () {
    return 'Unreadable';
  };
  protoOf(Unreadable_0).hashCode = function () {
    return -994688800;
  };
  protoOf(Unreadable_0).equals = function (other) {
    if (this === other)
      return true;
    if (!(other instanceof Unreadable_0))
      return false;
    return true;
  };
  var Unreadable_instance_0;
  function Unreadable_getInstance_0() {
    return Unreadable_instance_0;
  }
  function wxFileText(result) {
    var data = result.data;
    var tmp;
    if (data == null || typeof data === 'undefined') {
      tmp = Unreadable_instance_0;
    } else {
      if (typeof data === 'string') {
        tmp = new Present_0(data);
      } else {
        tmp = Unreadable_instance_0;
      }
    }
    return tmp;
  }
  function hasWxFileSystemMethod(method) {
    return typeof wx !== 'undefined' && typeof wx.getFileSystemManager === 'function' && typeof wx.getFileSystemManager()[method] === 'function';
  }
  function hasWxUserDataPath() {
    return typeof wx !== 'undefined' && typeof wx.env === 'object' && wx.env !== null && typeof wx.env.USER_DATA_PATH === 'string';
  }
  function wxReadFileOptions(filePath) {
    var options = {};
    options.filePath = filePath;
    options.encoding = 'utf8';
    return options;
  }
  function wxWriteFileOptions(filePath, data) {
    var options = {};
    options.filePath = filePath;
    options.data = data;
    options.encoding = 'utf8';
    return options;
  }
  function wxAccessOptions(path) {
    var options = {};
    options.path = path;
    return options;
  }
  function wxUnlinkOptions(filePath) {
    var options = {};
    options.filePath = filePath;
    return options;
  }
  function wxRequestHeader(headers) {
    var header = {};
    // Inline function 'kotlin.collections.iterator' call
    var _iterator__ex2g4s = headers.get_entries_p20ztl_k$().iterator_jk1svi_k$();
    while (_iterator__ex2g4s.hasNext_bitz1p_k$()) {
      var _destruct__k2r9zo = _iterator__ex2g4s.next_20eer_k$();
      // Inline function 'kotlin.collections.component1' call
      var name = _destruct__k2r9zo.get_key_18j28a_k$();
      // Inline function 'kotlin.collections.component2' call
      var value = _destruct__k2r9zo.get_value_j01efc_k$();
      header[name] = value;
    }
    return header;
  }
  function wxResponseHeaders(raw) {
    if (raw == null)
      return emptyMap();
    // Inline function 'kotlin.collections.mutableMapOf' call
    var headers = LinkedHashMap_init_$Create$();
    var names = Object.keys(raw);
    var inductionVariable = 0;
    var last = names.length;
    while (inductionVariable < last) {
      var name = names[inductionVariable];
      inductionVariable = inductionVariable + 1 | 0;
      var value = raw[name];
      if (!(value == null) ? typeof value === 'string' : false) {
        // Inline function 'kotlin.collections.set' call
        headers.put_4fpzoq_k$(name, value);
      }
    }
    return headers;
  }
  function wxLoginOptions(timeoutMillis) {
    timeoutMillis = timeoutMillis === VOID ? null : timeoutMillis;
    var options = {};
    if (!(timeoutMillis == null)) {
      options.timeout = timeoutMillis;
    }
    return options;
  }
  function wxNavigateToOptions(url) {
    var options = {};
    options.url = url;
    return options;
  }
  function wxRedirectToOptions(url) {
    var options = {};
    options.url = url;
    return options;
  }
  function wxNavigateBackOptions(delta) {
    delta = delta === VOID ? null : delta;
    var options = {};
    if (!(delta == null)) {
      options.delta = delta;
    }
    return options;
  }
  function Absent() {
  }
  protoOf(Absent).toString = function () {
    return 'Absent';
  };
  protoOf(Absent).hashCode = function () {
    return 420615739;
  };
  protoOf(Absent).equals = function (other) {
    if (this === other)
      return true;
    if (!(other instanceof Absent))
      return false;
    return true;
  };
  var Absent_instance;
  function Absent_getInstance() {
    return Absent_instance;
  }
  function Decided(granted) {
    this.granted_1 = granted;
  }
  protoOf(Decided).toString = function () {
    return 'Decided(granted=' + this.granted_1 + ')';
  };
  protoOf(Decided).hashCode = function () {
    return getBooleanHashCode(this.granted_1);
  };
  protoOf(Decided).equals = function (other) {
    if (this === other)
      return true;
    if (!(other instanceof Decided))
      return false;
    if (!(this.granted_1 === other.granted_1))
      return false;
    return true;
  };
  function Unreadable_1() {
  }
  protoOf(Unreadable_1).toString = function () {
    return 'Unreadable';
  };
  protoOf(Unreadable_1).hashCode = function () {
    return 59014027;
  };
  protoOf(Unreadable_1).equals = function (other) {
    if (this === other)
      return true;
    if (!(other instanceof Unreadable_1))
      return false;
    return true;
  };
  var Unreadable_instance_1;
  function Unreadable_getInstance_1() {
    return Unreadable_instance_1;
  }
  function wxScopeEntry(authSetting, scope) {
    if (authSetting == null)
      return Unreadable_instance_1;
    if (!(typeof authSetting === 'object'))
      return Unreadable_instance_1;
    var present = Object.prototype.hasOwnProperty.call(authSetting, scope);
    if (!present)
      return Absent_instance;
    var value = authSetting[scope];
    var tmp;
    if (!(value == null) ? typeof value === 'boolean' : false) {
      tmp = new Decided(value);
    } else {
      tmp = Unreadable_instance_1;
    }
    return tmp;
  }
  function wxGetSettingOptions() {
    var options = {};
    return options;
  }
  function wxAuthorizeOptions(scope) {
    var options = {};
    options.scope = scope;
    return options;
  }
  function wxOpenSettingOptions() {
    var options = {};
    return options;
  }
  function Required(contractName) {
    this.contractName_1 = contractName;
  }
  protoOf(Required).toString = function () {
    return 'Required(contractName=' + this.contractName_1 + ')';
  };
  protoOf(Required).hashCode = function () {
    return this.contractName_1 == null ? 0 : getStringHashCode(this.contractName_1);
  };
  protoOf(Required).equals = function (other) {
    if (this === other)
      return true;
    if (!(other instanceof Required))
      return false;
    if (!(this.contractName_1 == other.contractName_1))
      return false;
    return true;
  };
  function NotRequired(contractName) {
    this.contractName_1 = contractName;
  }
  protoOf(NotRequired).toString = function () {
    return 'NotRequired(contractName=' + this.contractName_1 + ')';
  };
  protoOf(NotRequired).hashCode = function () {
    return this.contractName_1 == null ? 0 : getStringHashCode(this.contractName_1);
  };
  protoOf(NotRequired).equals = function (other) {
    if (this === other)
      return true;
    if (!(other instanceof NotRequired))
      return false;
    if (!(this.contractName_1 == other.contractName_1))
      return false;
    return true;
  };
  function Unreadable_2() {
  }
  protoOf(Unreadable_2).toString = function () {
    return 'Unreadable';
  };
  protoOf(Unreadable_2).hashCode = function () {
    return 497866830;
  };
  protoOf(Unreadable_2).equals = function (other) {
    if (this === other)
      return true;
    if (!(other instanceof Unreadable_2))
      return false;
    return true;
  };
  var Unreadable_instance_2;
  function Unreadable_getInstance_2() {
    return Unreadable_instance_2;
  }
  function wxPrivacyRequirement(result) {
    var needAuthorization = result.needAuthorization;
    if (!(!(needAuthorization == null) ? typeof needAuthorization === 'boolean' : false))
      return Unreadable_instance_2;
    var rawName = result.privacyContractName;
    var tmp;
    if (rawName == null) {
      tmp = null;
    } else {
      if (typeof rawName === 'string') {
        tmp = rawName;
      } else {
        return Unreadable_instance_2;
      }
    }
    var contractName = tmp;
    var tmp_0;
    if (needAuthorization) {
      tmp_0 = new Required(contractName);
    } else {
      tmp_0 = new NotRequired(contractName);
    }
    return tmp_0;
  }
  function hasWxGetPrivacySetting() {
    return typeof wx !== 'undefined' && typeof wx.getPrivacySetting === 'function';
  }
  function hasWxRequirePrivacyAuthorize() {
    return typeof wx !== 'undefined' && typeof wx.requirePrivacyAuthorize === 'function';
  }
  function wxGetPrivacySettingOptions() {
    var options = {};
    return options;
  }
  function wxRequirePrivacyAuthorizeOptions() {
    var options = {};
    return options;
  }
  function wxRequestOptions(url, method, data, timeoutMillis) {
    data = data === VOID ? null : data;
    timeoutMillis = timeoutMillis === VOID ? null : timeoutMillis;
    var options = {};
    options.url = url;
    options.method = method;
    options.dataType = 'text';
    if (!(data == null)) {
      options.data = data;
    }
    if (!(timeoutMillis == null)) {
      options.timeout = timeoutMillis;
    }
    return options;
  }
  function hasWxCanIUse() {
    return typeof wx !== 'undefined' && typeof wx.canIUse === 'function';
  }
  function hasWxGetAppBaseInfo() {
    return typeof wx !== 'undefined' && typeof wx.getAppBaseInfo === 'function';
  }
  function hasWxGetSystemInfoSync() {
    return typeof wx !== 'undefined' && typeof wx.getSystemInfoSync === 'function';
  }
  function hasWxGetDeviceInfo() {
    return typeof wx !== 'undefined' && typeof wx.getDeviceInfo === 'function';
  }
  function wxGetStorageOptions(key) {
    var options = {};
    options.key = key;
    return options;
  }
  function wxSetStorageOptions(key, data) {
    var options = {};
    options.key = key;
    options.data = data;
    return options;
  }
  function wxRemoveStorageOptions(key) {
    var options = {};
    options.key = key;
    return options;
  }
  function hasWxVibrateShort() {
    return typeof wx !== 'undefined' && typeof wx.vibrateShort === 'function';
  }
  function hasWxVibrateLong() {
    return typeof wx !== 'undefined' && typeof wx.vibrateLong === 'function';
  }
  function wxVibrateOptions() {
    var options = {};
    return options;
  }
  function WechatAppLifecycle() {
    this.lifecycleState_1 = MutableStateFlow(MiniAppLifecycleState_BACKGROUND_getInstance());
    this.stateChanges_1 = this.lifecycleState_1;
  }
  protoOf(WechatAppLifecycle).get_state_iypx7s_k$ = function () {
    return this.lifecycleState_1.get_value_j01efc_k$();
  };
  protoOf(WechatAppLifecycle).appLaunched_2kde65_k$ = function () {
    this.lifecycleState_1.set_value_v1vabv_k$(MiniAppLifecycleState_FOREGROUND_getInstance());
  };
  protoOf(WechatAppLifecycle).appShown_f14600_k$ = function () {
    this.lifecycleState_1.set_value_v1vabv_k$(MiniAppLifecycleState_FOREGROUND_getInstance());
  };
  protoOf(WechatAppLifecycle).appHidden_uce53v_k$ = function () {
    this.lifecycleState_1.set_value_v1vabv_k$(MiniAppLifecycleState_BACKGROUND_getInstance());
  };
  function WechatCapabilityRequirement(canIUseSchemas, minimumBaseLibraryVersion, presence) {
    canIUseSchemas = canIUseSchemas === VOID ? emptyList() : canIUseSchemas;
    minimumBaseLibraryVersion = minimumBaseLibraryVersion === VOID ? null : minimumBaseLibraryVersion;
    presence = presence === VOID ? null : presence;
    this.canIUseSchemas_1 = canIUseSchemas;
    this.minimumBaseLibraryVersion_1 = minimumBaseLibraryVersion;
    this.presence_1 = presence;
  }
  function WechatCapabilityCatalog$requirements$lambda(host) {
    return host.hasFileSystemMethod_e0uzkc_k$('readFile');
  }
  function WechatCapabilityCatalog$requirements$lambda_0(host) {
    return host.hasFileSystemMethod_e0uzkc_k$('writeFile');
  }
  function WechatCapabilityCatalog$requirements$lambda_1(host) {
    return host.hasFileSystemMethod_e0uzkc_k$('access');
  }
  function WechatCapabilityCatalog$requirements$lambda_2(host) {
    return host.hasFileSystemMethod_e0uzkc_k$('unlink');
  }
  function WechatCapabilityCatalog$requirements$lambda_3(host) {
    return host.hasUserDataPath_z1qkq4_k$();
  }
  function WechatCapabilityCatalog() {
    WechatCapabilityCatalog_instance = this;
    this.RuntimeDetectionKey_1 = new CapabilityKey('wechat.runtime-detection');
    var tmp = this;
    var tmp0 = Companion_instance_7.parse_pc1q8p_k$('1.9.9');
    var tmp$ret$0;
    $l$block: {
      // Inline function 'kotlin.requireNotNull' call
      if (tmp0 == null) {
        var message = 'The recorded minimum base-library version must be a dotted numeric version';
        throw IllegalArgumentException_init_$Create$(toString_0(message));
      } else {
        tmp$ret$0 = tmp0;
        break $l$block;
      }
    }
    tmp.FILESYSTEM_MINIMUM_1 = tmp$ret$0;
    var tmp_0 = this;
    var tmp_1 = to(Companion_getInstance_4().Key_1, new WechatCapabilityRequirement(listOf(['getStorage', 'setStorage', 'removeStorage'])));
    var tmp_2 = to(Companion_getInstance_0().Key_1, new WechatCapabilityRequirement(listOf_0('request')));
    var tmp_3 = to(Companion_getInstance().Key_1, new WechatCapabilityRequirement());
    var tmp_4 = to(Companion_getInstance_1().Key_1, new WechatCapabilityRequirement(listOf(['getSetting', 'authorize', 'openSetting'])));
    var tmp_5 = to(Companion_getInstance_6().Key_1, new WechatCapabilityRequirement(listOf_0('checkSession')));
    var tmp_6 = WeChatDeviceCapabilities_getInstance().FileSystemRead_1;
    var tmp_7 = to(tmp_6, new WechatCapabilityRequirement(VOID, this.FILESYSTEM_MINIMUM_1, WechatCapabilityCatalog$requirements$lambda));
    var tmp_8 = WeChatDeviceCapabilities_getInstance().FileSystemWrite_1;
    var tmp_9 = to(tmp_8, new WechatCapabilityRequirement(VOID, this.FILESYSTEM_MINIMUM_1, WechatCapabilityCatalog$requirements$lambda_0));
    var tmp_10 = WeChatDeviceCapabilities_getInstance().FileSystemAccess_1;
    var tmp_11 = to(tmp_10, new WechatCapabilityRequirement(VOID, this.FILESYSTEM_MINIMUM_1, WechatCapabilityCatalog$requirements$lambda_1));
    var tmp_12 = WeChatDeviceCapabilities_getInstance().FileSystemRemove_1;
    var tmp_13 = to(tmp_12, new WechatCapabilityRequirement(VOID, this.FILESYSTEM_MINIMUM_1, WechatCapabilityCatalog$requirements$lambda_2));
    var tmp_14 = WeChatDeviceCapabilities_getInstance().FileSystemSandboxPath_1;
    var tmp_15 = to(tmp_14, new WechatCapabilityRequirement(VOID, this.FILESYSTEM_MINIMUM_1, WechatCapabilityCatalog$requirements$lambda_3));
    var tmp_16 = WeChatDeviceCapabilities_getInstance().ClipboardRead_1;
    var tmp_17 = listOf_0('getClipboardData');
    var tmp0_0 = Companion_instance_7.parse_pc1q8p_k$('1.1.0');
    var tmp$ret$2;
    $l$block_0: {
      // Inline function 'kotlin.requireNotNull' call
      if (tmp0_0 == null) {
        var message_0 = 'The recorded minimum base-library version must be a dotted numeric version';
        throw IllegalArgumentException_init_$Create$(toString_0(message_0));
      } else {
        tmp$ret$2 = tmp0_0;
        break $l$block_0;
      }
    }
    var tmp_18 = to(tmp_16, new WechatCapabilityRequirement(tmp_17, tmp$ret$2));
    var tmp_19 = WeChatDeviceCapabilities_getInstance().ClipboardWrite_1;
    var tmp_20 = listOf_0('setClipboardData');
    var tmp0_1 = Companion_instance_7.parse_pc1q8p_k$('1.1.0');
    var tmp$ret$4;
    $l$block_1: {
      // Inline function 'kotlin.requireNotNull' call
      if (tmp0_1 == null) {
        var message_1 = 'The recorded minimum base-library version must be a dotted numeric version';
        throw IllegalArgumentException_init_$Create$(toString_0(message_1));
      } else {
        tmp$ret$4 = tmp0_1;
        break $l$block_1;
      }
    }
    var tmp_21 = to(tmp_19, new WechatCapabilityRequirement(tmp_20, tmp$ret$4));
    var tmp_22 = WeChatDeviceCapabilities_getInstance().VibrateShort_1;
    var tmp_23 = listOf_0('vibrateShort');
    var tmp0_2 = Companion_instance_7.parse_pc1q8p_k$('1.2.0');
    var tmp$ret$6;
    $l$block_2: {
      // Inline function 'kotlin.requireNotNull' call
      if (tmp0_2 == null) {
        var message_2 = 'The recorded minimum base-library version must be a dotted numeric version';
        throw IllegalArgumentException_init_$Create$(toString_0(message_2));
      } else {
        tmp$ret$6 = tmp0_2;
        break $l$block_2;
      }
    }
    var tmp_24 = to(tmp_22, new WechatCapabilityRequirement(tmp_23, tmp$ret$6));
    var tmp_25 = WeChatDeviceCapabilities_getInstance().VibrateLong_1;
    var tmp_26 = listOf_0('vibrateLong');
    var tmp0_3 = Companion_instance_7.parse_pc1q8p_k$('1.2.0');
    var tmp$ret$8;
    $l$block_3: {
      // Inline function 'kotlin.requireNotNull' call
      if (tmp0_3 == null) {
        var message_3 = 'The recorded minimum base-library version must be a dotted numeric version';
        throw IllegalArgumentException_init_$Create$(toString_0(message_3));
      } else {
        tmp$ret$8 = tmp0_3;
        break $l$block_3;
      }
    }
    var tmp_27 = to(tmp_25, new WechatCapabilityRequirement(tmp_26, tmp$ret$8));
    var tmp_28 = Companion_getInstance_3().Key_1;
    var tmp_29 = listOf(['getPrivacySetting', 'requirePrivacyAuthorize']);
    var tmp0_4 = Companion_instance_7.parse_pc1q8p_k$('2.32.3');
    var tmp$ret$10;
    $l$block_4: {
      // Inline function 'kotlin.requireNotNull' call
      if (tmp0_4 == null) {
        var message_4 = 'The recorded minimum base-library version must be a dotted numeric version';
        throw IllegalArgumentException_init_$Create$(toString_0(message_4));
      } else {
        tmp$ret$10 = tmp0_4;
        break $l$block_4;
      }
    }
    var tmp_30 = to(tmp_28, new WechatCapabilityRequirement(tmp_29, tmp$ret$10));
    var tmp_31 = listOf_0('getAppBaseInfo');
    var tmp0_5 = Companion_instance_7.parse_pc1q8p_k$('2.20.1');
    var tmp$ret$12;
    $l$block_5: {
      // Inline function 'kotlin.requireNotNull' call
      if (tmp0_5 == null) {
        var message_5 = 'The recorded minimum base-library version must be a dotted numeric version';
        throw IllegalArgumentException_init_$Create$(toString_0(message_5));
      } else {
        tmp$ret$12 = tmp0_5;
        break $l$block_5;
      }
    }
    tmp_0.requirements_1 = mapOf_0([tmp_1, tmp_2, tmp_3, tmp_4, tmp_5, tmp_7, tmp_9, tmp_11, tmp_13, tmp_15, tmp_18, tmp_21, tmp_24, tmp_27, tmp_30, to(this.RuntimeDetectionKey_1, new WechatCapabilityRequirement(tmp_31, tmp$ret$12))]);
  }
  protoOf(WechatCapabilityCatalog).requirementFor_k8c79s_k$ = function (key) {
    return this.requirements_1.get_wei43m_k$(key);
  };
  var WechatCapabilityCatalog_instance;
  function WechatCapabilityCatalog_getInstance() {
    if (WechatCapabilityCatalog_instance == null)
      new WechatCapabilityCatalog();
    return WechatCapabilityCatalog_instance;
  }
  function WechatCapabilityGate(runtimeInfo, runtimeHost) {
    this.runtimeInfo_1 = runtimeInfo;
    this.runtimeHost_1 = runtimeHost;
  }
  protoOf(WechatCapabilityGate).supportFor_tmznfg_k$ = function (capability) {
    var tmp0_elvis_lhs = WechatCapabilityCatalog_getInstance().requirementFor_k8c79s_k$(capability);
    var tmp;
    if (tmp0_elvis_lhs == null) {
      return Unsupported_instance;
    } else {
      tmp = tmp0_elvis_lhs;
    }
    var requirement = tmp;
    var minimum = requirement.minimumBaseLibraryVersion_1;
    if (!(minimum == null)) {
      var current = this.runtimeInfo_1.get_baseLibraryVersion_i4tt1z_k$();
      if (!(current == null) && current.compareTo_wrrkcm_k$(minimum) < 0) {
        return new VersionDependent(minimum, current);
      }
    }
    var tmp0 = requirement.canIUseSchemas_1;
    var tmp$ret$0;
    $l$block_0: {
      // Inline function 'kotlin.collections.any' call
      var tmp_0;
      if (isInterface(tmp0, Collection)) {
        tmp_0 = tmp0.isEmpty_y1axqb_k$();
      } else {
        tmp_0 = false;
      }
      if (tmp_0) {
        tmp$ret$0 = false;
        break $l$block_0;
      }
      var _iterator__ex2g4s = tmp0.iterator_jk1svi_k$();
      while (_iterator__ex2g4s.hasNext_bitz1p_k$()) {
        var element = _iterator__ex2g4s.next_20eer_k$();
        if (!this.runtimeHost_1.canIUse_xcpuhg_k$(element)) {
          tmp$ret$0 = true;
          break $l$block_0;
        }
      }
      tmp$ret$0 = false;
    }
    var unprobeable = tmp$ret$0;
    if (unprobeable)
      return Unsupported_instance;
    var presence = requirement.presence_1;
    if (!(presence == null) && !presence(this.runtimeHost_1))
      return Unsupported_instance;
    return Supported_instance;
  };
  function WechatPageLifecycle() {
    this.route_1 = null;
    this.visible_1 = false;
  }
  protoOf(WechatPageLifecycle).get_currentRoute_smvwyh_k$ = function () {
    return this.route_1;
  };
  protoOf(WechatPageLifecycle).pageShown_ox1yl4_k$ = function (pageRoute) {
    this.route_1 = pageRoute;
    this.visible_1 = true;
  };
  protoOf(WechatPageLifecycle).pageHidden_de0n61_k$ = function () {
    this.visible_1 = false;
  };
  protoOf(WechatPageLifecycle).pageUnloaded_2xrz7h_k$ = function (pageRoute) {
    if (!(this.route_1 === pageRoute))
      return Unit_instance;
    this.route_1 = null;
    this.visible_1 = false;
  };
  function _get_parsedBaseLibraryVersion__44h3pw($this) {
    var tmp0 = $this.parsedBaseLibraryVersion$delegate_1;
    var tmp = KProperty1;
    // Inline function 'kotlin.getValue' call
    getPropertyCallableRef('parsedBaseLibraryVersion', 1, tmp, WechatRuntimeInfo$_get_parsedBaseLibraryVersion_$ref_xmes1t(), null);
    return tmp0.get_value_j01efc_k$();
  }
  function _get_reportedPlatform__hgio63($this) {
    var tmp0 = $this.reportedPlatform$delegate_1;
    var tmp = KProperty1;
    // Inline function 'kotlin.getValue' call
    getPropertyCallableRef('reportedPlatform', 1, tmp, WechatRuntimeInfo$_get_reportedPlatform_$ref_mmka14(), null);
    return tmp0.get_value_j01efc_k$();
  }
  function Companion_8() {
    this.DEVELOPER_TOOLS_PLATFORM_1 = 'devtools';
  }
  var Companion_instance_10;
  function Companion_getInstance_8() {
    return Companion_instance_10;
  }
  function WechatRuntimeInfo$parsedBaseLibraryVersion$delegate$lambda(this$0) {
    return function () {
      var tmp0_safe_receiver = this$0.host_1.baseLibraryVersion_8f7rqq_k$();
      var tmp;
      if (tmp0_safe_receiver == null) {
        tmp = null;
      } else {
        // Inline function 'kotlin.let' call
        tmp = Companion_instance_7.parse_pc1q8p_k$(tmp0_safe_receiver);
      }
      return tmp;
    };
  }
  function WechatRuntimeInfo$_get_parsedBaseLibraryVersion_$ref_xmes1t() {
    return constructCallableReference(function (p0) {
      return _get_parsedBaseLibraryVersion__44h3pw(p0);
    }, 1, 0, 6);
  }
  function WechatRuntimeInfo$reportedPlatform$delegate$lambda(this$0) {
    return function () {
      return this$0.host_1.platform_v04zg3_k$();
    };
  }
  function WechatRuntimeInfo$_get_reportedPlatform_$ref_mmka14() {
    return constructCallableReference(function (p0) {
      return _get_reportedPlatform__hgio63(p0);
    }, 1, 0, 7);
  }
  function WechatRuntimeInfo(host) {
    this.host_1 = host;
    var tmp = this;
    tmp.parsedBaseLibraryVersion$delegate_1 = lazy(WechatRuntimeInfo$parsedBaseLibraryVersion$delegate$lambda(this));
    var tmp_0 = this;
    tmp_0.reportedPlatform$delegate_1 = lazy(WechatRuntimeInfo$reportedPlatform$delegate$lambda(this));
  }
  protoOf(WechatRuntimeInfo).get_baseLibraryVersion_i4tt1z_k$ = function () {
    return _get_parsedBaseLibraryVersion__44h3pw(this);
  };
  protoOf(WechatRuntimeInfo).get_platform_ssr7o_k$ = function () {
    return _get_reportedPlatform__hgio63(this);
  };
  protoOf(WechatRuntimeInfo).get_isDeveloperTools_3vcog4_k$ = function () {
    return _get_reportedPlatform__hgio63(this) === 'devtools';
  };
  protoOf(WechatRuntimeInfo).canIUse_xcpuhg_k$ = function (schema) {
    return this.host_1.canIUse_xcpuhg_k$(schema);
  };
  //region block: init
  Supported_instance = new Supported();
  Unsupported_instance = new Unsupported();
  NotRequested_instance = new NotRequested();
  Granted_instance = new Granted();
  Denied_instance = new Denied();
  Authorized_instance = new Authorized();
  Refused_instance = new Refused();
  Companion_instance_7 = new Companion_5();
  WxAuthHost_instance = new WxAuthHost();
  WxClipboardHost_instance = new WxClipboardHost();
  Refused_instance_0 = new Refused_0();
  NotFound_instance = new NotFound();
  WxFileSystemHost_instance = new WxFileSystemHost();
  WxHapticsHost_instance = new WxHapticsHost();
  WxNavigationHost_instance = new WxNavigationHost();
  WxNetworkHost_instance = new WxNetworkHost();
  WxPermissionHost_instance = new WxPermissionHost();
  WxPrivacyHost_instance = new WxPrivacyHost();
  WxRuntimeInfoHost_instance = new WxRuntimeInfoHost();
  Companion_instance_9 = new Companion_7();
  WxStorageHost_instance = new WxStorageHost();
  Unreadable_instance = new Unreadable();
  Unreadable_instance_0 = new Unreadable_0();
  Absent_instance = new Absent();
  Unreadable_instance_1 = new Unreadable_1();
  Unreadable_instance_2 = new Unreadable_2();
  Companion_instance_10 = new Companion_8();
  //endregion
  //region block: exports
  function $jsExportAll$(_) {
    var io = _.io || (_.io = {});
    var github = io.github || (io.github = {});
    var bobcgn = github.bobcgn || (github.bobcgn = {});
    var miniapp = bobcgn.miniapp || (bobcgn.miniapp = {});
    var export_0 = miniapp.export || (miniapp.export = {});
    export_0.JsCapabilitySupport = JsCapabilitySupport;
    var io_0 = _.io || (_.io = {});
    var github_0 = io_0.github || (io_0.github = {});
    var bobcgn_0 = github_0.bobcgn || (github_0.bobcgn = {});
    var miniapp_0 = bobcgn_0.miniapp || (bobcgn_0.miniapp = {});
    var export_1 = miniapp_0.export || (miniapp_0.export = {});
    export_1.JsPrivacyStatus = JsPrivacyStatus;
    var io_1 = _.io || (_.io = {});
    var github_1 = io_1.github || (io_1.github = {});
    var bobcgn_1 = github_1.bobcgn || (github_1.bobcgn = {});
    var miniapp_1 = bobcgn_1.miniapp || (bobcgn_1.miniapp = {});
    var export_2 = miniapp_1.export || (miniapp_1.export = {});
    export_2.JsRuntimeInfo = JsRuntimeInfo;
    var io_2 = _.io || (_.io = {});
    var github_2 = io_2.github || (io_2.github = {});
    var bobcgn_2 = github_2.bobcgn || (github_2.bobcgn = {});
    var miniapp_2 = bobcgn_2.miniapp || (bobcgn_2.miniapp = {});
    var export_3 = miniapp_2.export || (miniapp_2.export = {});
    defineProp(export_3, 'MiniAppExports', MiniAppExports_getInstance, VOID, true);
    var io_3 = _.io || (_.io = {});
    var github_3 = io_3.github || (io_3.github = {});
    var bobcgn_3 = github_3.bobcgn || (github_3.bobcgn = {});
    var miniapp_3 = bobcgn_3.miniapp || (bobcgn_3.miniapp = {});
    var export_4 = miniapp_3.export || (miniapp_3.export = {});
    export_4.MiniAppHttpResult = MiniAppHttpResult;
    var io_4 = _.io || (_.io = {});
    var github_4 = io_4.github || (io_4.github = {});
    var bobcgn_4 = github_4.bobcgn || (github_4.bobcgn = {});
    var miniapp_4 = bobcgn_4.miniapp || (bobcgn_4.miniapp = {});
    var host = miniapp_4.host || (miniapp_4.host = {});
    var wechat = host.wechat || (host.wechat = {});
    wechat.WeChatLoginResult = WeChatLoginResult;
  }
  $jsExportAll$(_);
  //endregion
  return _;
}(module.exports, require('./kotlin-kotlin-stdlib.js'), require('./kotlinx-coroutines-core.js')));

//# sourceMappingURL=kmp-miniapp-sdk-kotlin.js.map
