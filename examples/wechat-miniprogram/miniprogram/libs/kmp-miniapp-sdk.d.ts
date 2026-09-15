export function sdkVersion(): string;
export function storageGet(key: string): Promise<string | null | undefined>;
export function storageSet(key: string, value: string): Promise<void>;
export function storageRemove(key: string): Promise<void>;

/** Short-lived WeChat credential; it is not an authenticated user or session. */
export interface WeChatLoginResult {
  readonly code: string;
}

/** Obtains a code that must be exchanged by a trusted consumer backend. */
export function wechatLogin(): Promise<WeChatLoginResult>;

/** Options accepted by {@link networkRequest}. */
export interface NetworkRequestInit {
  /** HTTP method name; defaults to `GET`. */
  readonly method?: string;
  /** Request headers. */
  readonly headers?: Readonly<Record<string, string>>;
  /** Already-encoded request body; omit for no body. */
  readonly body?: string | null;
  /** Host timeout in milliseconds; `null` or omitted applies the host default. */
  readonly timeoutMillis?: number | null;
}

/** Result of a completed HTTP exchange. */
export interface NetworkResponse {
  /** HTTP status code, including `4xx` and `5xx`. */
  readonly statusCode: number;
  /** Response headers the host reported as single string values. */
  readonly headers: Readonly<Record<string, string>>;
  /** Response body decoded as text. */
  readonly body: string;
}

/**
 * Performs an HTTP exchange and resolves with its result.
 *
 * A completed exchange resolves even when its status reports an HTTP error. Only
 * a transport failure rejects: an expired timeout and any other host failure are
 * reported as distinct SDK errors.
 */
export function networkRequest(
  url: string,
  init?: NetworkRequestInit,
): Promise<NetworkResponse>;

/**
 * App-level lifecycle state, the only lifecycle concept the SDK models as
 * host-neutral. Page-level lifecycle is WeChat-specific and stays below.
 */
export type AppLifecycleState = 'FOREGROUND' | 'BACKGROUND';

/** Forwards WeChat's `App.onLaunch` hook. */
export function wechatAppOnLaunch(): void;

/** Forwards WeChat's `App.onShow` hook. */
export function wechatAppOnShow(): void;

/** Forwards WeChat's `App.onHide` hook. */
export function wechatAppOnHide(): void;

/** Returns the app-level lifecycle state WeChat reported most recently. */
export function wechatAppLifecycleState(): AppLifecycleState;

/**
 * Forwards WeChat's `Page.onShow` hook with the page's own `this.route`.
 *
 * `Page.onLoad` is not forwarded: a page becomes the current one when it is
 * shown, and `onShow` reports that same route.
 */
export function wechatPageOnShow(route: string): void;

/** Forwards WeChat's `Page.onHide` hook. */
export function wechatPageOnHide(): void;

/** Forwards WeChat's `Page.onUnload` hook with the page's own `this.route`. */
export function wechatPageOnUnload(route: string): void;

/** Returns the route of the page the runtime reported as shown most recently. */
export function wechatPageRoute(): string | null | undefined;

/**
 * Opens `url` on top of the current WeChat page stack.
 *
 * WeChat keeps a bounded stack, so this rejects even for a valid `url` when the
 * stack is full. Navigation is WeChat-specific, not a portable capability.
 */
export function wechatNavigateTo(url: string): Promise<void>;

/** Replaces the current page with `url`, removing it from the stack. */
export function wechatRedirectTo(url: string): Promise<void>;

/**
 * Pops `delta` pages off the current page stack, or one page when `delta` is
 * omitted. Going back from the first page rejects.
 */
export function wechatNavigateBack(delta?: number | null): Promise<void>;

/**
 * How the host currently supports a capability.
 *
 * A host answers from its own runtime, so the same build can answer differently
 * on two hosts. `VersionDependent` and `PermissionDependent` each mean the
 * capability is not available here.
 */
export type CapabilityState =
  | 'Supported'
  | 'Unsupported'
  | 'VersionDependent'
  | 'PermissionDependent';

/** Result of {@link capabilitySupport}. */
export interface CapabilitySupportResult {
  readonly state: CapabilityState;
  /** Oldest host version providing the capability; set for `VersionDependent`. */
  readonly requiredVersion: string | null | undefined;
  /** Host version observed; set for `VersionDependent` when it could be read. */
  readonly currentVersion: string | null | undefined;
  /** Permission the capability depends on; set for `PermissionDependent`. */
  readonly permission: string | null | undefined;
}

/**
 * Returns how the host currently supports `capability`.
 *
 * `storage`, `network`, and `lifecycle` are the capabilities this SDK gates.
 */
export function capabilitySupport(capability: string): CapabilitySupportResult;

/**
 * Fails unless the host currently supports `capability`.
 *
 * Every state other than `Supported` fails, so use this only when the caller
 * cannot proceed without the capability.
 */
export function requireCapability(capability: string): void;

/** What the WeChat runtime reports about itself. */
export interface RuntimeInfo {
  /** Base library version, or null when the runtime cannot report one. */
  readonly baseLibraryVersion: string | null | undefined;
  /** Runtime platform, for example `devtools`, or null when unreadable. */
  readonly platform: string | null | undefined;
  /** Whether this runtime is WeChat Developer Tools rather than a device. */
  readonly isDeveloperTools: boolean;
}

/** Returns what the WeChat runtime reports about itself. */
export function wechatRuntimeInfo(): RuntimeInfo;

/**
 * Reports whether a WeChat API, parameter, or component exists in this base
 * library.
 *
 * This is the live answer for the running host, so a capability the SDK does not
 * gate can still be probed here.
 */
export function wechatCanIUse(schema: string): boolean;

/**
 * What a host currently knows about a permission.
 *
 * `NotRequested` and `Denied` are different answers: only the first can still be
 * resolved by asking again, because a host will not re-prompt after a refusal.
 */
export type PermissionState = 'NotRequested' | 'Granted' | 'Denied';

/** Permissions this SDK maps to a host scope. */
export type PermissionName = 'microphone';

/**
 * Returns the host's current state for `permission`.
 *
 * Every call asks the host. The answer is never a cached fact, because the user
 * can change a permission in the host's own settings at any time.
 */
export function permissionState(permission: PermissionName): Promise<PermissionState>;

/**
 * Asks the host for `permission` and resolves with the state afterwards.
 *
 * Must be called from a user gesture. Rejects with the SDK permission-denied
 * error when the host refuses, which is not a host failure. A permission the host
 * has already refused is reported as denied without a second prompt; the user has
 * to re-enable it through {@link openPermissionSettings}.
 */
export function requestPermission(permission: PermissionName): Promise<PermissionState>;

/**
 * Opens the host's permission settings and resolves with the state afterwards.
 *
 * Must be called from a user gesture, because it leaves the mini program.
 * Resolving does not mean the permission was granted: the settings page can close
 * with the permission still refused.
 */
export function openPermissionSettings(permission: PermissionName): Promise<PermissionState>;
