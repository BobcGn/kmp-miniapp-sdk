interface MiniProgramAppOptions {}

interface MiniProgramPageOptions<TData extends object> {
  data: TData;
}

declare function App(options: MiniProgramAppOptions): void;

declare function Page<TData extends object>(
  options: MiniProgramPageOptions<TData>,
): void;

declare const console: {
  log(message?: unknown, ...optionalParameters: unknown[]): void;
};
