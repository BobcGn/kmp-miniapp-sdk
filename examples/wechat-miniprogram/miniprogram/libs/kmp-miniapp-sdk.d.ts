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

/**
 * Coordinate system a WeChat position is reported in.
 *
 * `wgs84` is the raw satellite fix; `gcj02` is the offset system Chinese map data
 * uses. They are not interchangeable, and a coordinate used in the wrong one
 * lands hundreds of metres away.
 */
export type GeoCoordinateSystem = 'wgs84' | 'gcj02';

/** One position WeChat reported, in the system that was requested. */
export interface GeoPosition {
  /** Degrees, in -90..90; negative is south. */
  readonly latitude: number;
  /** Degrees, in -180..180; negative is west. */
  readonly longitude: number;
  /** Radius of the horizontal uncertainty in metres; never negative. */
  readonly accuracyMeters: number;
  /** The system the coordinates are in. */
  readonly coordinateSystem: GeoCoordinateSystem;
}

/**
 * Obtains the device's current position.
 *
 * The coordinates default to `gcj02`, because that is the system WeChat's own map
 * views accept.
 *
 * Three separate conditions decide whether this resolves: whether WeChat exposes
 * the API, whether `scope.userLocation` is granted, and whether the host's privacy
 * contract has been accepted. Use {@link capabilitySupport}, {@link permissionState}
 * and {@link privacyStatus} to observe each on its own. This call never prompts for
 * a permission and never accepts a privacy contract on the user's behalf.
 *
 * The result is a precise position: do not render it, log it, or send it anywhere
 * the user has not agreed to.
 */
export function wechatGetCurrentLocation(
  coordinateSystem?: GeoCoordinateSystem,
): Promise<GeoPosition>;

/**
 * A scan category `wechatScanCode` can ask for.
 *
 * These are the coarse categories the request accepts. They are not what a
 * completed scan reports: {@link ScanResult.scanType} names the specific format
 * the host decoded, which {@link ScanFormat} lists.
 */
export type ScanCategory = 'barCode' | 'qrCode' | 'datamatrix' | 'pdf417';

/**
 * A scan format this SDK recognizes.
 *
 * The host's vocabulary is open, so a decoded format it reports may not be a
 * member. That is not a failure: {@link ScanResult.scanType} then carries the
 * host's own name while {@link ScanResult.format} is `null`.
 */
export type ScanFormat =
  | 'QR_CODE'
  | 'AZTEC'
  | 'CODABAR'
  | 'CODE_39'
  | 'CODE_93'
  | 'CODE_128'
  | 'DATA_MATRIX'
  | 'EAN_8'
  | 'EAN_13'
  | 'ITF'
  | 'MAXICODE'
  | 'PDF_417'
  | 'RSS_14'
  | 'RSS_EXPANDED'
  | 'UPC_A'
  | 'UPC_E'
  | 'UPC_EAN_EXTENSION'
  | 'WX_CODE'
  | 'CODE_25';

/** One scan WeChat reported. */
export interface ScanResult {
  /**
   * The decoded content.
   *
   * This is the user's data, handed over because the caller asked to scan. The SDK
   * never logs, stores, or uploads it, and a caller that does takes on the
   * responsibility that comes with content the user chose to scan.
   */
  readonly text: string;
  /** The host's own name for the format, verbatim, when it reported one. */
  readonly scanType: string | null | undefined;
  /** `scanType` resolved against {@link ScanFormat}, or `null` when unrecognized. */
  readonly format: ScanFormat | null | undefined;
  /** The character set of the decoded content, when the host reported one. */
  readonly charSet: string | null | undefined;
  /** The decoded bytes as the host reports them, when it reported them. */
  readonly rawData: string | null | undefined;
  /** A path to the scanned image, when the host reported one. */
  readonly path: string | null | undefined;
}

/**
 * Asks WeChat to scan through its own scanning interface.
 *
 * Resolves with what the host decoded. A host cancel signal rejects with
 * `HostInteractionInterrupted`: a real device uses that same signal for a user
 * dismissal and for camera access preventing launch, so the SDK does not infer
 * which cause occurred. Other failures reject as host failures or invalid responses.
 *
 * This call asks the host for no permission. `wx.scanCode` drives WeChat's own
 * interface, and no permission precondition for it could be established from the
 * host contract, so nothing is prompted for.
 *
 * @param onlyFromCamera whether the host must scan through its camera rather than
 *   also accepting an image the user already has; defaults to `false`
 * @param scanTypes categories to ask for; an empty or omitted array asks for every
 *   category the host supports, which is not the same as asking for none
 */
export function wechatScanCode(
  onlyFromCamera?: boolean,
  scanTypes?: readonly ScanCategory[],
): Promise<ScanResult>;

/** A media type {@link wechatChooseMedia} can ask for. */
export type MediaType = 'image' | 'video' | 'mix';

/** Where {@link wechatChooseMedia} may take media from. */
export type MediaSource = 'album' | 'camera';

/** How much {@link wechatChooseMedia} may compress an image it returns. */
export type MediaSizeType = 'original' | 'compressed';

/** Which camera {@link wechatChooseMedia} may use. */
export type CameraPosition = 'back' | 'front';

/** A kind of file the host may report. */
export type MediaFileType = 'image' | 'video';

/**
 * One file the user selected.
 *
 * The path is a host temporary resource, not a durable one: it belongs to the
 * session that produced it and nothing in the host contract promises it survives.
 * Copy it to storage you own before relying on it.
 *
 * The SDK never logs, stores, or uploads what the user selected. A caller that
 * keeps it takes on the responsibility that comes with it.
 */
export interface MediaFile {
  /** The host's temporary path to the selected file. */
  readonly tempFilePath: string;
  /** The file's size in bytes. */
  readonly sizeBytes: number;
  /** The kind resolved against this SDK's known set, or `null` when unrecognized. */
  readonly fileType: MediaFileType | null | undefined;
  /** The host's required name for the kind, verbatim. */
  readonly hostFileType: string;
  /** A video's duration in seconds; `null` for an image or when unreported. */
  readonly durationSeconds: number | null | undefined;
  /** A video's width in pixels, or `null` when the host reported none. */
  readonly width: number | null | undefined;
  /** A video's height in pixels, or `null` when the host reported none. */
  readonly height: number | null | undefined;
  /** A temporary path to a video's thumbnail, or `null` when the host reported none. */
  readonly thumbTempFilePath: string | null | undefined;
}

/** What one media selection asks the host for. */
export interface MediaRequest {
  /**
   * Media types to ask for.
   *
   * WeChat documents this option as required, so it must not be empty.
   */
  readonly mediaTypes: readonly MediaType[];
  /**
   * Maximum number of files to ask for; defaults to `1`.
   *
   * The host applies its own limit and may return fewer than requested.
   */
  readonly count?: number;
  /** Where the host may take media from; an empty or omitted array means no restriction. */
  readonly sourceTypes?: readonly MediaSource[];
  /**
   * Longest video recording to ask for, in seconds.
   *
   * WeChat documents the range as 3 to 60, and states that it does not restrict
   * album selection. Omit it for the host's own default.
   */
  readonly maxDurationSeconds?: number | null;
  /** How much the host may compress returned images; effective only for images. */
  readonly sizeTypes?: readonly MediaSizeType[];
  /** Which camera to use; effective only when `camera` is among `sourceTypes`. */
  readonly camera?: CameraPosition | null;
}

/**
 * Asks WeChat to let the user choose images or videos through its own picker.
 *
 * Resolves with the non-empty file array the host returned. A success callback
 * without a selected file is rejected as an invalid host response.
 *
 * The interaction ending without a selection rejects with the SDK's
 * host-interrupted error (`error.name === 'HostInteractionInterrupted'`), which does
 * not claim to know whether the user dismissed the picker or something prevented it
 * from completing. Every other failure rejects as a host failure or an invalid
 * response.
 *
 * This call asks the host for no permission: WeChat's own picker needs none.
 */
export function wechatChooseMedia(request: MediaRequest): Promise<MediaFile[]>;

/**
 * Reads the system clipboard as text.
 *
 * An empty clipboard resolves with an empty string. The clipboard belongs to the
 * user: the SDK neither stores nor logs what it reads.
 */
export function wechatGetClipboardText(): Promise<string>;

/**
 * Replaces the system clipboard with `value`.
 *
 * @param value text to place on the clipboard
 */
export function wechatSetClipboardText(value: string): Promise<void>;

/**
 * Performs WeChat's short vibration.
 *
 * Resolving means the host accepted and performed the call. It is not evidence
 * that anyone felt a vibration, which no software check can confirm.
 */
export function wechatVibrateShort(): Promise<void>;

/**
 * Performs WeChat's long vibration.
 *
 * A separate host API from the short one, and it may be absent while the short
 * one is present.
 */
export function wechatVibrateLong(): Promise<void>;

/**
 * Returns the sandbox root the file functions below work against.
 *
 * The caller builds paths from this. The SDK does not normalise paths and makes
 * no claim to prevent traversal, so passing one outside the sandbox is the
 * caller's mistake rather than something this refuses.
 */
export function wechatUserDataPath(): string;

/**
 * Reads a UTF-8 text file from the mini program file sandbox.
 *
 * An empty file resolves with an empty string.
 *
 * @param path file path inside the sandbox
 */
export function wechatReadTextFile(path: string): Promise<string>;

/**
 * Writes UTF-8 text to a file in the mini program file sandbox, replacing what is
 * there.
 *
 * The parent directory must already exist; this creates none.
 */
export function wechatWriteTextFile(path: string, content: string): Promise<void>;

/**
 * Reports whether a sandbox path exists and is accessible.
 *
 * A path the host reports as missing resolves `false`. Any other failure rejects,
 * so a permission error is never reported as a missing file.
 */
export function wechatFileExists(path: string): Promise<boolean>;

/**
 * Removes a file from the mini program file sandbox.
 *
 * Removing a file that is not there rejects, because that is what WeChat's
 * `unlink` does. Check {@link wechatFileExists} first when that matters.
 */
export function wechatRemoveFile(path: string): Promise<void>;

/**
 * Whether WeChat still holds a usable client login session.
 *
 * `Valid` says only that WeChat's own client login state is intact. It is not an
 * authenticated user, a consumer backend session, or proof that any credential
 * the consumer holds is still accepted.
 */
export type WeChatSessionState = 'Valid' | 'Invalid';

/**
 * Reports whether WeChat still holds a usable login session.
 *
 * An `Invalid` result is a query result and changes nothing by itself: no code is
 * acquired, no session is exchanged, and no token is refreshed. Call
 * {@link wechatLogin} when a new code is needed.
 */
export function wechatCheckSession(): Promise<WeChatSessionState>;

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
export type PermissionName = 'microphone' | 'location';

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
 * What the host currently requires for its privacy contract.
 *
 * `NOT_REQUIRED` means the host requires nothing right now; it is not proof that
 * the user agreed, because a host may also report it when the mini program
 * declares no personal-data collection at all.
 */
export type PrivacyRequirement = 'REQUIRED' | 'NOT_REQUIRED';

/** Result of {@link privacyStatus}. */
export interface PrivacyStatusResult {
  readonly requirement: PrivacyRequirement;
  /** The host's own name for its privacy contract, or null when it reports none. */
  readonly contractName: string | null | undefined;
}

/**
 * Returns what the host currently requires for its privacy contract.
 *
 * Privacy is not a permission: the host tracks its privacy contract separately
 * from the system permissions the permission functions above report.
 */
export function privacyStatus(): Promise<PrivacyStatusResult>;

/** What one privacy authorization attempt reported. */
export type PrivacyAuthorizationResult = 'Authorized' | 'Refused';

/**
 * Asks the host to obtain the user's acceptance of its privacy contract.
 *
 * Must be called from a user gesture, because the host presents its own prompt.
 * A refusal resolves with `Refused`: it is the user's answer, not a failure, so
 * only a host-level problem rejects. A declined and a dismissed prompt are
 * reported the same way, because the host does not distinguish them.
 */
export function requestPrivacyAuthorization(): Promise<PrivacyAuthorizationResult>;

/**
 * Fails unless the host currently requires no privacy authorization.
 *
 * The precondition point for capabilities the host gates behind its privacy
 * contract. It shows nothing and starts no prompt, so a capability that needs the
 * user to accept the contract reports that and leaves the consumer to ask again
 * from a gesture.
 */
export function requirePrivacySatisfied(): Promise<void>;

/**
 * Opens the host's permission settings and resolves with the state afterwards.
 *
 * Must be called from a user gesture, because it leaves the mini program.
 * Resolving does not mean the permission was granted: the settings page can close
 * with the permission still refused.
 */
export function openPermissionSettings(permission: PermissionName): Promise<PermissionState>;
