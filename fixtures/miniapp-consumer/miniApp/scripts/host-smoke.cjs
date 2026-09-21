// Loads the assembled bundle exactly the way `miniprogram/pages/index/index.js` does, so the host's
// consumption path is checked without opening WeChat Developer Tools. This is not host acceptance:
// it runs on Node, not on the WeChat Mini Program runtime.
//
// The script lives inside the Mini App project, so `assembleMiniApp` copies it into the distribution
// along with everything else, and it resolves only relative to that project. Running the copy that
// sits in `build/miniapp-dist` is what shows the assembled distribution needs nothing from the
// repository it was built in.
const path = require('path');

const projectRoot = process.argv[2] ? path.resolve(process.argv[2]) : path.join(__dirname, '..');
const entry = path.join(projectRoot, 'miniprogram', 'libs', 'miniapp-consumer-miniapp.js');
const consumer = require(entry).consumer;

const greeting = consumer.hostGreeting('WeChat');
const counted = consumer.countUpTo(5);
const version = consumer.sdkVersion();

console.log('fixture.projectRoot=' + projectRoot);
console.log('fixture.greeting=' + greeting);
console.log('fixture.counted=' + counted);
console.log('fixture.sdkVersion=' + version);

const expected = greeting === 'Hello, WeChat, from commonMain' && counted === 5 && version.length > 0;
console.log('fixture.result=' + (expected ? 'PASS' : 'FAIL'));
process.exit(expected ? 0 : 1);
