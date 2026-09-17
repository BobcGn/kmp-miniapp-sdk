// The thin host: it calls the assembled bundle and renders what comes back. It contains no Kotlin
// and no business logic — the shared behaviour lives in the consumer's `commonMain`, the host-facing
// entry points in its `miniappMain`, and the runtime SDK is what those entry points call.
const consumer = require('../../libs/miniapp-consumer-miniapp.js').consumer;

Page({
  data: {
    greeting: '',
    counted: '',
    version: '',
    failure: ''
  },

  onLoad() {
    try {
      const greeting = consumer.hostGreeting('WeChat');
      const counted = consumer.countUpTo(5);
      const version = consumer.sdkVersion();

      this.setData({ greeting, counted: String(counted), version });

      console.log('fixture.greeting=' + greeting);
      console.log('fixture.counted=' + counted);
      console.log('fixture.sdkVersion=' + version);
      console.log('fixture.result=PASS');
    } catch (error) {
      this.setData({ failure: String(error) });
      console.log('fixture.failure=' + String(error));
      console.log('fixture.result=FAIL');
    }
  }
});
