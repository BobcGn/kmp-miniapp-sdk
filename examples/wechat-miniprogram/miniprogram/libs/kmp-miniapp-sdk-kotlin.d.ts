type Nullable<T> = T | null | undefined
declare function KtSingleton<T>(): T & (abstract new() => any);
export declare namespace io.github.bobcgn.miniapp.export {
    class JsCapabilitySupport {
        private constructor();
        get state(): string;
        get requiredVersion(): Nullable<string>;
        get currentVersion(): Nullable<string>;
        get permission(): Nullable<string>;
    }
    namespace JsCapabilitySupport {
        /** @deprecated $metadata$ is used for internal purposes, please don't use it in your code, because it can be removed at any moment */
        namespace $metadata$ {
            const constructor: abstract new () => JsCapabilitySupport;
        }
    }
}
export declare namespace io.github.bobcgn.miniapp.export {
    class JsGeoPosition {
        private constructor();
        get latitude(): number;
        get longitude(): number;
        get accuracyMeters(): number;
        get coordinateSystem(): string;
    }
    namespace JsGeoPosition {
        /** @deprecated $metadata$ is used for internal purposes, please don't use it in your code, because it can be removed at any moment */
        namespace $metadata$ {
            const constructor: abstract new () => JsGeoPosition;
        }
    }
}
export declare namespace io.github.bobcgn.miniapp.export {
    class JsMediaFile {
        private constructor();
        get tempFilePath(): string;
        get sizeBytes(): number;
        get fileType(): Nullable<string>;
        get hostFileType(): string;
        get durationSeconds(): Nullable<number>;
        get width(): Nullable<number>;
        get height(): Nullable<number>;
        get thumbTempFilePath(): Nullable<string>;
    }
    namespace JsMediaFile {
        /** @deprecated $metadata$ is used for internal purposes, please don't use it in your code, because it can be removed at any moment */
        namespace $metadata$ {
            const constructor: abstract new () => JsMediaFile;
        }
    }
}
export declare namespace io.github.bobcgn.miniapp.export {
    class JsNetworkState {
        private constructor();
        get isConnected(): boolean;
        get networkType(): Nullable<string>;
        get hostNetworkType(): string;
    }
    namespace JsNetworkState {
        /** @deprecated $metadata$ is used for internal purposes, please don't use it in your code, because it can be removed at any moment */
        namespace $metadata$ {
            const constructor: abstract new () => JsNetworkState;
        }
    }
}
export declare namespace io.github.bobcgn.miniapp.export {
    class JsTransferProgress {
        private constructor();
        get percent(): number;
        get bytesTransferred(): Nullable<number>;
        get bytesExpected(): Nullable<number>;
    }
    namespace JsTransferProgress {
        /** @deprecated $metadata$ is used for internal purposes, please don't use it in your code, because it can be removed at any moment */
        namespace $metadata$ {
            const constructor: abstract new () => JsTransferProgress;
        }
    }
    class JsUploadResult {
        private constructor();
        get statusCode(): number;
        get responseText(): string;
    }
    namespace JsUploadResult {
        /** @deprecated $metadata$ is used for internal purposes, please don't use it in your code, because it can be removed at any moment */
        namespace $metadata$ {
            const constructor: abstract new () => JsUploadResult;
        }
    }
    class JsDownloadResult {
        private constructor();
        get statusCode(): number;
        get tempFilePath(): string;
        get filePath(): Nullable<string>;
    }
    namespace JsDownloadResult {
        /** @deprecated $metadata$ is used for internal purposes, please don't use it in your code, because it can be removed at any moment */
        namespace $metadata$ {
            const constructor: abstract new () => JsDownloadResult;
        }
    }
    class JsUploadTransfer {
        private constructor();
        get abortable(): boolean;
        result(): Promise<io.github.bobcgn.miniapp.export.JsUploadResult>;
        abort(): boolean;
        progress(): Nullable<io.github.bobcgn.miniapp.export.JsTransferProgress>;
    }
    namespace JsUploadTransfer {
        /** @deprecated $metadata$ is used for internal purposes, please don't use it in your code, because it can be removed at any moment */
        namespace $metadata$ {
            const constructor: abstract new () => JsUploadTransfer;
        }
    }
    class JsDownloadTransfer {
        private constructor();
        get abortable(): boolean;
        result(): Promise<io.github.bobcgn.miniapp.export.JsDownloadResult>;
        abort(): boolean;
        progress(): Nullable<io.github.bobcgn.miniapp.export.JsTransferProgress>;
    }
    namespace JsDownloadTransfer {
        /** @deprecated $metadata$ is used for internal purposes, please don't use it in your code, because it can be removed at any moment */
        namespace $metadata$ {
            const constructor: abstract new () => JsDownloadTransfer;
        }
    }
}
export declare namespace io.github.bobcgn.miniapp.export {
    class JsPrivacyStatus {
        private constructor();
        get requirement(): string;
        get contractName(): Nullable<string>;
    }
    namespace JsPrivacyStatus {
        /** @deprecated $metadata$ is used for internal purposes, please don't use it in your code, because it can be removed at any moment */
        namespace $metadata$ {
            const constructor: abstract new () => JsPrivacyStatus;
        }
    }
}
export declare namespace io.github.bobcgn.miniapp.export {
    class JsRuntimeInfo {
        private constructor();
        get baseLibraryVersion(): Nullable<string>;
        get platform(): Nullable<string>;
        get isDeveloperTools(): boolean;
    }
    namespace JsRuntimeInfo {
        /** @deprecated $metadata$ is used for internal purposes, please don't use it in your code, because it can be removed at any moment */
        namespace $metadata$ {
            const constructor: abstract new () => JsRuntimeInfo;
        }
    }
}
export declare namespace io.github.bobcgn.miniapp.export {
    class JsScanResult {
        private constructor();
        get text(): string;
        get scanType(): Nullable<string>;
        get format(): Nullable<string>;
        get charSet(): Nullable<string>;
        get rawData(): Nullable<string>;
        get path(): Nullable<string>;
    }
    namespace JsScanResult {
        /** @deprecated $metadata$ is used for internal purposes, please don't use it in your code, because it can be removed at any moment */
        namespace $metadata$ {
            const constructor: abstract new () => JsScanResult;
        }
    }
}
export declare namespace io.github.bobcgn.miniapp.export {
    class JsSubscriptionResult {
        private constructor();
        get templateId(): string;
        get status(): Nullable<string>;
        get hostStatus(): string;
    }
    namespace JsSubscriptionResult {
        /** @deprecated $metadata$ is used for internal purposes, please don't use it in your code, because it can be removed at any moment */
        namespace $metadata$ {
            const constructor: abstract new () => JsSubscriptionResult;
        }
    }
}
export declare namespace io.github.bobcgn.miniapp.export {
    abstract class MiniAppExports extends KtSingleton<MiniAppExports.$metadata$.constructor>() {
        private constructor();
    }
    namespace MiniAppExports {
        /** @deprecated $metadata$ is used for internal purposes, please don't use it in your code, because it can be removed at any moment */
        namespace $metadata$ {
            abstract class constructor {
                sdkVersion(): string;
                storageGet(key: string): Promise<Nullable<string>>;
                storageSet(key: string, value: string): Promise<void>;
                storageRemove(key: string): Promise<void>;
                wechatLogin(): Promise<io.github.bobcgn.miniapp.host.wechat.WeChatLoginResult>;
                wechatCheckSession(): Promise<string>;
                networkRequest(url: string, method: string, headers: Array<string>, body: Nullable<string>, timeoutMillis: Nullable<number>): Promise<io.github.bobcgn.miniapp.export.MiniAppHttpResult>;
                wechatAppOnLaunch(): void;
                wechatAppOnShow(): void;
                wechatAppOnHide(): void;
                wechatAppLifecycleState(): string;
                wechatPageOnShow(route: string): void;
                wechatPageOnHide(): void;
                wechatPageOnUnload(route: string): void;
                wechatPageRoute(): Nullable<string>;
                wechatNavigateTo(url: string): Promise<void>;
                wechatRedirectTo(url: string): Promise<void>;
                wechatNavigateBack(delta: Nullable<number>): Promise<void>;
                capabilitySupport(capability: string): io.github.bobcgn.miniapp.export.JsCapabilitySupport;
                requireCapability(capability: string): void;
                wechatRuntimeInfo(): io.github.bobcgn.miniapp.export.JsRuntimeInfo;
                wechatCanIUse(schema: string): boolean;
                permissionState(permission: string): Promise<string>;
                requestPermission(permission: string): Promise<string>;
                openPermissionSettings(permission: string): Promise<string>;
                privacyStatus(): Promise<io.github.bobcgn.miniapp.export.JsPrivacyStatus>;
                requestPrivacyAuthorization(): Promise<string>;
                wechatGetClipboardText(): Promise<string>;
                wechatSetClipboardText(value: string): Promise<void>;
                wechatVibrateShort(): Promise<void>;
                wechatVibrateLong(): Promise<void>;
                wechatUserDataPath(): string;
                wechatReadTextFile(path: string): Promise<string>;
                wechatWriteTextFile(path: string, content: string): Promise<void>;
                wechatFileExists(path: string): Promise<boolean>;
                wechatRemoveFile(path: string): Promise<void>;
                wechatGetCurrentLocation(coordinateSystem: string): Promise<io.github.bobcgn.miniapp.export.JsGeoPosition>;
                wechatScanCode(onlyFromCamera: boolean, scanTypes: Array<string>): Promise<io.github.bobcgn.miniapp.export.JsScanResult>;
                wechatChooseMedia(mediaTypes: Array<string>, count: number, sourceTypes: Array<string>, maxDurationSeconds: Nullable<number>, sizeTypes: Array<string>, camera: Nullable<string>): Promise<Array<io.github.bobcgn.miniapp.export.JsMediaFile>>;
                wechatRequestSubscribeMessage(templateIds: Array<string>): Promise<Array<io.github.bobcgn.miniapp.export.JsSubscriptionResult>>;
                networkStatus(): Promise<io.github.bobcgn.miniapp.export.JsNetworkState>;
                startNetworkStatusObservation(): void;
                stopNetworkStatusObservation(): Promise<Array<io.github.bobcgn.miniapp.export.JsNetworkState>>;
                networkStatusObservationFailure(): Nullable<string>;
                wechatUploadFile(url: string, filePath: string, name: string, headers: Array<string>, formData: Array<string>, timeoutMillis: Nullable<number>): io.github.bobcgn.miniapp.export.JsUploadTransfer;
                wechatDownloadFile(url: string, headers: Array<string>, timeoutMillis: Nullable<number>, filePath: Nullable<string>): io.github.bobcgn.miniapp.export.JsDownloadTransfer;
                requirePrivacySatisfied(): Promise<void>;
                private constructor();
            }
        }
    }
}
export declare namespace io.github.bobcgn.miniapp.export {
    class MiniAppHttpResult {
        private constructor();
        get statusCode(): number;
        get headers(): Array<string>;
        get body(): string;
    }
    namespace MiniAppHttpResult {
        /** @deprecated $metadata$ is used for internal purposes, please don't use it in your code, because it can be removed at any moment */
        namespace $metadata$ {
            const constructor: abstract new () => MiniAppHttpResult;
        }
    }
}
export declare namespace io.github.bobcgn.miniapp.host.wechat {
    class WeChatLoginResult {
        private constructor();
        get code(): string;
    }
    namespace WeChatLoginResult {
        /** @deprecated $metadata$ is used for internal purposes, please don't use it in your code, because it can be removed at any moment */
        namespace $metadata$ {
            const constructor: abstract new () => WeChatLoginResult;
        }
    }
}