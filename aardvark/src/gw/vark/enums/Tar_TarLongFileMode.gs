package gw.vark.enums

enum Tar_TarLongFileMode{

  Warn("warn"),
  Fail("fail"),
  Truncate("truncate"),
  Gnu("gnu"),
  Omit("omit"),

  var _val : String as Val

  private construct( s : String ) { Val = s }


}