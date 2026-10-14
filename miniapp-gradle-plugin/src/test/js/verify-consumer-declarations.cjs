// Read the real consumer and compiler declarations; virtual probes never write host source.
// Language-service evidence is not evidence of an IDE click or WeChat execution.
const assert = require('node:assert/strict');
const path = require('node:path');
const fs = require('node:fs');
const [hostArgument, typescriptArgument] = process.argv.slice(2);
assert.ok(hostArgument && typescriptArgument, 'Usage: node verify-consumer-declarations.cjs <miniprogram directory> <typescript package>');
const hostRoot = path.resolve(hostArgument);
const ts = require(path.join(path.resolve(typescriptArgument), 'lib/typescript.js'));
const configPath = path.join(hostRoot, 'jsconfig.json');
const config = ts.readConfigFile(configPath, ts.sys.readFile);
assert.equal(config.error, undefined);
const parsed = ts.parseJsonConfigFileContent(config.config, ts.sys, hostRoot);
assert.deepEqual(parsed.errors, []);
const page = path.join(hostRoot, 'pages/index/index.js');
const source = fs.readFileSync(page, 'utf8');
const specifier = source.match(/const core = require\(['"]([^'"]+)['"]\)/)?.[1];
assert.ok(specifier, 'Expected the actual demo bundle require');
const resolved = ts.resolveModuleName(specifier, page, parsed.options, ts.sys).resolvedModule;
assert.ok(resolved, 'Bundle type resolution failed');
assert.equal(resolved.extension, ts.Extension.Dts);
const manifest = JSON.parse(fs.readFileSync(path.join(hostRoot, 'libs/package.json'), 'utf8'));
const declaration = path.join(hostRoot, 'libs', manifest.types);
assert.equal(path.resolve(resolved.resolvedFileName), declaration);
const namespace = source.match(/core\.(org\.[\w.]+)\.platformGreeting\(/)?.[1];
assert.ok(namespace);
const sdk = 'io.github.bobcgn.miniapp.export.MiniAppExports';
const good = `import core = require(${JSON.stringify(specifier)});\nconst greeting: string = core.${namespace}.platformGreeting();\nconst version: string = core.${sdk}.sdkVersion();\nconst info = core.${sdk}.wechatRuntimeInfo();\nconst base: string | null | undefined = info.baseLibraryVersion;\n`;
const bad = `${good}\ncore.${namespace}.platformGreeting(123);\nconst invalidVersion: number = core.${sdk}.sdkVersion();\ncore.${sdk}.doesNotExist();\n`;
const probe = path.join(path.dirname(page), '__declaration_probe__.ts');
const virtual = new Map([[probe, good]]);
const compilerHost = ts.createCompilerHost(parsed.options);
const readFile = compilerHost.readFile.bind(compilerHost);
const fileExists = compilerHost.fileExists.bind(compilerHost);
compilerHost.readFile = f => virtual.get(f) ?? readFile(f);
compilerHost.fileExists = f => virtual.has(f) || fileExists(f);
compilerHost.getSourceFile = (f, version) => {
  const text = compilerHost.readFile(f);
  return text === undefined ? undefined : ts.createSourceFile(f, text, version, true);
};
function diagnostics(text) {
  virtual.set(probe, text);
  return ts.getPreEmitDiagnostics(ts.createProgram([probe], parsed.options, compilerHost));
}
const errors = diagnostics(good);
assert.equal(errors.length, 0, ts.formatDiagnosticsWithColorAndContext(errors, {
  getCanonicalFileName: f => f, getCurrentDirectory: () => hostRoot, getNewLine: () => '\n'
}));
const rejected = diagnostics(bad);
assert.deepEqual(rejected.map(d => d.code).sort(), [2322, 2339, 2554]);
const service = ts.createLanguageService({
  getScriptFileNames: () => parsed.fileNames,
  getScriptVersion: () => '0',
  getScriptSnapshot: f => {
    const text = ts.sys.readFile(f);
    return text === undefined ? undefined : ts.ScriptSnapshot.fromString(text);
  },
  getCurrentDirectory: () => hostRoot,
  getCompilationSettings: () => parsed.options,
  getDefaultLibFileName: options => ts.getDefaultLibFilePath(options),
  fileExists: ts.sys.fileExists, readFile: ts.sys.readFile, readDirectory: ts.sys.readDirectory
});
for (const symbol of ['platformGreeting', 'sdkVersion']) {
  const position = source.indexOf(`${symbol}(`);
  assert.ok(position >= 0, `Actual page must consume ${symbol}`);
  const definitions = service.getDefinitionAtPosition(page, position + 1);
  assert.equal(definitions?.length, 1, `${symbol} must resolve without competing declarations`);
  assert.equal(path.resolve(definitions[0].fileName), declaration);
  assert.equal(definitions[0].name, symbol);
  console.log(`${symbol} -> ${path.relative(hostRoot, declaration)}`);
}
console.log(`declarations=PASS TypeScript ${ts.version}; valid calls accepted, invalid calls rejected; IDE acceptance pending`);
