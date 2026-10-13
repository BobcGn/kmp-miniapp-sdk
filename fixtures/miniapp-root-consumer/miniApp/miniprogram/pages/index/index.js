const consumer = require('../../libs/miniapp-root-consumer-shared-miniapp.js').consumer;

Page({
  data: { greeting: '', failure: '' },
  onLoad() {
    try {
      const greeting = consumer.platformGreeting();
      this.setData({ greeting });
      console.log('rootHost.greeting=' + greeting);
      console.log('rootHost.result=PASS');
    } catch (error) {
      this.setData({ failure: String(error) });
      console.log('rootHost.result=FAIL');
    }
  }
});
