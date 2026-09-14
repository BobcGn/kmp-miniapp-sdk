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
                private constructor();
            }
        }
    }
}