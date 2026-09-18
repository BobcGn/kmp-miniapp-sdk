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
 * A per-template answer this SDK is able to name.
 *
 * **This union is deliberately narrow.** WeChat's own status vocabulary for this API
 * could not be verified from the sources available offline: the base library shipped
 * with the installed WeChat Developer Tools declares the API's option and states that
 * the success result is keyed by template ID, but names no status value and ships no
 * implementation of it. The one status string observed anywhere in that bundle is
 * `accept`, in the simulator's prompt preview payload, which is prompt-rendering data
 * rather than an API result. Only what could be evidenced is named here.
 *
 * A status the host reports that is not a member is **not** guessed at: it arrives in
 * {@link SubscriptionResult.hostStatus} with `status` left `null`, so a caller can act
 * on what the host actually said. A real-device run is expected to extend this union
 * from evidence.
 */
export type SubscriptionStatus = 'accept';

/** What the host answered about one requested template. */
export interface SubscriptionResult {
  /** The template this entry answers, echoed from the request. */
  readonly templateId: string;
  /** The answer resolved against {@link SubscriptionStatus}, or `null` when unnamed. */
  readonly status: SubscriptionStatus | null | undefined;
  /** The host's own non-blank answer, verbatim. */
  readonly hostStatus: string;
}

/**
 * Asks WeChat to put message templates in front of the user.
 *
 * **The caller must call this from a user gesture.** WeChat requires one, and the SDK
 * neither supplies one nor retries when the host refuses because none preceded the
 * call. Nothing in this SDK calls it on page load.
 *
 * Resolves with exactly one entry per requested template, in the order given.
 * Missing, unexpected, blank, or non-text answers reject as invalid responses.
 *
 * An answer says what the user decided about a template. It never says a message was
 * sent or delivered: acceptance is a subscription state, and sending is the consumer
 * backend's business.
 *
 * Failures remain host failures until a real-host run establishes an exact dismissal
 * signal for this API. Malformed success values reject as invalid responses.
 *
 * This call asks the host for no permission: the offline sources for this API name no
 * scope for it.
 *
 * @param templateIds templates to ask about; blank identifiers are refused
 */
export function wechatRequestSubscribeMessage(
  templateIds: readonly string[],
): Promise<SubscriptionResult[]>;

/**
 * The kind of network connection a host reports, as this SDK names it.
 *
 * The vocabulary is the host's, so a connection kind this union does not name still
 * arrives: {@link NetworkState.networkType} is then `null` and
 * {@link NetworkState.hostNetworkType} carries the host's own word.
 */
export type NetworkType =
  | 'WIFI'
  | 'CELLULAR_2G'
  | 'CELLULAR_3G'
  | 'CELLULAR_4G'
  | 'CELLULAR_5G'
  | 'UNKNOWN'
  | 'NONE';

/** What the host reports about its network. */
export interface NetworkState {
  /** Whether the host currently has a usable connection. */
  readonly isConnected: boolean;
  /** The kind of link, resolved against {@link NetworkType} when recognized. */
  readonly networkType: NetworkType | null | undefined;
  /** The host's own word for the link, verbatim. */
  readonly hostNetworkType: string;
}

/**
 * Returns the host's current network state.
 *
 * Rejects with the SDK's unsupported-capability error on a host that cannot answer.
 */
export function networkStatus(): Promise<NetworkState>;

/**
 * Begins observing network status changes.
 *
 * WeChat pushes changes, and this registers one host listener for the session. Nothing
 * polls. Calling this while already observing does nothing.
 */
export function startNetworkStatusObservation(): void;

/**
 * Stops observing and resolves with every state the session saw.
 *
 * Resolves after the host listener is removed, so nothing observed arrives afterwards.
 * Only the most recent states are kept, because a session has no natural end.
 */
export function stopNetworkStatusObservation(): Promise<NetworkState[]>;

/**
 * Why the last observation session ended on its own, or `null` when it was stopped.
 *
 * A host that reports a network state this SDK cannot read ends the session, and this
 * is how the caller learns that rather than seeing an empty result.
 */
export function networkStatusObservationFailure(): string | null | undefined;

/** What one upload asks the host for. */
export interface UploadRequest {
  /** Absolute HTTPS endpoint to post to. */
  readonly url: string;
  /** Path to the file inside the mini program file sandbox. */
  readonly filePath: string;
  /** Form field name the host gives the file; defaults to `file`. */
  readonly name?: string;
  /** Request headers. */
  readonly headers?: Readonly<Record<string, string>>;
  /** Additional multipart form fields. */
  readonly formData?: Readonly<Record<string, string>>;
  /** Host timeout in milliseconds; omit for the host default. */
  readonly timeoutMillis?: number | null;
}

/** What one download asks the host for. */
export interface DownloadRequest {
  /** Absolute endpoint to fetch from. */
  readonly url: string;
  /** Request headers. */
  readonly headers?: Readonly<Record<string, string>>;
  /** Host timeout in milliseconds; omit for the host default. */
  readonly timeoutMillis?: number | null;
  /** Where the host should put the file; omit for its own temporary location. */
  readonly filePath?: string | null;
}

/** Progress the host reported for an in-flight transfer. */
export interface TransferProgress {
  /** Percentage complete, in `0..100`, as the host reported it. */
  readonly percent: number;
  /** Bytes transferred so far, when the host reports that figure. */
  readonly bytesTransferred: number | null | undefined;
  /** Bytes expected in total, when the host reports that figure. */
  readonly bytesExpected: number | null | undefined;
}

/** One completed upload. */
export interface UploadResult {
  /** HTTP status the host received, including `4xx` and `5xx`. */
  readonly statusCode: number;
  /** The response body as text. */
  readonly responseText: string;
}

/** One completed download. */
export interface DownloadResult {
  /** HTTP status the host received. */
  readonly statusCode: number;
  /** The host's temporary copy of the downloaded content. */
  readonly tempFilePath: string;
  /** The path the host reports for a requested target, else `null`. */
  readonly filePath: string | null | undefined;
}

/**
 * Handle for an in-flight upload.
 *
 * A completed upload resolves even when its status reports an HTTP error; only a
 * transport failure rejects. After {@link abort} it rejects, because a transfer the
 * caller stopped has no answer — a caller that aborted knows it did.
 */
export interface UploadTransfer {
  /** Whether this transfer can still be aborted. */
  readonly abortable: boolean;
  /** Stops the transfer, at most once. Returns whether a host abort was invoked. */
  abort(): boolean;
  /** The most recent progress the host reported, or `null` when it reported none. */
  progress(): TransferProgress | null;
  /** Resolves with the host's answer, or rejects on failure or after an abort. */
  result(): Promise<UploadResult>;
}

/** Handle for an in-flight download. Behaviour matches {@link UploadTransfer}. */
export interface DownloadTransfer {
  /** Whether this transfer can still be aborted. */
  readonly abortable: boolean;
  /** Stops the transfer, at most once. Returns whether a host abort was invoked. */
  abort(): boolean;
  /** The most recent progress the host reported, or `null` when it reported none. */
  progress(): TransferProgress | null;
  /** Resolves with the host's answer, or rejects on failure or after an abort. */
  result(): Promise<DownloadResult>;
}

/**
 * Uploads a file to an HTTP endpoint through WeChat.
 *
 * The SDK reads no file, logs no path, and defines no upload protocol: it carries what
 * the request describes to the host and reports what came back.
 */
export function wechatUploadFile(request: UploadRequest): UploadTransfer;

/**
 * Downloads a URL into WeChat's file system.
 *
 * The result carries the host's own file reference. The SDK reads no content, moves
 * nothing, and makes no claim that the path outlives the session.
 */
export function wechatDownloadFile(request: DownloadRequest): DownloadTransfer;

/**
 * The parameters of one payment interaction, exactly as a trusted backend produced them.
 *
 * **None of this originates in the SDK or in the client.** The SDK does not sign, holds
 * no merchant key, and does not obtain a prepay identifier; these are what WeChat Pay's
 * unified-order API returned to your backend. Do not print or store them: `nonceStr`,
 * `package`, and `paySign` are credentials for this one payment.
 */
export type PaymentSignType = 'MD5' | 'HMAC-SHA256';

export interface PaymentRequest {
  /** Backend-provided timestamp. */
  readonly timeStamp: string;
  /** Backend-provided random string. */
  readonly nonceStr: string;
  /** Backend-provided prepay package, sent to the host under its own `package` name. */
  readonly package: string;
  /** `MD5` or `HMAC-SHA256`: whichever the backend signed with. */
  readonly signType: PaymentSignType;
  /** Backend-provided signature. */
  readonly paySign: string;
}

/**
 * What a completed WeChat payment interaction means, and nothing more.
 *
 * The host's success callback reports that the payment interaction ended. It is not an
 * order, not a receipt, and not proof that money moved, so the field below is the only
 * thing this value can say. Ask your backend — which learns the truth from WeChat Pay's
 * server API, its asynchronous notification, or an order query — before delivering
 * anything on the strength of a resolved promise.
 */
export interface PaymentOutcome {
  /**
   * Whether the host reported the payment interaction completed. `true` whenever this
   * value exists at all: the field names what resolving means, so a caller cannot read a
   * completed call as a completed order.
   */
  readonly interactionCompleted: boolean;
}

/**
 * Asks WeChat to run the standard payment interaction for backend-produced parameters.
 *
 * **A resolved promise is not a paid order.** Call it only from a user gesture, because
 * WeChat requires one and the SDK neither supplies one nor retries. An ended interaction
 * rejects with the SDK's host-interrupted error; a payment failure rejects as a host
 * failure; a host without the API rejects as an unsupported capability.
 */
export function wechatRequestPayment(request: PaymentRequest): Promise<PaymentOutcome>;

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
 * Switches to the tabBar page named by `url`.
 *
 * WeChat accepts only a route declared in the mini program's own `tabBar`, and
 * reports any other route as a failure; the SDK keeps no list of tabBar pages to
 * check against. The route carries no query, because a tabBar page is switched
 * to rather than opened with parameters. Switching to the page already shown is
 * a success.
 *
 * Rejects with an `IllegalArgumentException` when `url` is blank, before the
 * host is called.
 */
export function wechatSwitchTab(url: string): Promise<void>;

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

/**
 * One device a Bluetooth discovery session reported.
 *
 * `deviceId` is the host's own identifier. On some platforms it is a MAC address,
 * so it is identifying: log a shortened or session-local form, never the value.
 */
export interface BleDevice {
  readonly deviceId: string;
  /** The advertised name, or `null` when the device advertised none. */
  readonly name: string | null | undefined;
  /** The reported signal strength, or `null` when the host reported none. */
  readonly rssi: number | null | undefined;
}

/**
 * What the host reports about its Bluetooth adapter.
 *
 * `discovering` and `powered` are `null` when the source of the state does not
 * report them, which is not the same as `false`.
 */
export interface BleAdapterState {
  readonly available: boolean;
  readonly discovering: boolean | null | undefined;
  readonly powered: boolean | null | undefined;
}

/** One connection state change. */
export interface BleConnectionState {
  readonly deviceId: string;
  readonly connected: boolean;
}

/**
 * Reports what the host says about its Bluetooth adapter.
 *
 * Experimental, like every BLE export here: this is a proof of concept for the SDK's
 * event-driven resource model, not a Bluetooth API. Rejects when the adapter cannot
 * be queried, including when the host has no Bluetooth adapter API at all.
 */
export function wechatBleAdapterState(): Promise<BleAdapterState>;

/** Opens the adapter and starts collecting connection state changes. */
export function wechatBleOpenAdapter(): Promise<void>;

/** Stops collecting, then closes the adapter. Closing a closed adapter does nothing. */
export function wechatBleCloseAdapter(): Promise<void>;

/** Starts a scan and begins collecting the devices it reports. */
export function wechatBleStartDiscovery(): Promise<void>;

/** Stops the scan and stops collecting. Stopping a scan that is not running does nothing. */
export function wechatBleStopDiscovery(): Promise<void>;

/**
 * The devices this session has seen, oldest first, at most 32.
 *
 * One entry per device: a device the host reports again is not repeated.
 */
export function wechatBleDevices(): BleDevice[];

/** How the discovery stream ended, or `null` when it has not failed. */
export function wechatBleDiscoveryFailure(): string | null | undefined;

/** Connects to `deviceId`, which is a host value and must not be logged. */
export function wechatBleConnect(deviceId: string): Promise<void>;

/** Disconnects from `deviceId`. */
export function wechatBleDisconnect(deviceId: string): Promise<void>;

/** The connection state changes this session has seen, oldest first, at most 32. */
export function wechatBleConnectionStates(): BleConnectionState[];

/** How the connection-state stream ended, or `null` when it has not failed. */
export function wechatBleConnectionFailure(): string | null | undefined;

/**
 * How many host listeners the BLE adapter currently holds.
 *
 * This is what the proof of concept exists to account for: it reaches zero once every
 * stream has ended, and a number above zero after a stop is a listener still held.
 */
export function wechatBleListenerCount(): number;
