package gw.vark

uses java.io.File

enhancement CoreAardvarkFileEnhancement : AardvarkFile {

  static function loadPom( pomFile : File ) {
    Pom.load( pomFile )
  }

}
