package gw.vark.aether

uses gw.vark.IAardvarkUtils

enhancement AetherIAardvarkUtilsEnhancement : IAardvarkUtils {
  static function pom( pomFile : String = "pom.xml" ) : PomHelper {
    return PomHelper.load( IAardvarkUtils.file( pomFile ) )
  }
}
