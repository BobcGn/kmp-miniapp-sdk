(function (_, kotlin_kotlin) {
  'use strict';
  //region block: imports
  var protoOf = kotlin_kotlin.$_$.d;
  var initMetadataForObject = kotlin_kotlin.$_$.c;
  var defineProp = kotlin_kotlin.$_$.b;
  var VOID = kotlin_kotlin.$_$.a;
  //endregion
  //region block: pre-declaration
  initMetadataForObject(MiniAppExports, 'MiniAppExports');
  //endregion
  function MiniAppExports() {
  }
  protoOf(MiniAppExports).sdkVersion = function () {
    return '0.1.0-SNAPSHOT';
  };
  var MiniAppExports_instance;
  function MiniAppExports_getInstance() {
    return MiniAppExports_instance;
  }
  //region block: init
  MiniAppExports_instance = new MiniAppExports();
  //endregion
  //region block: exports
  function $jsExportAll$(_) {
    var io = _.io || (_.io = {});
    var github = io.github || (io.github = {});
    var bobcgn = github.bobcgn || (github.bobcgn = {});
    var miniapp = bobcgn.miniapp || (bobcgn.miniapp = {});
    var export_0 = miniapp.export || (miniapp.export = {});
    defineProp(export_0, 'MiniAppExports', MiniAppExports_getInstance, VOID, true);
  }
  $jsExportAll$(_);
  //endregion
  return _;
}(module.exports, require('./kotlin-kotlin-stdlib.js')));

//# sourceMappingURL=kmp-miniapp-sdk-sdk.js.map
