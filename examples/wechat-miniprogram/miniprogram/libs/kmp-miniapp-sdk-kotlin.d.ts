type Nullable<T> = T | null | undefined
declare function KtSingleton<T>(): T & (abstract new() => any);
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