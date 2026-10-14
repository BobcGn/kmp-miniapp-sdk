// Kotlin owns the greeting logic and host inspection; WXML/WXSS own rendering.
const core = require('../../libs/kmp-demo-with-server-core-miniapp.js');

Page({
  data: { greeting: '', failure: '' },

  onLoad() {
    try {
      const greeting = core.org.example.kmp_demo_with_server.platformGreeting();
      this.setData({ greeting, failure: '' });
      console.log('demo.greeting=' + greeting);
      console.log('demo.result=PASS');
    } catch (error) {
      this.setData({ greeting: '', failure: '暂时无法读取平台信息，请重新打开小程序。' });
      console.error('demo.result=FAIL', error);
    }
  }
});
