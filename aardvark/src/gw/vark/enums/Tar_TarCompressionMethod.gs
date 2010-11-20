package gw.vark.enums

enum Tar_TarCompressionMethod{

  None("none"),
  Gzip("gzip"),
  Bzip2("bzip2"),

  var _val : String as Val

  private construct( s : String ) { Val = s }


}