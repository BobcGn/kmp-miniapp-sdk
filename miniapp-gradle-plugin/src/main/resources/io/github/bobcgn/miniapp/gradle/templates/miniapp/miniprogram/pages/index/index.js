// The thin host page. The SDK shares behaviour and the host renders, so this file holds no business
// logic: it loads the compiled Kotlin module and reports what it exported. Replace it with the page
// that calls your own `miniappMain` exports.
//
// `initMiniApp` wrote the module path below from the plugin's default Kotlin/JS output module name.
// This page is yours, and neither `syncMiniApp` nor `assembleMiniApp` ever rewrites it, so if your
// build sets a different `outputModuleName`, change the path here to match.
//
// Until `./gradlew syncMiniApp` has run there is no module to load, which is what the failure row
// reports.
Page({
  data: {
    modulePath: '__MINIAPP_MODULE__.js',
    exports: '',
    failure: ''
  },

  onLoad() {
    try {
      /** @type {typeof import('../../libs/__MINIAPP_MODULE__.js')} */
      const miniAppModule = require('../../libs/__MINIAPP_MODULE__.js');
      const exported = Object.keys(miniAppModule).sort().join(',');
      this.setData({ exports: exported });
      console.log('miniapp.module=__MINIAPP_MODULE__.js');
      console.log('miniapp.exports=' + exported);
      console.log('miniapp.result=PASS');
    } catch (error) {
      this.setData({ failure: String(error) });
      console.log('miniapp.failure=' + String(error));
      console.log('miniapp.result=FAIL');
    }
  }
});
