package gw.vark

uses java.io.File

enhancement CoreAardvarkFileEnhancement : AardvarkFile {

  static function loadPom( pomFile : File ) {
    PomHelper.load( pomFile )
  }

}
