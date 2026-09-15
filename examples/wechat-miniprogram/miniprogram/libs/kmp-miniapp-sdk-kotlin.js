(function (_, kotlin_kotlin, kotlin_org_jetbrains_kotlinx_kotlinx_coroutines_core) {
  'use strict';
  //region block: imports
  var protoOf = kotlin_kotlin.$_$.e2;
  var initMetadataForInterface = kotlin_kotlin.$_$.b2;
  var Unit_instance = kotlin_kotlin.$_$.g;
  var Companion_instance = kotlin_kotlin.$_$.f;
  var _Result___init__impl__xyqfz8 = kotlin_kotlin.$_$.b;
  var createFailure = kotlin_kotlin.$_$.q2;
  var CoroutineImpl = kotlin_kotlin.$_$.l1;
  var intercepted = kotlin_kotlin.$_$.d1;
  var CancellableContinuationImpl = kotlin_org_jetbrains_kotlinx_kotlinx_coroutines_core.$_$.b;
  var returnIfSuspended = kotlin_kotlin.$_$.i;
  var get_COROUTINE_SUSPENDED = kotlin_kotlin.$_$.c1;
  var initMetadataForCoroutine = kotlin_kotlin.$_$.a2;
  var Enum = kotlin_kotlin.$_$.j2;
  var initMetadataForClass = kotlin_kotlin.$_$.y1;
  var VOID = kotlin_kotlin.$_$.a;
  var enumEntries = kotlin_kotlin.$_$.m1;
  var emptyMap = kotlin_kotlin.$_$.z;
  var captureStack = kotlin_kotlin.$_$.t1;
  var Exception = kotlin_kotlin.$_$.l2;
  var Exception_init_$Init$ = kotlin_kotlin.$_$.q;
  var await_0 = kotlin_kotlin.$_$.h;
  var ArrayList_init_$Create$ = kotlin_kotlin.$_$.k;
  var listOf = kotlin_kotlin.$_$.a1;
  var addAll = kotlin_kotlin.$_$.x;
  var copyToArray = kotlin_kotlin.$_$.y;
  var promisify = kotlin_kotlin.$_$.e1;
  var initMetadataForObject = kotlin_kotlin.$_$.c2;
  var toString = kotlin_kotlin.$_$.f2;
  var IllegalArgumentException_init_$Create$ = kotlin_kotlin.$_$.r;
  var LinkedHashMap_init_$Create$ = kotlin_kotlin.$_$.l;
  var defineProp = kotlin_kotlin.$_$.v1;
  var isBlank = kotlin_kotlin.$_$.i2;
  var to = kotlin_kotlin.$_$.t2;
  var mapOf = kotlin_kotlin.$_$.b1;
  var contains = kotlin_kotlin.$_$.h2;
  var equals = kotlin_kotlin.$_$.w1;
  var FunctionAdapter = kotlin_kotlin.$_$.r1;
  var isInterface = kotlin_kotlin.$_$.d2;
  var hashCode = kotlin_kotlin.$_$.x1;
  var initMetadataForCompanion = kotlin_kotlin.$_$.z1;
  var MutableStateFlow = kotlin_org_jetbrains_kotlinx_kotlinx_coroutines_core.$_$.a;
  //endregion
  //region block: pre-declaration
  initMetadataForInterface(HostOperationAborter, 'HostOperationAborter');
  initMetadataForCoroutine($awaitHostCallbackCOROUTINE$, CoroutineImpl);
  initMetadataForClass(MiniAppLifecycleState, 'MiniAppLifecycleState', VOID, Enum);
  initMetadataForClass(HttpMethod, 'HttpMethod', VOID, Enum);
  initMetadataForClass(MiniAppHttpRequest, 'MiniAppHttpRequest');
  initMetadataForClass(MiniAppHttpResponse, 'MiniAppHttpResponse');
  initMetadataForClass(MiniAppException, 'MiniAppException', VOID, Exception);
  initMetadataForClass(Timeout, 'Timeout', VOID, MiniAppException);
  initMetadataForClass(HostFailure, 'HostFailure', VOID, MiniAppException);
  initMetadataForClass(InvalidResponse, 'InvalidResponse', VOID, MiniAppException);
  initMetadataForClass(InternalFailure, 'InternalFailure', VOID, MiniAppException);
  initMetadataForCoroutine($storageGet$suspendBridgeCOROUTINE$, CoroutineImpl);
  initMetadataForCoroutine($storageSet$suspendBridgeCOROUTINE$, CoroutineImpl);
  initMetadataForCoroutine($storageRemove$suspendBridgeCOROUTINE$, CoroutineImpl);
  initMetadataForCoroutine($wechatLogin$suspendBridgeCOROUTINE$, CoroutineImpl);
  initMetadataForCoroutine($networkRequestCOROUTINE$, CoroutineImpl);
  initMetadataForCoroutine($networkRequest$suspendBridgeCOROUTINE$, CoroutineImpl);
  initMetadataForCoroutine($wechatNavigateTo$suspendBridgeCOROUTINE$, CoroutineImpl);
  initMetadataForCoroutine($wechatRedirectTo$suspendBridgeCOROUTINE$, CoroutineImpl);
  initMetadataForCoroutine($wechatNavigateBack$suspendBridgeCOROUTINE$, CoroutineImpl);
  initMetadataForObject(MiniAppExports, 'MiniAppExports', VOID, VOID, VOID, [1, 2, 0, 5]);
  initMetadataForClass(MiniAppHttpResult, 'MiniAppHttpResult');
  initMetadataForClass(WeChatLoginResult, 'WeChatLoginResult');
  initMetadataForClass(WechatPlatformApi, 'WechatPlatformApi');
  initMetadataForClass(WechatHost, 'WechatHost', WechatHost);
  initMetadataForClass(WechatAuth, 'WechatAuth', WechatAuth, VOID, VOID, [0]);
  initMetadataForObject(WxAuthHost, 'WxAuthHost');
  initMetadataForClass(WechatNavigation, 'WechatNavigation', WechatNavigation, VOID, VOID, [1]);
  initMetadataForObject(WxNavigationHost, 'WxNavigationHost');
  initMetadataForClass(sam$io_github_bobcgn_miniapp_async_HostOperationAborter$0, 'sam$io_github_bobcgn_miniapp_async_HostOperationAborter$0', VOID, VOID, [HostOperationAborter, FunctionAdapter]);
  initMetadataForClass(WechatNetwork, 'WechatNetwork', WechatNetwork, VOID, VOID, [1]);
  initMetadataForObject(WxNetworkHost, 'WxNetworkHost');
  initMetadataForCompanion(Companion);
  initMetadataForClass(WechatStorage, 'WechatStorage', WechatStorage, VOID, VOID, [1, 2]);
  initMetadataForObject(WxStorageHost, 'WxStorageHost');
  initMetadataForClass(WechatAppLifecycle, 'WechatAppLifecycle', WechatAppLifecycle);
  initMetadataForClass(WechatPageLifecycle, 'WechatPageLifecycle', WechatPageLifecycle);
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
            var destination = ArrayList_init_$Create$();
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
  function MiniAppExports() {
    MiniAppExports_instance = this;
    this.host_1 = new WechatHost();
    this.storage_1 = this.host_1.storage_1;
    this.network_1 = this.host_1.network_1;
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
  var MiniAppExports_instance;
  function MiniAppExports_getInstance() {
    if (MiniAppExports_instance == null)
      new MiniAppExports();
    return MiniAppExports_instance;
  }
  function headerMap(headers) {
    // Inline function 'kotlin.require' call
    if (!((headers.length % 2 | 0) === 0)) {
      var message = 'headers must contain alternating name and value entries';
      throw IllegalArgumentException_init_$Create$(toString(message));
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
  function WeChatLoginResult(code) {
    this.code = code;
  }
  protoOf(WeChatLoginResult).get_code_wok7xy_k$ = function () {
    return this.code;
  };
  function WechatPlatformApi(auth, navigation, appLifecycle, pageLifecycle) {
    this.auth_1 = auth;
    this.navigation_1 = navigation;
    this.appLifecycle_1 = appLifecycle;
    this.pageLifecycle_1 = pageLifecycle;
  }
  function WechatHost(storageHost, authHost, networkHost, navigationHost) {
    storageHost = storageHost === VOID ? WxStorageHost_instance : storageHost;
    authHost = authHost === VOID ? WxAuthHost_instance : authHost;
    networkHost = networkHost === VOID ? WxNetworkHost_instance : networkHost;
    navigationHost = navigationHost === VOID ? WxNavigationHost_instance : navigationHost;
    this.appLifecycle_1 = new WechatAppLifecycle();
    this.platform_1 = new WechatPlatformApi(new WechatAuth(authHost), new WechatNavigation(navigationHost), this.appLifecycle_1, new WechatPageLifecycle());
    this.storage_1 = new WechatStorage(storageHost);
    this.network_1 = new WechatNetwork(networkHost);
    this.lifecycle_1 = this.appLifecycle_1;
  }
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
  function WechatAuth(host) {
    host = host === VOID ? WxAuthHost_instance : host;
    this.host_1 = host;
  }
  protoOf(WechatAuth).login_hvnjm8_k$ = function ($completion) {
    return awaitHostCallback(WechatAuth$login$lambda(this), $completion);
  };
  function WxAuthHost() {
  }
  protoOf(WxAuthHost).login_mku97k_k$ = function (success, failure) {
    var options = wxLoginOptions();
    options.success = success;
    options.fail = failure;
    wx.login(options);
  };
  var WxAuthHost_instance;
  function WxAuthHost_getInstance() {
    return WxAuthHost_instance;
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
  function Companion() {
    this.MISSING_KEY_ERROR_1 = 'getStorage:fail data not found';
  }
  var Companion_instance_0;
  function Companion_getInstance() {
    return Companion_instance_0;
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
  //region block: init
  WxAuthHost_instance = new WxAuthHost();
  WxNavigationHost_instance = new WxNavigationHost();
  WxNetworkHost_instance = new WxNetworkHost();
  Companion_instance_0 = new Companion();
  WxStorageHost_instance = new WxStorageHost();
  //endregion
  //region block: exports
  function $jsExportAll$(_) {
    var io = _.io || (_.io = {});
    var github = io.github || (io.github = {});
    var bobcgn = github.bobcgn || (github.bobcgn = {});
    var miniapp = bobcgn.miniapp || (bobcgn.miniapp = {});
    var export_0 = miniapp.export || (miniapp.export = {});
    defineProp(export_0, 'MiniAppExports', MiniAppExports_getInstance, VOID, true);
    var io_0 = _.io || (_.io = {});
    var github_0 = io_0.github || (io_0.github = {});
    var bobcgn_0 = github_0.bobcgn || (github_0.bobcgn = {});
    var miniapp_0 = bobcgn_0.miniapp || (bobcgn_0.miniapp = {});
    var export_1 = miniapp_0.export || (miniapp_0.export = {});
    export_1.MiniAppHttpResult = MiniAppHttpResult;
    var io_1 = _.io || (_.io = {});
    var github_1 = io_1.github || (io_1.github = {});
    var bobcgn_1 = github_1.bobcgn || (github_1.bobcgn = {});
    var miniapp_1 = bobcgn_1.miniapp || (bobcgn_1.miniapp = {});
    var host = miniapp_1.host || (miniapp_1.host = {});
    var wechat = host.wechat || (host.wechat = {});
    wechat.WeChatLoginResult = WeChatLoginResult;
  }
  $jsExportAll$(_);
  //endregion
  return _;
}(module.exports, require('./kotlin-kotlin-stdlib.js'), require('./kotlinx-coroutines-core.js')));

//# sourceMappingURL=kmp-miniapp-sdk-kotlin.js.map
