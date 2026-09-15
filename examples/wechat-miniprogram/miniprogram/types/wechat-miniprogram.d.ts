interface MiniProgramAppOptions {
  onLaunch?(): void;
  onShow?(): void;
  onHide?(): void;
}

interface MiniProgramPageOptions<TData extends object> {
  data: TData;
  onLoad?(this: MiniProgramPageInstance<TData>): void;
  onShow?(this: MiniProgramPageInstance<TData>): void;
  onHide?(this: MiniProgramPageInstance<TData>): void;
  onUnload?(this: MiniProgramPageInstance<TData>): void;
  /** Event handlers referenced from WXML, for example `bindtap`. */
  readonly [handler: string]: unknown;
}

interface MiniProgramPageInstance<TData extends object> {
  readonly data: TData;
  /** Route of this page, as declared in app.json. */
  readonly route: string;
  setData(data: Partial<TData>): void;
}

declare function App(options: MiniProgramAppOptions): void;

declare function Page<TData extends object>(
  options: MiniProgramPageOptions<TData>,
): void;

declare const console: {
  log(message?: unknown, ...optionalParameters: unknown[]): void;
  error(message?: unknown, ...optionalParameters: unknown[]): void;
};
