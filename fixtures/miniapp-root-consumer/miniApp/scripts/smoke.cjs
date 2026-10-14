// Node checks module wiring only; actual WeChat results are recorded separately.
const assert = require('node:assert/strict');
const data = {};
global.Page = (page) => page.onLoad.call({ setData: (values) => Object.assign(data, values) });
require('../miniprogram/pages/index/index.js');
assert.equal(data.failure, undefined);
assert.match(data.greeting, /^Hello, WeChat · .+ · 基础库 .+!$/);
console.log('rootHost.nodeWiring=PASS');
