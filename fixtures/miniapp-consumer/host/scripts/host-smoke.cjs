// Loads the assembled bundle exactly the way `pages/index/index.js` does, so the host's consumption
// path is checked without opening WeChat Developer Tools. This is not host acceptance: it runs on
// Node, not on the WeChat Mini Program runtime.
const path = require('path');

const entry = path.join(__dirname, '..', 'miniprogram', 'libs', 'miniapp-consumer-miniapp.js');
const consumer = require(entry).consumer;

const greeting = consumer.hostGreeting('WeChat');
const counted = consumer.countUpTo(5);
const version = consumer.sdkVersion();

console.log('fixture.greeting=' + greeting);
console.log('fixture.counted=' + counted);
console.log('fixture.sdkVersion=' + version);

const expected = greeting === 'Hello, WeChat, from commonMain' && counted === 5 && version.length > 0;
console.log('fixture.result=' + (expected ? 'PASS' : 'FAIL'));
process.exit(expected ? 0 : 1);
