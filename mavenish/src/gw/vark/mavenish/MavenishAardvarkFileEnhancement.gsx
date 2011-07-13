package gw.vark.mavenish

uses gw.vark.*
uses java.io.File

enhancement MavenishAardvarkFileEnhancement : AardvarkFile {

  static function loadPom( pomFile : File ) {
    PomHelper.load( pomFile )
  }

}
