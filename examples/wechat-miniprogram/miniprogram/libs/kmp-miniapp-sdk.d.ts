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
