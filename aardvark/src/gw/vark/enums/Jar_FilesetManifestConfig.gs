package gw.vark.enums

enum Jar_FilesetManifestConfig{

  Skip("skip"),
  Merge("merge"),
  Mergewithoutmain("mergewithoutmain"),

  var _val : String as Val

  private construct( s : String ) { Val = s }


}