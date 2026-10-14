// Node verifies actual bundle/page wiring with isolated mock hosts, not real WeChat acceptance.
const assert = require('node:assert/strict');
const { execFileSync } = require('node:child_process');
const scenario = process.argv[2];
const cases = {
  devtools: ['devtools', '3.17.4'],
  android: ['android', '3.18.0'],
  missing: ['未知', '未知']
};
if (!scenario) {
  for (const name of Object.keys(cases)) {
    process.stdout.write(execFileSync(process.execPath, [__filename, name]));
  }
  console.log('demo.nodeWiring=PASS (mock host; not Developer Tools acceptance)');
} else {
  const [platform, version] = cases[scenario];
  if (scenario !== 'missing') {
    global.wx = {
      getAppBaseInfo: () => ({ SDKVersion: version }),
      getDeviceInfo: () => ({ platform })
    };
  }
  let page;
  global.Page = (definition) => { page = definition; };
  require('../miniprogram/pages/index/index.js');
  const data = { ...page.data };
  page.onLoad.call({ setData: (values) => Object.assign(data, values) });
  assert.equal(data.failure, '');
  assert.equal(data.greeting, `Hello, WeChat · ${platform} · 基础库 ${version}!`);
  console.log(`demo.mockHost.${scenario}=PASS`);
}
