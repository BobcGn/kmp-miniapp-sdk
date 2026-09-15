(function (_, kotlin_kotlin, kotlin_org_jetbrains_kotlinx_atomicfu) {
  'use strict';
  //region block: imports
  var imul = Math.imul;
  var UnsupportedOperationException_init_$Create$ = kotlin_kotlin.$_$.w;
  var Unit_instance = kotlin_kotlin.$_$.g;
  var isInterface = kotlin_kotlin.$_$.d2;
  var THROW_CCE = kotlin_kotlin.$_$.o2;
  var toString = kotlin_kotlin.$_$.f2;
  var IllegalStateException_init_$Create$ = kotlin_kotlin.$_$.s;
  var VOID = kotlin_kotlin.$_$.a;
  var toString_0 = kotlin_kotlin.$_$.s2;
  var atomic$int$1 = kotlin_org_jetbrains_kotlinx_atomicfu.$_$.c;
  var atomic$ref$1 = kotlin_org_jetbrains_kotlinx_atomicfu.$_$.b;
  var protoOf = kotlin_kotlin.$_$.e2;
  var get_COROUTINE_SUSPENDED = kotlin_kotlin.$_$.c1;
  var Continuation = kotlin_kotlin.$_$.j1;
  var initMetadataForClass = kotlin_kotlin.$_$.y1;
  var initMetadataForInterface = kotlin_kotlin.$_$.b2;
  var initMetadataForObject = kotlin_kotlin.$_$.c2;
  var hashCode = kotlin_kotlin.$_$.x1;
  var equals = kotlin_kotlin.$_$.w1;
  var atomic$boolean$1 = kotlin_org_jetbrains_kotlinx_atomicfu.$_$.a;
  var CancellationException_init_$Create$ = kotlin_kotlin.$_$.n;
  var Result__exceptionOrNull_impl_p6xea9 = kotlin_kotlin.$_$.c;
  var _Result___get_value__impl__bjfvqg = kotlin_kotlin.$_$.d;
  var AbstractCoroutineContextKey = kotlin_kotlin.$_$.g1;
  var Key_instance = kotlin_kotlin.$_$.e;
  var AbstractCoroutineContextElement = kotlin_kotlin.$_$.f1;
  var get = kotlin_kotlin.$_$.h1;
  var ContinuationInterceptor = kotlin_kotlin.$_$.i1;
  var RuntimeException_init_$Create$ = kotlin_kotlin.$_$.v;
  var addSuppressed = kotlin_kotlin.$_$.p2;
  var Long = kotlin_kotlin.$_$.m2;
  var ArrayDeque_init_$Create$ = kotlin_kotlin.$_$.j;
  var compare = kotlin_kotlin.$_$.p1;
  var add = kotlin_kotlin.$_$.o1;
  var subtract = kotlin_kotlin.$_$.q1;
  var RuntimeException = kotlin_kotlin.$_$.n2;
  var RuntimeException_init_$Init$ = kotlin_kotlin.$_$.u;
  var captureStack = kotlin_kotlin.$_$.t1;
  var Error_0 = kotlin_kotlin.$_$.k2;
  var Error_init_$Init$ = kotlin_kotlin.$_$.p;
  var StringBuilder_init_$Create$ = kotlin_kotlin.$_$.o;
  var throwUninitializedPropertyAccessException = kotlin_kotlin.$_$.n1;
  var Element = kotlin_kotlin.$_$.k1;
  var anyToString = kotlin_kotlin.$_$.s1;
  var constructCallableReference = kotlin_kotlin.$_$.u1;
  var Exception = kotlin_kotlin.$_$.l2;
  var IllegalArgumentException_init_$Create$ = kotlin_kotlin.$_$.r;
  var Companion_instance = kotlin_kotlin.$_$.f;
  var _Result___init__impl__xyqfz8 = kotlin_kotlin.$_$.b;
  var createFailure = kotlin_kotlin.$_$.q2;
  var Exception_init_$Init$ = kotlin_kotlin.$_$.q;
  var defineProp = kotlin_kotlin.$_$.v1;
  var getKClassFromExpression = kotlin_kotlin.$_$.g2;
  var RuntimeException_init_$Init$_0 = kotlin_kotlin.$_$.t;
  var LinkedHashSet_init_$Create$ = kotlin_kotlin.$_$.m;
  var stackTraceToString = kotlin_kotlin.$_$.r2;
  //endregion
  //region block: pre-declaration
  initMetadataForClass(SchedulerTask, 'SchedulerTask');
  initMetadataForClass(DispatchedTask, 'DispatchedTask', VOID, SchedulerTask);
  initMetadataForClass(CancellableContinuationImpl, 'CancellableContinuationImpl', VOID, DispatchedTask, [Continuation]);
  initMetadataForInterface(NotCompleted, 'NotCompleted');
  initMetadataForInterface(CancelHandler, 'CancelHandler', VOID, VOID, [NotCompleted]);
  initMetadataForClass(UserSupplied, 'UserSupplied', VOID, VOID, [CancelHandler]);
  initMetadataForObject(Active, 'Active', VOID, VOID, [NotCompleted]);
  initMetadataForClass(CompletedContinuation, 'CompletedContinuation');
  initMetadataForClass(LockFreeLinkedListNode, 'LockFreeLinkedListNode', LockFreeLinkedListNode);
  initMetadataForInterface(Incomplete, 'Incomplete');
  initMetadataForClass(JobNode, 'JobNode', VOID, LockFreeLinkedListNode, [Incomplete]);
  initMetadataForClass(ChildContinuation, 'ChildContinuation', VOID, JobNode);
  initMetadataForClass(CompletedExceptionally, 'CompletedExceptionally');
  initMetadataForClass(CancelledContinuation, 'CancelledContinuation', VOID, CompletedExceptionally);
  initMetadataForObject(Key, 'Key', VOID, AbstractCoroutineContextKey);
  initMetadataForClass(CoroutineDispatcher, 'CoroutineDispatcher', VOID, AbstractCoroutineContextElement, [ContinuationInterceptor]);
  initMetadataForObject(Key_0, 'Key');
  initMetadataForClass(EventLoop, 'EventLoop', VOID, CoroutineDispatcher);
  initMetadataForObject(ThreadLocalEventLoop, 'ThreadLocalEventLoop');
  initMetadataForClass(CompletionHandlerException, 'CompletionHandlerException', VOID, RuntimeException);
  initMetadataForClass(CoroutinesInternalError, 'CoroutinesInternalError', VOID, Error_0);
  initMetadataForObject(Key_1, 'Key');
  initMetadataForObject(NonDisposableHandle, 'NonDisposableHandle');
  initMetadataForClass(Empty, 'Empty', VOID, VOID, [Incomplete]);
  initMetadataForClass(LockFreeLinkedListHead, 'LockFreeLinkedListHead', LockFreeLinkedListHead, LockFreeLinkedListNode);
  initMetadataForClass(NodeList, 'NodeList', NodeList, LockFreeLinkedListHead, [Incomplete]);
  initMetadataForClass(SynchronizedObject, 'SynchronizedObject', SynchronizedObject);
  initMetadataForClass(Finishing, 'Finishing', VOID, SynchronizedObject, [Incomplete]);
  initMetadataForClass(JobSupport, 'JobSupport', VOID, VOID, [Element], [0]);
  initMetadataForClass(InactiveNodeList, 'InactiveNodeList', VOID, VOID, [Incomplete]);
  initMetadataForClass(AbstractSharedFlow, 'AbstractSharedFlow', VOID, SynchronizedObject);
  initMetadataForClass(StateFlowImpl, 'StateFlowImpl', VOID, AbstractSharedFlow, VOID, [1]);
  initMetadataForClass(ConcurrentLinkedListNode, 'ConcurrentLinkedListNode');
  initMetadataForClass(Segment, 'Segment', VOID, ConcurrentLinkedListNode, [NotCompleted]);
  initMetadataForObject(ExceptionSuccessfullyProcessed, 'ExceptionSuccessfullyProcessed', VOID, Exception);
  initMetadataForClass(DispatchedContinuation, 'DispatchedContinuation', VOID, DispatchedTask, [Continuation]);
  initMetadataForClass(DispatchException, 'DispatchException', VOID, Exception);
  initMetadataForClass(Symbol, 'Symbol');
  initMetadataForClass(DiagnosticCoroutineContextException, 'DiagnosticCoroutineContextException', VOID, RuntimeException);
  initMetadataForClass(ListClosed, 'ListClosed', VOID, LockFreeLinkedListNode);
  initMetadataForClass(CommonThreadLocal, 'CommonThreadLocal', CommonThreadLocal);
  initMetadataForClass(UnconfinedEventLoop, 'UnconfinedEventLoop', UnconfinedEventLoop, EventLoop);
  //endregion
  function invokeOnCancellation(_this__u8e3s4, handler) {
    var tmp;
    if (_this__u8e3s4 instanceof CancellableContinuationImpl) {
      _this__u8e3s4.invokeOnCancellationInternal_vx7l43_k$(handler);
      tmp = Unit_instance;
    } else {
      throw UnsupportedOperationException_init_$Create$('third-party implementation of CancellableContinuation is not supported');
    }
    return tmp;
  }
  function _get_parentHandle__f8dcex($this) {
    return $this._parentHandle_1.kotlinx$atomicfu$value;
  }
  function _get_stateDebugRepresentation__bf18u4($this) {
    var tmp0_subject = $this.get_state_2t6sbp_k$();
    var tmp;
    if (!(tmp0_subject == null) ? isInterface(tmp0_subject, NotCompleted) : false) {
      tmp = 'Active';
    } else {
      if (tmp0_subject instanceof CancelledContinuation) {
        tmp = 'Cancelled';
      } else {
        tmp = 'Completed';
      }
    }
    return tmp;
  }
  function isReusable($this) {
    var tmp;
    if (get_isReusableMode($this.resumeMode_1)) {
      var tmp_0 = $this.delegate_1;
      tmp = (tmp_0 instanceof DispatchedContinuation ? tmp_0 : THROW_CCE()).isReusable_asltyw_k$();
    } else {
      tmp = false;
    }
    return tmp;
  }
  function cancelLater($this, cause) {
    if (!isReusable($this))
      return false;
    var tmp = $this.delegate_1;
    var dispatched = tmp instanceof DispatchedContinuation ? tmp : THROW_CCE();
    return dispatched.postponeCancellation_hjv3hh_k$(cause);
  }
  function callSegmentOnCancellation($this, segment, cause) {
    // Inline function 'kotlinx.coroutines.index' call
    var index = $this._decisionAndIndex_1.kotlinx$atomicfu$value & 536870911;
    // Inline function 'kotlin.check' call
    if (!!(index === 536870911)) {
      var message = 'The index for Segment.onCancellation(..) is broken';
      throw IllegalStateException_init_$Create$(toString(message));
    }
    // Inline function 'kotlinx.coroutines.CancellableContinuationImpl.callCancelHandlerSafely' call
    try {
      segment.onCancellation_4jec3b_k$(index, cause, $this.get_context_h02k06_k$());
    } catch ($p) {
      if ($p instanceof Error) {
        var ex = $p;
        handleCoroutineException($this.get_context_h02k06_k$(), new CompletionHandlerException('Exception in invokeOnCancellation handler for ' + $this.toString(), ex));
      } else {
        throw $p;
      }
    }
  }
  function trySuspend($this) {
    // Inline function 'kotlinx.atomicfu.loop' call
    var this_0 = $this._decisionAndIndex_1;
    while (true) {
      var cur = this_0.kotlinx$atomicfu$value;
      // Inline function 'kotlinx.coroutines.decision' call
      switch (cur >> 29) {
        case 0:
          // Inline function 'kotlinx.coroutines.index' call

          // Inline function 'kotlinx.coroutines.decisionAndIndex' call

          var tmp$ret$4 = (1 << 29) + (cur & 536870911) | 0;
          if ($this._decisionAndIndex_1.atomicfu$compareAndSet(cur, tmp$ret$4))
            return true;
          break;
        case 2:
          return false;
        default:
          // Inline function 'kotlin.error' call

          var message = 'Already suspended';
          throw IllegalStateException_init_$Create$(toString(message));
      }
    }
  }
  function tryResume($this) {
    // Inline function 'kotlinx.atomicfu.loop' call
    var this_0 = $this._decisionAndIndex_1;
    while (true) {
      var cur = this_0.kotlinx$atomicfu$value;
      // Inline function 'kotlinx.coroutines.decision' call
      switch (cur >> 29) {
        case 0:
          // Inline function 'kotlinx.coroutines.index' call

          // Inline function 'kotlinx.coroutines.decisionAndIndex' call

          var tmp$ret$4 = (2 << 29) + (cur & 536870911) | 0;
          if ($this._decisionAndIndex_1.atomicfu$compareAndSet(cur, tmp$ret$4))
            return true;
          break;
        case 1:
          return false;
        default:
          // Inline function 'kotlin.error' call

          var message = 'Already resumed';
          throw IllegalStateException_init_$Create$(toString(message));
      }
    }
  }
  function installParentHandle($this) {
    var tmp0_elvis_lhs = $this.get_context_h02k06_k$().get_y2st91_k$(Key_instance_2);
    var tmp;
    if (tmp0_elvis_lhs == null) {
      return null;
    } else {
      tmp = tmp0_elvis_lhs;
    }
    var parent = tmp;
    var handle = invokeOnCompletion(parent, VOID, new ChildContinuation($this));
    $this._parentHandle_1.atomicfu$compareAndSet(null, handle);
    return handle;
  }
  function invokeOnCancellationImpl($this, handler) {
    // Inline function 'kotlinx.coroutines.assert' call
    // Inline function 'kotlinx.atomicfu.loop' call
    var this_0 = $this._state_1;
    while (true) {
      var state = this_0.kotlinx$atomicfu$value;
      if (state instanceof Active) {
        if ($this._state_1.atomicfu$compareAndSet(state, handler))
          return Unit_instance;
      } else {
        var tmp;
        if (!(state == null) ? isInterface(state, CancelHandler) : false) {
          tmp = true;
        } else {
          tmp = state instanceof Segment;
        }
        if (tmp) {
          multipleHandlersError($this, handler, state);
        } else {
          if (state instanceof CompletedExceptionally) {
            if (!state.makeHandled_ws9oq6_k$()) {
              multipleHandlersError($this, handler, state);
            }
            if (state instanceof CancelledContinuation) {
              var tmp1_safe_receiver = state instanceof CompletedExceptionally ? state : null;
              var cause = tmp1_safe_receiver == null ? null : tmp1_safe_receiver.cause_1;
              if (isInterface(handler, CancelHandler)) {
                $this.callCancelHandler_e6l0np_k$(handler, cause);
              } else {
                var segment = handler instanceof Segment ? handler : THROW_CCE();
                callSegmentOnCancellation($this, segment, cause);
              }
            }
            return Unit_instance;
          } else {
            if (state instanceof CompletedContinuation) {
              if (!(state.cancelHandler_1 == null)) {
                multipleHandlersError($this, handler, state);
              }
              if (handler instanceof Segment)
                return Unit_instance;
              if (!isInterface(handler, CancelHandler))
                THROW_CCE();
              if (state.get_cancelled_ge9r54_k$()) {
                $this.callCancelHandler_e6l0np_k$(handler, state.cancelCause_1);
                return Unit_instance;
              }
              var update = state.copy$default_uedmwo_k$(VOID, handler);
              if ($this._state_1.atomicfu$compareAndSet(state, update))
                return Unit_instance;
            } else {
              if (handler instanceof Segment)
                return Unit_instance;
              if (!isInterface(handler, CancelHandler))
                THROW_CCE();
              var update_0 = new CompletedContinuation(state, handler);
              if ($this._state_1.atomicfu$compareAndSet(state, update_0))
                return Unit_instance;
            }
          }
        }
      }
    }
  }
  function multipleHandlersError($this, handler, state) {
    // Inline function 'kotlin.error' call
    var message = "It's prohibited to register multiple handlers, tried to register " + toString(handler) + ', already has ' + toString_0(state);
    throw IllegalStateException_init_$Create$(toString(message));
  }
  function dispatchResume($this, mode) {
    if (tryResume($this))
      return Unit_instance;
    dispatch($this, mode);
  }
  function resumedState($this, state, proposedUpdate, resumeMode, onCancellation, idempotent) {
    var tmp;
    if (proposedUpdate instanceof CompletedExceptionally) {
      // Inline function 'kotlinx.coroutines.assert' call
      // Inline function 'kotlinx.coroutines.assert' call
      tmp = proposedUpdate;
    } else {
      if (!get_isCancellableMode(resumeMode) && idempotent == null) {
        tmp = proposedUpdate;
      } else {
        var tmp_0;
        var tmp_1;
        if (!(onCancellation == null)) {
          tmp_1 = true;
        } else {
          tmp_1 = isInterface(state, CancelHandler);
        }
        if (tmp_1) {
          tmp_0 = true;
        } else {
          tmp_0 = !(idempotent == null);
        }
        if (tmp_0) {
          tmp = new CompletedContinuation(proposedUpdate, isInterface(state, CancelHandler) ? state : null, onCancellation, idempotent);
        } else {
          tmp = proposedUpdate;
        }
      }
    }
    return tmp;
  }
  function alreadyResumedError($this, proposedUpdate) {
    // Inline function 'kotlin.error' call
    var message = 'Already resumed, but proposed with update ' + toString_0(proposedUpdate);
    throw IllegalStateException_init_$Create$(toString(message));
  }
  function detachChildIfNonReusable($this) {
    if (!isReusable($this)) {
      $this.detachChild_85lap8_k$();
    }
  }
  function CancellableContinuationImpl(delegate, resumeMode) {
    DispatchedTask.call(this, resumeMode);
    this.delegate_1 = delegate;
    // Inline function 'kotlinx.coroutines.assert' call
    this.context_1 = this.delegate_1.get_context_h02k06_k$();
    var tmp = this;
    // Inline function 'kotlinx.coroutines.decisionAndIndex' call
    var tmp$ret$1 = (0 << 29) + 536870911 | 0;
    tmp._decisionAndIndex_1 = atomic$int$1(tmp$ret$1);
    this._state_1 = atomic$ref$1(Active_instance);
    this._parentHandle_1 = atomic$ref$1(null);
  }
  protoOf(CancellableContinuationImpl).get_delegate_hasf9b_k$ = function () {
    return this.delegate_1;
  };
  protoOf(CancellableContinuationImpl).get_context_h02k06_k$ = function () {
    return this.context_1;
  };
  protoOf(CancellableContinuationImpl).get_state_2t6sbp_k$ = function () {
    return this._state_1.kotlinx$atomicfu$value;
  };
  protoOf(CancellableContinuationImpl).get_isActive_quafmh_k$ = function () {
    var tmp = this.get_state_2t6sbp_k$();
    return !(tmp == null) ? isInterface(tmp, NotCompleted) : false;
  };
  protoOf(CancellableContinuationImpl).get_isCompleted_a6j6c8_k$ = function () {
    var tmp = this.get_state_2t6sbp_k$();
    return !(!(tmp == null) ? isInterface(tmp, NotCompleted) : false);
  };
  protoOf(CancellableContinuationImpl).initCancellability_shqc60_k$ = function () {
    var tmp0_elvis_lhs = installParentHandle(this);
    var tmp;
    if (tmp0_elvis_lhs == null) {
      return Unit_instance;
    } else {
      tmp = tmp0_elvis_lhs;
    }
    var handle = tmp;
    if (this.get_isCompleted_a6j6c8_k$()) {
      handle.dispose_3nnxhr_k$();
      this._parentHandle_1.kotlinx$atomicfu$value = NonDisposableHandle_instance;
    }
  };
  protoOf(CancellableContinuationImpl).takeState_a1bv3x_k$ = function () {
    return this.get_state_2t6sbp_k$();
  };
  protoOf(CancellableContinuationImpl).cancelCompletedResult_pnx7en_k$ = function (takenState, cause) {
    var tmp$ret$0;
    // Inline function 'kotlinx.atomicfu.loop' call
    var this_0 = this._state_1;
    while (true) {
      var state = this_0.kotlinx$atomicfu$value;
      if (!(state == null) ? isInterface(state, NotCompleted) : false) {
        // Inline function 'kotlin.error' call
        var message = 'Not completed';
        throw IllegalStateException_init_$Create$(toString(message));
      } else {
        if (state instanceof CompletedExceptionally)
          return Unit_instance;
        else {
          if (state instanceof CompletedContinuation) {
            // Inline function 'kotlin.check' call
            if (!!state.get_cancelled_ge9r54_k$()) {
              var message_0 = 'Must be called at most once';
              throw IllegalStateException_init_$Create$(toString(message_0));
            }
            var update = state.copy$default_uedmwo_k$(VOID, VOID, VOID, VOID, cause);
            if (this._state_1.atomicfu$compareAndSet(state, update)) {
              state.invokeHandlers_gry311_k$(this, cause);
              return Unit_instance;
            }
          } else {
            if (this._state_1.atomicfu$compareAndSet(state, new CompletedContinuation(state, VOID, VOID, VOID, cause))) {
              return Unit_instance;
            }
          }
        }
      }
    }
    return tmp$ret$0;
  };
  protoOf(CancellableContinuationImpl).cancel_e74who_k$ = function (cause) {
    // Inline function 'kotlinx.atomicfu.loop' call
    var this_0 = this._state_1;
    while (true) {
      var tmp0 = this_0.kotlinx$atomicfu$value;
      $l$block: {
        if (!(!(tmp0 == null) ? isInterface(tmp0, NotCompleted) : false))
          return false;
        var tmp;
        if (isInterface(tmp0, CancelHandler)) {
          tmp = true;
        } else {
          tmp = tmp0 instanceof Segment;
        }
        var update = new CancelledContinuation(this, cause, tmp);
        if (!this._state_1.atomicfu$compareAndSet(tmp0, update)) {
          break $l$block;
        }
        if (isInterface(tmp0, CancelHandler)) {
          this.callCancelHandler_e6l0np_k$(tmp0, cause);
        } else {
          if (tmp0 instanceof Segment) {
            callSegmentOnCancellation(this, tmp0, cause);
          }
        }
        detachChildIfNonReusable(this);
        dispatchResume(this, this.resumeMode_1);
        return true;
      }
    }
  };
  protoOf(CancellableContinuationImpl).parentCancelled_jw71o9_k$ = function (cause) {
    if (cancelLater(this, cause))
      return Unit_instance;
    this.cancel_e74who_k$(cause);
    detachChildIfNonReusable(this);
  };
  protoOf(CancellableContinuationImpl).callCancelHandler_e6l0np_k$ = function (handler, cause) {
    // Inline function 'kotlinx.coroutines.CancellableContinuationImpl.callCancelHandlerSafely' call
    try {
      handler.invoke_py2q9a_k$(cause);
    } catch ($p) {
      if ($p instanceof Error) {
        var ex = $p;
        handleCoroutineException(this.get_context_h02k06_k$(), new CompletionHandlerException('Exception in invokeOnCancellation handler for ' + this.toString(), ex));
      } else {
        throw $p;
      }
    }
    return Unit_instance;
  };
  protoOf(CancellableContinuationImpl).callOnCancellation_n8igin_k$ = function (onCancellation, cause, value) {
    try {
      onCancellation(cause, value, this.get_context_h02k06_k$());
    } catch ($p) {
      if ($p instanceof Error) {
        var ex = $p;
        handleCoroutineException(this.get_context_h02k06_k$(), new CompletionHandlerException('Exception in resume onCancellation handler for ' + this.toString(), ex));
      } else {
        throw $p;
      }
    }
  };
  protoOf(CancellableContinuationImpl).getContinuationCancellationCause_3nurbc_k$ = function (parent) {
    return parent.getCancellationException_8i1q6u_k$();
  };
  protoOf(CancellableContinuationImpl).getResult_fck196_k$ = function () {
    var isReusable_0 = isReusable(this);
    if (trySuspend(this)) {
      if (_get_parentHandle__f8dcex(this) == null) {
        installParentHandle(this);
      }
      if (isReusable_0) {
        this.releaseClaimedReusableContinuation_mdg0s9_k$();
      }
      return get_COROUTINE_SUSPENDED();
    }
    if (isReusable_0) {
      this.releaseClaimedReusableContinuation_mdg0s9_k$();
    }
    var state = this.get_state_2t6sbp_k$();
    if (state instanceof CompletedExceptionally)
      throw recoverStackTrace(state.cause_1, this);
    if (get_isCancellableMode(this.resumeMode_1)) {
      var job = this.get_context_h02k06_k$().get_y2st91_k$(Key_instance_2);
      if (!(job == null) && !job.get_isActive_quafmh_k$()) {
        var cause = job.getCancellationException_8i1q6u_k$();
        this.cancelCompletedResult_pnx7en_k$(state, cause);
        throw recoverStackTrace(cause, this);
      }
    }
    return this.getSuccessfulResult_4uqe9r_k$(state);
  };
  protoOf(CancellableContinuationImpl).releaseClaimedReusableContinuation_mdg0s9_k$ = function () {
    var tmp = this.delegate_1;
    var tmp0_safe_receiver = tmp instanceof DispatchedContinuation ? tmp : null;
    var tmp1_elvis_lhs = tmp0_safe_receiver == null ? null : tmp0_safe_receiver.tryReleaseClaimedContinuation_ko810q_k$(this);
    var tmp_0;
    if (tmp1_elvis_lhs == null) {
      return Unit_instance;
    } else {
      tmp_0 = tmp1_elvis_lhs;
    }
    var cancellationCause = tmp_0;
    this.detachChild_85lap8_k$();
    this.cancel_e74who_k$(cancellationCause);
  };
  protoOf(CancellableContinuationImpl).resumeWith_dtxwbr_k$ = function (result) {
    return this.resumeImpl$default_g4h6ml_k$(toState(result, this), this.resumeMode_1);
  };
  protoOf(CancellableContinuationImpl).invokeOnCancellation_kffkqp_k$ = function (handler) {
    return invokeOnCancellation(this, new UserSupplied(handler));
  };
  protoOf(CancellableContinuationImpl).invokeOnCancellationInternal_vx7l43_k$ = function (handler) {
    return invokeOnCancellationImpl(this, handler);
  };
  protoOf(CancellableContinuationImpl).resumeImpl_fj866n_k$ = function (proposedUpdate, resumeMode, onCancellation) {
    // Inline function 'kotlinx.atomicfu.loop' call
    var this_0 = this._state_1;
    while (true) {
      var tmp0 = this_0.kotlinx$atomicfu$value;
      $l$block: {
        if (!(tmp0 == null) ? isInterface(tmp0, NotCompleted) : false) {
          var update = resumedState(this, tmp0, proposedUpdate, resumeMode, onCancellation, null);
          if (!this._state_1.atomicfu$compareAndSet(tmp0, update)) {
            break $l$block;
          }
          detachChildIfNonReusable(this);
          dispatchResume(this, resumeMode);
          return Unit_instance;
        } else {
          if (tmp0 instanceof CancelledContinuation) {
            if (tmp0.makeResumed_vjvawn_k$()) {
              if (onCancellation == null)
                null;
              else {
                // Inline function 'kotlin.let' call
                this.callOnCancellation_n8igin_k$(onCancellation, tmp0.cause_1, proposedUpdate);
              }
              return Unit_instance;
            }
          }
        }
        alreadyResumedError(this, proposedUpdate);
      }
    }
  };
  protoOf(CancellableContinuationImpl).resumeImpl$default_g4h6ml_k$ = function (proposedUpdate, resumeMode, onCancellation, $super) {
    onCancellation = onCancellation === VOID ? null : onCancellation;
    var tmp;
    if ($super === VOID) {
      this.resumeImpl_fj866n_k$(proposedUpdate, resumeMode, onCancellation);
      tmp = Unit_instance;
    } else {
      tmp = $super.resumeImpl_fj866n_k$.call(this, proposedUpdate, resumeMode, onCancellation);
    }
    return tmp;
  };
  protoOf(CancellableContinuationImpl).detachChild_85lap8_k$ = function () {
    var tmp0_elvis_lhs = _get_parentHandle__f8dcex(this);
    var tmp;
    if (tmp0_elvis_lhs == null) {
      return Unit_instance;
    } else {
      tmp = tmp0_elvis_lhs;
    }
    var handle = tmp;
    handle.dispose_3nnxhr_k$();
    this._parentHandle_1.kotlinx$atomicfu$value = NonDisposableHandle_instance;
  };
  protoOf(CancellableContinuationImpl).getSuccessfulResult_4uqe9r_k$ = function (state) {
    var tmp;
    if (state instanceof CompletedContinuation) {
      tmp = state.result_1;
    } else {
      tmp = state;
    }
    return tmp;
  };
  protoOf(CancellableContinuationImpl).getExceptionalResult_i3cs19_k$ = function (state) {
    var tmp0_safe_receiver = protoOf(DispatchedTask).getExceptionalResult_i3cs19_k$.call(this, state);
    var tmp;
    if (tmp0_safe_receiver == null) {
      tmp = null;
    } else {
      // Inline function 'kotlin.let' call
      tmp = recoverStackTrace(tmp0_safe_receiver, this.delegate_1);
    }
    return tmp;
  };
  protoOf(CancellableContinuationImpl).toString = function () {
    return this.nameString_cd9e9w_k$() + '(' + toDebugString(this.delegate_1) + '){' + _get_stateDebugRepresentation__bf18u4(this) + '}@' + get_hexAddress(this);
  };
  protoOf(CancellableContinuationImpl).nameString_cd9e9w_k$ = function () {
    return 'CancellableContinuation';
  };
  function NotCompleted() {
  }
  function UserSupplied(handler) {
    this.handler_1 = handler;
  }
  protoOf(UserSupplied).invoke_py2q9a_k$ = function (cause) {
    this.handler_1(cause);
  };
  protoOf(UserSupplied).toString = function () {
    return 'CancelHandler.UserSupplied[' + get_classSimpleName(this.handler_1) + '@' + get_hexAddress(this) + ']';
  };
  function CancelHandler() {
  }
  function Active() {
  }
  protoOf(Active).toString = function () {
    return 'Active';
  };
  var Active_instance;
  function Active_getInstance() {
    return Active_instance;
  }
  function CompletedContinuation(result, cancelHandler, onCancellation, idempotentResume, cancelCause) {
    cancelHandler = cancelHandler === VOID ? null : cancelHandler;
    onCancellation = onCancellation === VOID ? null : onCancellation;
    idempotentResume = idempotentResume === VOID ? null : idempotentResume;
    cancelCause = cancelCause === VOID ? null : cancelCause;
    this.result_1 = result;
    this.cancelHandler_1 = cancelHandler;
    this.onCancellation_1 = onCancellation;
    this.idempotentResume_1 = idempotentResume;
    this.cancelCause_1 = cancelCause;
  }
  protoOf(CompletedContinuation).get_cancelled_ge9r54_k$ = function () {
    return !(this.cancelCause_1 == null);
  };
  protoOf(CompletedContinuation).invokeHandlers_gry311_k$ = function (cont, cause) {
    var tmp0_safe_receiver = this.cancelHandler_1;
    if (tmp0_safe_receiver == null)
      null;
    else {
      // Inline function 'kotlin.let' call
      cont.callCancelHandler_e6l0np_k$(tmp0_safe_receiver, cause);
    }
    var tmp1_safe_receiver = this.onCancellation_1;
    if (tmp1_safe_receiver == null)
      null;
    else {
      // Inline function 'kotlin.let' call
      cont.callOnCancellation_n8igin_k$(tmp1_safe_receiver, cause, this.result_1);
    }
  };
  protoOf(CompletedContinuation).copy_umt5fl_k$ = function (result, cancelHandler, onCancellation, idempotentResume, cancelCause) {
    return new CompletedContinuation(result, cancelHandler, onCancellation, idempotentResume, cancelCause);
  };
  protoOf(CompletedContinuation).copy$default_uedmwo_k$ = function (result, cancelHandler, onCancellation, idempotentResume, cancelCause, $super) {
    result = result === VOID ? this.result_1 : result;
    cancelHandler = cancelHandler === VOID ? this.cancelHandler_1 : cancelHandler;
    onCancellation = onCancellation === VOID ? this.onCancellation_1 : onCancellation;
    idempotentResume = idempotentResume === VOID ? this.idempotentResume_1 : idempotentResume;
    cancelCause = cancelCause === VOID ? this.cancelCause_1 : cancelCause;
    return $super === VOID ? this.copy_umt5fl_k$(result, cancelHandler, onCancellation, idempotentResume, cancelCause) : $super.copy_umt5fl_k$.call(this, result, cancelHandler, onCancellation, idempotentResume, cancelCause);
  };
  protoOf(CompletedContinuation).toString = function () {
    return 'CompletedContinuation(result=' + toString_0(this.result_1) + ', cancelHandler=' + toString_0(this.cancelHandler_1) + ', onCancellation=' + toString_0(this.onCancellation_1) + ', idempotentResume=' + toString_0(this.idempotentResume_1) + ', cancelCause=' + toString_0(this.cancelCause_1) + ')';
  };
  protoOf(CompletedContinuation).hashCode = function () {
    var result = this.result_1 == null ? 0 : hashCode(this.result_1);
    result = imul(result, 31) + (this.cancelHandler_1 == null ? 0 : hashCode(this.cancelHandler_1)) | 0;
    result = imul(result, 31) + (this.onCancellation_1 == null ? 0 : hashCode(this.onCancellation_1)) | 0;
    result = imul(result, 31) + (this.idempotentResume_1 == null ? 0 : hashCode(this.idempotentResume_1)) | 0;
    result = imul(result, 31) + (this.cancelCause_1 == null ? 0 : hashCode(this.cancelCause_1)) | 0;
    return result;
  };
  protoOf(CompletedContinuation).equals = function (other) {
    if (this === other)
      return true;
    if (!(other instanceof CompletedContinuation))
      return false;
    if (!equals(this.result_1, other.result_1))
      return false;
    if (!equals(this.cancelHandler_1, other.cancelHandler_1))
      return false;
    if (!equals(this.onCancellation_1, other.onCancellation_1))
      return false;
    if (!equals(this.idempotentResume_1, other.idempotentResume_1))
      return false;
    if (!equals(this.cancelCause_1, other.cancelCause_1))
      return false;
    return true;
  };
  function ChildContinuation(child) {
    JobNode.call(this);
    this.child_1 = child;
  }
  protoOf(ChildContinuation).get_onCancelling_k07uns_k$ = function () {
    return true;
  };
  protoOf(ChildContinuation).invoke_py2q9a_k$ = function (cause) {
    this.child_1.parentCancelled_jw71o9_k$(this.child_1.getContinuationCancellationCause_3nurbc_k$(this.get_job_18j2r0_k$()));
  };
  function CompletedExceptionally(cause, handled) {
    handled = handled === VOID ? false : handled;
    this.cause_1 = cause;
    this._handled_1 = atomic$boolean$1(handled);
  }
  protoOf(CompletedExceptionally).makeHandled_ws9oq6_k$ = function () {
    return this._handled_1.atomicfu$compareAndSet(false, true);
  };
  protoOf(CompletedExceptionally).toString = function () {
    return get_classSimpleName(this) + '[' + this.cause_1.toString() + ']';
  };
  function CancelledContinuation(continuation, cause, handled) {
    CompletedExceptionally.call(this, cause == null ? CancellationException_init_$Create$('Continuation ' + toString(continuation) + ' was cancelled normally') : cause, handled);
    this._resumed_1 = atomic$boolean$1(false);
  }
  protoOf(CancelledContinuation).makeResumed_vjvawn_k$ = function () {
    return this._resumed_1.atomicfu$compareAndSet(false, true);
  };
  function toState(_this__u8e3s4, caller) {
    // Inline function 'kotlin.getOrElse' call
    var exception = Result__exceptionOrNull_impl_p6xea9(_this__u8e3s4);
    var tmp;
    if (exception == null) {
      tmp = _Result___get_value__impl__bjfvqg(_this__u8e3s4);
    } else {
      tmp = new CompletedExceptionally(recoverStackTrace(exception, caller));
    }
    return tmp;
  }
  function toState_0(_this__u8e3s4) {
    // Inline function 'kotlin.getOrElse' call
    var exception = Result__exceptionOrNull_impl_p6xea9(_this__u8e3s4);
    var tmp;
    if (exception == null) {
      tmp = _Result___get_value__impl__bjfvqg(_this__u8e3s4);
    } else {
      tmp = new CompletedExceptionally(exception);
    }
    return tmp;
  }
  function CoroutineDispatcher$Key$_init_$lambda_akl8b5(it) {
    return it instanceof CoroutineDispatcher ? it : null;
  }
  function Key() {
    Key_instance_0 = this;
    var tmp = Key_instance;
    AbstractCoroutineContextKey.call(this, tmp, CoroutineDispatcher$Key$_init_$lambda_akl8b5);
  }
  var Key_instance_0;
  function Key_getInstance() {
    if (Key_instance_0 == null)
      new Key();
    return Key_instance_0;
  }
  function CoroutineDispatcher() {
    Key_getInstance();
    AbstractCoroutineContextElement.call(this, Key_instance);
  }
  protoOf(CoroutineDispatcher).isDispatchNeeded_ft82v4_k$ = function (context) {
    return true;
  };
  protoOf(CoroutineDispatcher).interceptContinuation_3dnmlu_k$ = function (continuation) {
    return new DispatchedContinuation(this, continuation);
  };
  protoOf(CoroutineDispatcher).releaseInterceptedContinuation_rgafzi_k$ = function (continuation) {
    var dispatched = continuation instanceof DispatchedContinuation ? continuation : THROW_CCE();
    dispatched.release_8sql92_k$();
  };
  protoOf(CoroutineDispatcher).toString = function () {
    return get_classSimpleName(this) + '@' + get_hexAddress(this);
  };
  function handleCoroutineException(context, exception) {
    var tmp;
    if (exception instanceof DispatchException) {
      tmp = exception.cause_1;
    } else {
      tmp = exception;
    }
    var reportException = tmp;
    try {
      var tmp0_safe_receiver = context.get_y2st91_k$(Key_instance_1);
      if (tmp0_safe_receiver == null)
        null;
      else {
        // Inline function 'kotlin.let' call
        tmp0_safe_receiver.handleException_e679jj_k$(context, reportException);
        return Unit_instance;
      }
    } catch ($p) {
      if ($p instanceof Error) {
        var t = $p;
        handleUncaughtCoroutineException(context, handlerException(reportException, t));
        return Unit_instance;
      } else {
        throw $p;
      }
    }
    handleUncaughtCoroutineException(context, reportException);
  }
  function Key_0() {
  }
  var Key_instance_1;
  function Key_getInstance_0() {
    return Key_instance_1;
  }
  function handlerException(originalException, thrownException) {
    if (originalException === thrownException)
      return originalException;
    // Inline function 'kotlin.apply' call
    var this_0 = RuntimeException_init_$Create$('Exception while trying to handle coroutine exception', thrownException);
    addSuppressed(this_0, originalException);
    return this_0;
  }
  function delta($this, unconfined) {
    return unconfined ? new Long(0, 1) : new Long(1, 0);
  }
  function EventLoop() {
    CoroutineDispatcher.call(this);
    this.useCount_1 = new Long(0, 0);
    this.shared_1 = false;
    this.unconfinedQueue_1 = null;
  }
  protoOf(EventLoop).processUnconfinedEvent_mypjl6_k$ = function () {
    var tmp0_elvis_lhs = this.unconfinedQueue_1;
    var tmp;
    if (tmp0_elvis_lhs == null) {
      return false;
    } else {
      tmp = tmp0_elvis_lhs;
    }
    var queue = tmp;
    var tmp1_elvis_lhs = queue.removeFirstOrNull_eges3a_k$();
    var tmp_0;
    if (tmp1_elvis_lhs == null) {
      return false;
    } else {
      tmp_0 = tmp1_elvis_lhs;
    }
    var task = tmp_0;
    task.run_mvkpxh_k$();
    return true;
  };
  protoOf(EventLoop).dispatchUnconfined_o79kaq_k$ = function (task) {
    var tmp0_elvis_lhs = this.unconfinedQueue_1;
    var tmp;
    if (tmp0_elvis_lhs == null) {
      // Inline function 'kotlin.also' call
      var this_0 = ArrayDeque_init_$Create$();
      this.unconfinedQueue_1 = this_0;
      tmp = this_0;
    } else {
      tmp = tmp0_elvis_lhs;
    }
    var queue = tmp;
    queue.addLast_gaaijb_k$(task);
  };
  protoOf(EventLoop).get_isUnconfinedLoopActive_g78ri6_k$ = function () {
    return compare(this.useCount_1, delta(this, true)) >= 0;
  };
  protoOf(EventLoop).get_isUnconfinedQueueEmpty_mi405s_k$ = function () {
    var tmp0_safe_receiver = this.unconfinedQueue_1;
    var tmp1_elvis_lhs = tmp0_safe_receiver == null ? null : tmp0_safe_receiver.isEmpty_y1axqb_k$();
    return tmp1_elvis_lhs == null ? true : tmp1_elvis_lhs;
  };
  protoOf(EventLoop).incrementUseCount_jadqvy_k$ = function (unconfined) {
    this.useCount_1 = add(this.useCount_1, delta(this, unconfined));
    if (!unconfined)
      this.shared_1 = true;
  };
  protoOf(EventLoop).decrementUseCount_x8i8ca_k$ = function (unconfined) {
    this.useCount_1 = subtract(this.useCount_1, delta(this, unconfined));
    if (compare(this.useCount_1, new Long(0, 0)) > 0)
      return Unit_instance;
    // Inline function 'kotlinx.coroutines.assert' call
    if (this.shared_1) {
      this.shutdown_cplwmy_k$();
    }
  };
  protoOf(EventLoop).shutdown_cplwmy_k$ = function () {
  };
  function ThreadLocalEventLoop() {
    ThreadLocalEventLoop_instance = this;
    this.ref_1 = commonThreadLocal(new Symbol('ThreadLocalEventLoop'));
  }
  protoOf(ThreadLocalEventLoop).get_eventLoop_wo5hfs_k$ = function () {
    var tmp0_elvis_lhs = this.ref_1.get_26vq_k$();
    var tmp;
    if (tmp0_elvis_lhs == null) {
      // Inline function 'kotlin.also' call
      var this_0 = createEventLoop();
      ThreadLocalEventLoop_getInstance().ref_1.set_tg4fwj_k$(this_0);
      tmp = this_0;
    } else {
      tmp = tmp0_elvis_lhs;
    }
    return tmp;
  };
  var ThreadLocalEventLoop_instance;
  function ThreadLocalEventLoop_getInstance() {
    if (ThreadLocalEventLoop_instance == null)
      new ThreadLocalEventLoop();
    return ThreadLocalEventLoop_instance;
  }
  function CompletionHandlerException(message, cause) {
    RuntimeException_init_$Init$(message, cause, this);
    captureStack(this, CompletionHandlerException);
  }
  function CoroutinesInternalError(message, cause) {
    Error_init_$Init$(message, cause, this);
    captureStack(this, CoroutinesInternalError);
  }
  function Key_1() {
  }
  var Key_instance_2;
  function Key_getInstance_1() {
    return Key_instance_2;
  }
  function NonDisposableHandle() {
  }
  protoOf(NonDisposableHandle).dispose_3nnxhr_k$ = function () {
  };
  protoOf(NonDisposableHandle).toString = function () {
    return 'NonDisposableHandle';
  };
  var NonDisposableHandle_instance;
  function NonDisposableHandle_getInstance() {
    return NonDisposableHandle_instance;
  }
  function invokeOnCompletion(_this__u8e3s4, invokeImmediately, handler) {
    invokeImmediately = invokeImmediately === VOID ? true : invokeImmediately;
    var tmp;
    if (_this__u8e3s4 instanceof JobSupport) {
      tmp = _this__u8e3s4.invokeOnCompletionInternal_5jxuhy_k$(invokeImmediately, handler);
    } else {
      var tmp_0 = handler.get_onCancelling_k07uns_k$();
      tmp = _this__u8e3s4.invokeOnCompletion_sct3wq_k$(tmp_0, invokeImmediately, JobNode$invoke$ref(handler));
    }
    return tmp;
  }
  var COMPLETING_ALREADY;
  var COMPLETING_WAITING_CHILDREN;
  var COMPLETING_RETRY;
  var TOO_LATE_TO_CANCEL;
  var SEALED;
  var EMPTY_NEW;
  function get_EMPTY_ACTIVE() {
    _init_properties_JobSupport_kt__68f172();
    return EMPTY_ACTIVE;
  }
  var EMPTY_ACTIVE;
  function Empty(isActive) {
    this.isActive_1 = isActive;
  }
  protoOf(Empty).get_list_wopuqv_k$ = function () {
    return null;
  };
  protoOf(Empty).toString = function () {
    return 'Empty{' + (this.isActive_1 ? 'Active' : 'New') + '}';
  };
  function Incomplete() {
  }
  function NodeList() {
    LockFreeLinkedListHead.call(this);
  }
  protoOf(NodeList).get_list_wopuqv_k$ = function () {
    return this;
  };
  protoOf(NodeList).getString_gb1pt9_k$ = function (state) {
    // Inline function 'kotlin.text.buildString' call
    // Inline function 'kotlin.apply' call
    var this_0 = StringBuilder_init_$Create$();
    this_0.append_22ad7x_k$('List{');
    this_0.append_22ad7x_k$(state);
    this_0.append_22ad7x_k$('}[');
    var first = true;
    // Inline function 'kotlinx.coroutines.internal.LockFreeLinkedListHead.forEach' call
    var cur = this._next_1;
    while (!equals(cur, this)) {
      var node = cur;
      if (node instanceof JobNode) {
        if (first) {
          first = false;
        } else
          this_0.append_22ad7x_k$(', ');
        this_0.append_t8pm91_k$(node);
      }
      cur = cur._next_1;
    }
    this_0.append_22ad7x_k$(']');
    return this_0.toString();
  };
  protoOf(NodeList).toString = function () {
    return get_DEBUG() ? this.getString_gb1pt9_k$('Active') : protoOf(LockFreeLinkedListHead).toString.call(this);
  };
  function JobNode() {
    LockFreeLinkedListNode.call(this);
  }
  protoOf(JobNode).get_job_18j2r0_k$ = function () {
    var tmp = this.job_1;
    if (!(tmp == null))
      return tmp;
    else {
      throwUninitializedPropertyAccessException('job');
    }
  };
  protoOf(JobNode).get_list_wopuqv_k$ = function () {
    return null;
  };
  protoOf(JobNode).dispose_3nnxhr_k$ = function () {
    return this.get_job_18j2r0_k$().removeNode_qrwaz_k$(this);
  };
  protoOf(JobNode).toString = function () {
    return get_classSimpleName(this) + '@' + get_hexAddress(this) + '[job@' + get_hexAddress(this.get_job_18j2r0_k$()) + ']';
  };
  function promoteEmptyToNodeList($this, state) {
    var list = new NodeList();
    var update = state.isActive_1 ? list : new InactiveNodeList(list);
    $this._state_1.atomicfu$compareAndSet(state, update);
  }
  function promoteSingleToNodeList($this, state) {
    state.addOneIfEmpty_2jwoix_k$(new NodeList());
    // Inline function 'kotlinx.coroutines.internal.LockFreeLinkedListNode.nextNode' call
    var list = state._next_1;
    $this._state_1.atomicfu$compareAndSet(state, list);
  }
  function Finishing() {
  }
  protoOf(Finishing).get_rootCause_69dwxu_k$ = function () {
    return this._rootCause_1.kotlinx$atomicfu$value;
  };
  function JobSupport() {
  }
  protoOf(JobSupport).get_state_2t6sbp_k$ = function () {
    return this._state_1.kotlinx$atomicfu$value;
  };
  protoOf(JobSupport).invokeOnCompletionInternal_5jxuhy_k$ = function (invokeImmediately, node) {
    node.job_1 = this;
    var tmp$ret$0;
    $l$block_1: {
      // Inline function 'kotlinx.coroutines.JobSupport.tryPutNodeIntoList' call
      // Inline function 'kotlinx.coroutines.JobSupport.loopOnState' call
      while (true) {
        var state = this.get_state_2t6sbp_k$();
        if (state instanceof Empty) {
          if (state.isActive_1) {
            if (this._state_1.atomicfu$compareAndSet(state, node)) {
              tmp$ret$0 = true;
              break $l$block_1;
            }
          } else {
            promoteEmptyToNodeList(this, state);
          }
        } else {
          if (!(state == null) ? isInterface(state, Incomplete) : false) {
            var list = state.get_list_wopuqv_k$();
            if (list == null) {
              promoteSingleToNodeList(this, state instanceof JobNode ? state : THROW_CCE());
            } else {
              var tmp;
              if (node.get_onCancelling_k07uns_k$()) {
                var tmp0_safe_receiver = state instanceof Finishing ? state : null;
                var rootCause = tmp0_safe_receiver == null ? null : tmp0_safe_receiver.get_rootCause_69dwxu_k$();
                var tmp_0;
                if (rootCause == null) {
                  tmp_0 = list.addLast_5b0i77_k$(node, 5);
                } else {
                  if (invokeImmediately) {
                    node.invoke_py2q9a_k$(rootCause);
                  }
                  return NonDisposableHandle_instance;
                }
                tmp = tmp_0;
              } else {
                tmp = list.addLast_5b0i77_k$(node, 1);
              }
              if (tmp) {
                tmp$ret$0 = true;
                break $l$block_1;
              }
            }
          } else {
            tmp$ret$0 = false;
            break $l$block_1;
          }
        }
      }
    }
    var added = tmp$ret$0;
    if (added)
      return node;
    else if (invokeImmediately) {
      var tmp_1 = this.get_state_2t6sbp_k$();
      var tmp0_safe_receiver_0 = tmp_1 instanceof CompletedExceptionally ? tmp_1 : null;
      node.invoke_py2q9a_k$(tmp0_safe_receiver_0 == null ? null : tmp0_safe_receiver_0.cause_1);
    }
    return NonDisposableHandle_instance;
  };
  protoOf(JobSupport).removeNode_qrwaz_k$ = function (node) {
    // Inline function 'kotlinx.coroutines.JobSupport.loopOnState' call
    while (true) {
      var state = this.get_state_2t6sbp_k$();
      if (state instanceof JobNode) {
        if (!(state === node))
          return Unit_instance;
        if (this._state_1.atomicfu$compareAndSet(state, get_EMPTY_ACTIVE()))
          return Unit_instance;
      } else {
        if (!(state == null) ? isInterface(state, Incomplete) : false) {
          if (!(state.get_list_wopuqv_k$() == null)) {
            node.remove_fgfybg_k$();
          }
          return Unit_instance;
        } else {
          return Unit_instance;
        }
      }
    }
  };
  function InactiveNodeList(list) {
    this.list_1 = list;
  }
  protoOf(InactiveNodeList).get_list_wopuqv_k$ = function () {
    return this.list_1;
  };
  protoOf(InactiveNodeList).toString = function () {
    return get_DEBUG() ? this.list_1.getString_gb1pt9_k$('New') : anyToString(this);
  };
  var properties_initialized_JobSupport_kt_5iq8a4;
  function _init_properties_JobSupport_kt__68f172() {
    if (!properties_initialized_JobSupport_kt_5iq8a4) {
      properties_initialized_JobSupport_kt_5iq8a4 = true;
      COMPLETING_ALREADY = new Symbol('COMPLETING_ALREADY');
      COMPLETING_WAITING_CHILDREN = new Symbol('COMPLETING_WAITING_CHILDREN');
      COMPLETING_RETRY = new Symbol('COMPLETING_RETRY');
      TOO_LATE_TO_CANCEL = new Symbol('TOO_LATE_TO_CANCEL');
      SEALED = new Symbol('SEALED');
      EMPTY_NEW = new Empty(false);
      EMPTY_ACTIVE = new Empty(true);
    }
  }
  function JobNode$invoke$ref(p0) {
    return constructCallableReference(function (p0_0) {
      p0.invoke_py2q9a_k$(p0_0);
      return Unit_instance;
    }, 1, 0, 0, 'invoke', [p0]);
  }
  function get_NONE() {
    _init_properties_StateFlow_kt__eu9yi5();
    return NONE;
  }
  var NONE;
  function get_PENDING() {
    _init_properties_StateFlow_kt__eu9yi5();
    return PENDING;
  }
  var PENDING;
  function MutableStateFlow(value) {
    _init_properties_StateFlow_kt__eu9yi5();
    return new StateFlowImpl(value == null ? get_NULL() : value);
  }
  function updateState($this, expectedState, newState) {
    var curSequence;
    var curSlots;
    // Inline function 'kotlinx.coroutines.internal.synchronized' call
    // Inline function 'kotlinx.coroutines.internal.synchronizedImpl' call
    var oldState = $this._state_1.kotlinx$atomicfu$value;
    if (!(expectedState == null) && !equals(oldState, expectedState))
      return false;
    if (equals(oldState, newState))
      return true;
    $this._state_1.kotlinx$atomicfu$value = newState;
    curSequence = $this.sequence_1;
    if ((curSequence & 1) === 0) {
      curSequence = curSequence + 1 | 0;
      $this.sequence_1 = curSequence;
    } else {
      $this.sequence_1 = curSequence + 2 | 0;
      return true;
    }
    curSlots = $this.slots_1;
    while (true) {
      var tmp0_safe_receiver = curSlots;
      if (tmp0_safe_receiver == null)
        null;
      else {
        // Inline function 'kotlin.collections.forEach' call
        var inductionVariable = 0;
        var last = tmp0_safe_receiver.length;
        while (inductionVariable < last) {
          var element = tmp0_safe_receiver[inductionVariable];
          inductionVariable = inductionVariable + 1 | 0;
          if (element == null)
            null;
          else {
            element.makePending_e7hvrb_k$();
          }
        }
      }
      // Inline function 'kotlinx.coroutines.internal.synchronized' call
      // Inline function 'kotlinx.coroutines.internal.synchronizedImpl' call
      if ($this.sequence_1 === curSequence) {
        $this.sequence_1 = curSequence + 1 | 0;
        return true;
      }
      curSequence = $this.sequence_1;
      curSlots = $this.slots_1;
    }
  }
  function StateFlowImpl(initialState) {
    AbstractSharedFlow.call(this);
    this._state_1 = atomic$ref$1(initialState);
    this.sequence_1 = 0;
  }
  protoOf(StateFlowImpl).set_value_v1vabv_k$ = function (value) {
    updateState(this, null, value == null ? get_NULL() : value);
  };
  protoOf(StateFlowImpl).get_value_j01efc_k$ = function () {
    var tmp0 = get_NULL();
    // Inline function 'kotlinx.coroutines.internal.Symbol.unbox' call
    var value = this._state_1.kotlinx$atomicfu$value;
    return value === tmp0 ? null : value;
  };
  var properties_initialized_StateFlow_kt_nsqikx;
  function _init_properties_StateFlow_kt__eu9yi5() {
    if (!properties_initialized_StateFlow_kt_nsqikx) {
      properties_initialized_StateFlow_kt_nsqikx = true;
      NONE = new Symbol('NONE');
      PENDING = new Symbol('PENDING');
    }
  }
  function AbstractSharedFlow() {
    SynchronizedObject.call(this);
    this.slots_1 = null;
    this.nCollectors_1 = 0;
    this.nextIndex_1 = 0;
    this._subscriptionCount_1 = null;
  }
  function get_NULL() {
    _init_properties_NullSurrogate_kt__n2yti9();
    return NULL;
  }
  var NULL;
  var UNINITIALIZED;
  var DONE;
  var properties_initialized_NullSurrogate_kt_39v8bl;
  function _init_properties_NullSurrogate_kt__n2yti9() {
    if (!properties_initialized_NullSurrogate_kt_39v8bl) {
      properties_initialized_NullSurrogate_kt_39v8bl = true;
      NULL = new Symbol('NULL');
      UNINITIALIZED = new Symbol('UNINITIALIZED');
      DONE = new Symbol('DONE');
    }
  }
  function get_value(_this__u8e3s4) {
    return _this__u8e3s4.get_26vq_k$();
  }
  function Segment() {
  }
  function ConcurrentLinkedListNode() {
  }
  function handleUncaughtCoroutineException(context, exception) {
    var _iterator__ex2g4s = get_platformExceptionHandlers().iterator_jk1svi_k$();
    while (_iterator__ex2g4s.hasNext_bitz1p_k$()) {
      var handler = _iterator__ex2g4s.next_20eer_k$();
      try {
        handler.handleException_e679jj_k$(context, exception);
      } catch ($p) {
        if ($p instanceof ExceptionSuccessfullyProcessed) {
          var _unused_var__etf5q3 = $p;
          return Unit_instance;
        } else {
          if ($p instanceof Error) {
            var t = $p;
            propagateExceptionFinalResort(handlerException(exception, t));
          } else {
            throw $p;
          }
        }
      }
    }
    try {
      addSuppressed(exception, new DiagnosticCoroutineContextException(context));
    } catch ($p_0) {
      if ($p_0 instanceof Error) {
        var e = $p_0;
      } else {
        throw $p_0;
      }
    }
    propagateExceptionFinalResort(exception);
  }
  function ExceptionSuccessfullyProcessed() {
  }
  function get_UNDEFINED() {
    _init_properties_DispatchedContinuation_kt__tnmqc0();
    return UNDEFINED;
  }
  var UNDEFINED;
  function get_REUSABLE_CLAIMED() {
    _init_properties_DispatchedContinuation_kt__tnmqc0();
    return REUSABLE_CLAIMED;
  }
  var REUSABLE_CLAIMED;
  function _get_reusableCancellableContinuation__9qex09($this) {
    var tmp = $this._reusableCancellableContinuation_1.kotlinx$atomicfu$value;
    return tmp instanceof CancellableContinuationImpl ? tmp : null;
  }
  function DispatchedContinuation(dispatcher, continuation) {
    DispatchedTask.call(this, -1);
    this.dispatcher_1 = dispatcher;
    this.continuation_1 = continuation;
    this._state_1 = get_UNDEFINED();
    this.countOrElement_1 = threadContextElements(this.get_context_h02k06_k$());
    this._reusableCancellableContinuation_1 = atomic$ref$1(null);
  }
  protoOf(DispatchedContinuation).isReusable_asltyw_k$ = function () {
    return !(this._reusableCancellableContinuation_1.kotlinx$atomicfu$value == null);
  };
  protoOf(DispatchedContinuation).awaitReusability_6g41eu_k$ = function () {
    // Inline function 'kotlinx.atomicfu.loop' call
    var this_0 = this._reusableCancellableContinuation_1;
    while (true) {
      if (!(this_0.kotlinx$atomicfu$value === get_REUSABLE_CLAIMED()))
        return Unit_instance;
    }
  };
  protoOf(DispatchedContinuation).release_8sql92_k$ = function () {
    this.awaitReusability_6g41eu_k$();
    var tmp0_safe_receiver = _get_reusableCancellableContinuation__9qex09(this);
    if (tmp0_safe_receiver == null)
      null;
    else {
      tmp0_safe_receiver.detachChild_85lap8_k$();
    }
  };
  protoOf(DispatchedContinuation).tryReleaseClaimedContinuation_ko810q_k$ = function (continuation) {
    // Inline function 'kotlinx.atomicfu.loop' call
    var this_0 = this._reusableCancellableContinuation_1;
    while (true) {
      var state = this_0.kotlinx$atomicfu$value;
      if (state === get_REUSABLE_CLAIMED()) {
        if (this._reusableCancellableContinuation_1.atomicfu$compareAndSet(get_REUSABLE_CLAIMED(), continuation))
          return null;
      } else {
        if (state instanceof Error) {
          // Inline function 'kotlin.require' call
          // Inline function 'kotlin.require' call
          if (!this._reusableCancellableContinuation_1.atomicfu$compareAndSet(state, null)) {
            var message = 'Failed requirement.';
            throw IllegalArgumentException_init_$Create$(toString(message));
          }
          return state;
        } else {
          // Inline function 'kotlin.error' call
          var message_0 = 'Inconsistent state ' + toString_0(state);
          throw IllegalStateException_init_$Create$(toString(message_0));
        }
      }
    }
  };
  protoOf(DispatchedContinuation).postponeCancellation_hjv3hh_k$ = function (cause) {
    // Inline function 'kotlinx.atomicfu.loop' call
    var this_0 = this._reusableCancellableContinuation_1;
    while (true) {
      var state = this_0.kotlinx$atomicfu$value;
      if (equals(state, get_REUSABLE_CLAIMED())) {
        if (this._reusableCancellableContinuation_1.atomicfu$compareAndSet(get_REUSABLE_CLAIMED(), cause))
          return true;
      } else {
        if (state instanceof Error)
          return true;
        else {
          if (this._reusableCancellableContinuation_1.atomicfu$compareAndSet(state, null))
            return false;
        }
      }
    }
  };
  protoOf(DispatchedContinuation).takeState_a1bv3x_k$ = function () {
    var state = this._state_1;
    // Inline function 'kotlinx.coroutines.assert' call
    this._state_1 = get_UNDEFINED();
    return state;
  };
  protoOf(DispatchedContinuation).get_delegate_hasf9b_k$ = function () {
    return this;
  };
  protoOf(DispatchedContinuation).resumeWith_dtxwbr_k$ = function (result) {
    var state = toState_0(result);
    if (safeIsDispatchNeeded(this.dispatcher_1, this.get_context_h02k06_k$())) {
      this._state_1 = state;
      this.resumeMode_1 = 0;
      safeDispatch(this.dispatcher_1, this.get_context_h02k06_k$(), this);
    } else {
      $l$block: {
        // Inline function 'kotlinx.coroutines.internal.executeUnconfined' call
        // Inline function 'kotlinx.coroutines.assert' call
        var eventLoop = ThreadLocalEventLoop_getInstance().get_eventLoop_wo5hfs_k$();
        if (false && eventLoop.get_isUnconfinedQueueEmpty_mi405s_k$()) {
          break $l$block;
        }
        var tmp;
        if (eventLoop.get_isUnconfinedLoopActive_g78ri6_k$()) {
          this._state_1 = state;
          this.resumeMode_1 = 0;
          eventLoop.dispatchUnconfined_o79kaq_k$(this);
          tmp = true;
        } else {
          // Inline function 'kotlinx.coroutines.runUnconfinedEventLoop' call
          eventLoop.incrementUseCount_jadqvy_k$(true);
          try {
            this.get_context_h02k06_k$();
            // Inline function 'kotlinx.coroutines.withCoroutineContext' call
            this.countOrElement_1;
            this.continuation_1.resumeWith_dtxwbr_k$(result);
            $l$loop: while (eventLoop.processUnconfinedEvent_mypjl6_k$()) {
            }
          } catch ($p) {
            if ($p instanceof Error) {
              var e = $p;
              this.handleFatalException_fix1y3_k$(e);
            } else {
              throw $p;
            }
          }
          finally {
            eventLoop.decrementUseCount_x8i8ca_k$(true);
          }
          tmp = false;
        }
      }
    }
  };
  protoOf(DispatchedContinuation).toString = function () {
    return 'DispatchedContinuation[' + this.dispatcher_1.toString() + ', ' + toDebugString(this.continuation_1) + ']';
  };
  protoOf(DispatchedContinuation).get_context_h02k06_k$ = function () {
    return this.continuation_1.get_context_h02k06_k$();
  };
  function safeDispatch(_this__u8e3s4, context, runnable) {
    _init_properties_DispatchedContinuation_kt__tnmqc0();
    try {
      _this__u8e3s4.dispatch_qa3n0o_k$(context, runnable);
    } catch ($p) {
      if ($p instanceof Error) {
        var e = $p;
        throw new DispatchException(e, _this__u8e3s4, context);
      } else {
        throw $p;
      }
    }
  }
  function safeIsDispatchNeeded(_this__u8e3s4, context) {
    _init_properties_DispatchedContinuation_kt__tnmqc0();
    try {
      return _this__u8e3s4.isDispatchNeeded_ft82v4_k$(context);
    } catch ($p) {
      if ($p instanceof Error) {
        var e = $p;
        throw new DispatchException(e, _this__u8e3s4, context);
      } else {
        throw $p;
      }
    }
  }
  var properties_initialized_DispatchedContinuation_kt_2siadq;
  function _init_properties_DispatchedContinuation_kt__tnmqc0() {
    if (!properties_initialized_DispatchedContinuation_kt_2siadq) {
      properties_initialized_DispatchedContinuation_kt_2siadq = true;
      UNDEFINED = new Symbol('UNDEFINED');
      REUSABLE_CLAIMED = new Symbol('REUSABLE_CLAIMED');
    }
  }
  function DispatchedTask(resumeMode) {
    SchedulerTask.call(this);
    this.resumeMode_1 = resumeMode;
  }
  protoOf(DispatchedTask).cancelCompletedResult_pnx7en_k$ = function (takenState, cause) {
  };
  protoOf(DispatchedTask).getSuccessfulResult_4uqe9r_k$ = function (state) {
    return state;
  };
  protoOf(DispatchedTask).getExceptionalResult_i3cs19_k$ = function (state) {
    var tmp0_safe_receiver = state instanceof CompletedExceptionally ? state : null;
    return tmp0_safe_receiver == null ? null : tmp0_safe_receiver.cause_1;
  };
  protoOf(DispatchedTask).run_mvkpxh_k$ = function () {
    // Inline function 'kotlinx.coroutines.assert' call
    try {
      var tmp = this.get_delegate_hasf9b_k$();
      var delegate = tmp instanceof DispatchedContinuation ? tmp : THROW_CCE();
      var continuation = delegate.continuation_1;
      // Inline function 'kotlinx.coroutines.withContinuationContext' call
      delegate.countOrElement_1;
      var context = continuation.get_context_h02k06_k$();
      var state = this.takeState_a1bv3x_k$();
      var exception = this.getExceptionalResult_i3cs19_k$(state);
      var job = exception == null && get_isCancellableMode(this.resumeMode_1) ? context.get_y2st91_k$(Key_instance_2) : null;
      if (!(job == null) && !job.get_isActive_quafmh_k$()) {
        var cause = job.getCancellationException_8i1q6u_k$();
        this.cancelCompletedResult_pnx7en_k$(state, cause);
        // Inline function 'kotlinx.coroutines.resumeWithStackTrace' call
        // Inline function 'kotlin.Companion.failure' call
        var exception_0 = recoverStackTrace(cause, continuation);
        var tmp$ret$4 = _Result___init__impl__xyqfz8(createFailure(exception_0));
        continuation.resumeWith_dtxwbr_k$(tmp$ret$4);
      } else {
        if (!(exception == null)) {
          // Inline function 'kotlin.coroutines.resumeWithException' call
          // Inline function 'kotlin.Companion.failure' call
          var tmp$ret$6 = _Result___init__impl__xyqfz8(createFailure(exception));
          continuation.resumeWith_dtxwbr_k$(tmp$ret$6);
        } else {
          // Inline function 'kotlin.coroutines.resume' call
          // Inline function 'kotlin.Companion.success' call
          var value = this.getSuccessfulResult_4uqe9r_k$(state);
          var tmp$ret$8 = _Result___init__impl__xyqfz8(value);
          continuation.resumeWith_dtxwbr_k$(tmp$ret$8);
        }
      }
    } catch ($p) {
      if ($p instanceof DispatchException) {
        var e = $p;
        handleCoroutineException(this.get_delegate_hasf9b_k$().get_context_h02k06_k$(), e.cause_1);
      } else {
        if ($p instanceof Error) {
          var e_0 = $p;
          this.handleFatalException_fix1y3_k$(e_0);
        } else {
          throw $p;
        }
      }
    }
  };
  protoOf(DispatchedTask).handleFatalException_fix1y3_k$ = function (exception) {
    var reason = new CoroutinesInternalError('Fatal exception in coroutines machinery for ' + toString(this) + '. ' + "Please read KDoc to 'handleFatalException' method and report this incident to maintainers", exception);
    handleCoroutineException(this.get_delegate_hasf9b_k$().get_context_h02k06_k$(), reason);
  };
  function get_isReusableMode(_this__u8e3s4) {
    return _this__u8e3s4 === 2;
  }
  function get_isCancellableMode(_this__u8e3s4) {
    return _this__u8e3s4 === 1 || _this__u8e3s4 === 2;
  }
  function dispatch(_this__u8e3s4, mode) {
    // Inline function 'kotlinx.coroutines.assert' call
    var delegate = _this__u8e3s4.get_delegate_hasf9b_k$();
    var undispatched = mode === 4;
    var tmp;
    var tmp_0;
    if (!undispatched) {
      tmp_0 = delegate instanceof DispatchedContinuation;
    } else {
      tmp_0 = false;
    }
    if (tmp_0) {
      tmp = get_isCancellableMode(mode) === get_isCancellableMode(_this__u8e3s4.resumeMode_1);
    } else {
      tmp = false;
    }
    if (tmp) {
      var dispatcher = delegate.dispatcher_1;
      var context = delegate.get_context_h02k06_k$();
      if (safeIsDispatchNeeded(dispatcher, context)) {
        safeDispatch(dispatcher, context, _this__u8e3s4);
      } else {
        resumeUnconfined(_this__u8e3s4);
      }
    } else {
      resume(_this__u8e3s4, delegate, undispatched);
    }
  }
  function DispatchException(cause, dispatcher, context) {
    Exception_init_$Init$('Coroutine dispatcher ' + dispatcher.toString() + ' threw an exception, context = ' + toString(context), cause, this);
    captureStack(this, DispatchException);
    this.cause_1 = cause;
    delete this.cause;
  }
  protoOf(DispatchException).get_cause_iplhs0_k$ = function () {
    return this.cause_1;
  };
  function resumeUnconfined(_this__u8e3s4) {
    var eventLoop = ThreadLocalEventLoop_getInstance().get_eventLoop_wo5hfs_k$();
    if (eventLoop.get_isUnconfinedLoopActive_g78ri6_k$()) {
      eventLoop.dispatchUnconfined_o79kaq_k$(_this__u8e3s4);
    } else {
      // Inline function 'kotlinx.coroutines.runUnconfinedEventLoop' call
      eventLoop.incrementUseCount_jadqvy_k$(true);
      try {
        resume(_this__u8e3s4, _this__u8e3s4.get_delegate_hasf9b_k$(), true);
        $l$loop: while (eventLoop.processUnconfinedEvent_mypjl6_k$()) {
        }
      } catch ($p) {
        if ($p instanceof Error) {
          var e = $p;
          _this__u8e3s4.handleFatalException_fix1y3_k$(e);
        } else {
          throw $p;
        }
      }
      finally {
        eventLoop.decrementUseCount_x8i8ca_k$(true);
      }
    }
  }
  function resume(_this__u8e3s4, delegate, undispatched) {
    var state = _this__u8e3s4.takeState_a1bv3x_k$();
    var exception = _this__u8e3s4.getExceptionalResult_i3cs19_k$(state);
    var tmp;
    if (!(exception == null)) {
      // Inline function 'kotlin.Companion.failure' call
      tmp = _Result___init__impl__xyqfz8(createFailure(exception));
    } else {
      // Inline function 'kotlin.Companion.success' call
      var value = _this__u8e3s4.getSuccessfulResult_4uqe9r_k$(state);
      tmp = _Result___init__impl__xyqfz8(value);
    }
    var result = tmp;
    if (undispatched) {
      // Inline function 'kotlinx.coroutines.internal.DispatchedContinuation.resumeUndispatchedWith' call
      var this_0 = delegate instanceof DispatchedContinuation ? delegate : THROW_CCE();
      this_0.continuation_1;
      // Inline function 'kotlinx.coroutines.withContinuationContext' call
      this_0.countOrElement_1;
      this_0.continuation_1.resumeWith_dtxwbr_k$(result);
    } else {
      delegate.resumeWith_dtxwbr_k$(result);
    }
  }
  function Symbol(symbol) {
    this.symbol_1 = symbol;
  }
  protoOf(Symbol).toString = function () {
    return '<' + this.symbol_1 + '>';
  };
  var counter;
  function get_DEBUG() {
    return DEBUG;
  }
  var DEBUG;
  function get_classSimpleName(_this__u8e3s4) {
    var tmp0_elvis_lhs = getKClassFromExpression(_this__u8e3s4).get_simpleName_r6f8py_k$();
    return tmp0_elvis_lhs == null ? 'Unknown' : tmp0_elvis_lhs;
  }
  function get_hexAddress(_this__u8e3s4) {
    // Inline function 'kotlin.js.asDynamic' call
    var result = _this__u8e3s4.__debug_counter;
    if (!(typeof result === 'number')) {
      counter = counter + 1 | 0;
      result = counter;
      // Inline function 'kotlin.js.asDynamic' call
      _this__u8e3s4.__debug_counter = result;
    }
    return ((!(result == null) ? typeof result === 'number' : false) ? result : THROW_CCE()).toString();
  }
  function toDebugString(_this__u8e3s4) {
    return toString(_this__u8e3s4);
  }
  function SchedulerTask() {
  }
  function get_platformExceptionHandlers_() {
    _init_properties_CoroutineExceptionHandlerImpl_kt__37d7wf();
    return platformExceptionHandlers_;
  }
  var platformExceptionHandlers_;
  function get_platformExceptionHandlers() {
    _init_properties_CoroutineExceptionHandlerImpl_kt__37d7wf();
    return get_platformExceptionHandlers_();
  }
  function DiagnosticCoroutineContextException(context) {
    RuntimeException_init_$Init$_0(toString(context), this);
    captureStack(this, DiagnosticCoroutineContextException);
  }
  var properties_initialized_CoroutineExceptionHandlerImpl_kt_qhrgvx;
  function _init_properties_CoroutineExceptionHandlerImpl_kt__37d7wf() {
    if (!properties_initialized_CoroutineExceptionHandlerImpl_kt_qhrgvx) {
      properties_initialized_CoroutineExceptionHandlerImpl_kt_qhrgvx = true;
      // Inline function 'kotlin.collections.mutableSetOf' call
      platformExceptionHandlers_ = LinkedHashSet_init_$Create$();
    }
  }
  function LockFreeLinkedListHead() {
    LockFreeLinkedListNode.call(this);
  }
  function LockFreeLinkedListNode() {
    this._next_1 = this;
    this._prev_1 = this;
    this._removed_1 = false;
  }
  protoOf(LockFreeLinkedListNode).addLast_5b0i77_k$ = function (node, permissionsBitmask) {
    var prev = this._prev_1;
    var tmp;
    if (prev instanceof ListClosed) {
      tmp = ((prev.forbiddenElementsBitmask_1 & permissionsBitmask) === 0 && prev.addLast_5b0i77_k$(node, permissionsBitmask));
    } else {
      node._next_1 = this;
      node._prev_1 = prev;
      prev._next_1 = node;
      this._prev_1 = node;
      tmp = true;
    }
    return tmp;
  };
  protoOf(LockFreeLinkedListNode).remove_fgfybg_k$ = function () {
    if (this._removed_1)
      return false;
    var prev = this._prev_1;
    var next = this._next_1;
    prev._next_1 = next;
    next._prev_1 = prev;
    this._removed_1 = true;
    return true;
  };
  protoOf(LockFreeLinkedListNode).addOneIfEmpty_2jwoix_k$ = function (node) {
    if (!(this._next_1 === this))
      return false;
    this.addLast_5b0i77_k$(node, -2147483648);
    return true;
  };
  function ListClosed() {
  }
  function recoverStackTrace(exception, continuation) {
    return exception;
  }
  function SynchronizedObject() {
  }
  function threadContextElements(context) {
    return 0;
  }
  function CommonThreadLocal() {
    this.value_1 = null;
  }
  protoOf(CommonThreadLocal).get_26vq_k$ = function () {
    return this.value_1;
  };
  protoOf(CommonThreadLocal).set_tg4fwj_k$ = function (value) {
    this.value_1 = value;
  };
  function commonThreadLocal(name) {
    return new CommonThreadLocal();
  }
  function createEventLoop() {
    return new UnconfinedEventLoop();
  }
  function UnconfinedEventLoop() {
    EventLoop.call(this);
  }
  protoOf(UnconfinedEventLoop).dispatch_qa3n0o_k$ = function (context, block) {
    unsupported();
  };
  function unsupported() {
    throw UnsupportedOperationException_init_$Create$('runBlocking event loop is not supported');
  }
  function propagateExceptionFinalResort(exception) {
    // Inline function 'kotlin.with' call
    throwAsyncJsError(exception.message, getKClassFromExpression(exception).get_simpleName_r6f8py_k$(), stackTraceToString(exception));
    return Unit_instance;
  }
  function throwAsyncJsError(message, className, stack) {
    var error = new Error();
    error.message = message;
    error.name = className;
    error.stack = stack;
    if (typeof globalThis.reportError === 'function') {
      // Up-to-date browsers and some non-browser JS runtimes (Deno, Bun)
      globalThis.reportError(error);
    } else {
      // Fallback for old browsers (pre-2022), Node.js
      setTimeout(function () {
        throw error;
      }, 0);
    }
  }
  //region block: post-declaration
  protoOf(CoroutineDispatcher).get_y2st91_k$ = get;
  defineProp(protoOf(DispatchException), 'cause', function () {
    return this.get_cause_iplhs0_k$();
  });
  //endregion
  //region block: init
  Active_instance = new Active();
  Key_instance_1 = new Key_0();
  Key_instance_2 = new Key_1();
  NonDisposableHandle_instance = new NonDisposableHandle();
  counter = 0;
  DEBUG = false;
  //endregion
  //region block: exports
  _.$_$ = _.$_$ || {};
  _.$_$.a = MutableStateFlow;
  _.$_$.b = CancellableContinuationImpl;
  //endregion
  return _;
}(module.exports, require('./kotlin-kotlin-stdlib.js'), require('./kotlinx-atomicfu.js')));

//# sourceMappingURL=kotlinx-coroutines-core.js.map
