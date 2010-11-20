package gw.vark.enums

enum Checksum_FormatElement{

  CHECKSUM("CHECKSUM"),
  MD5SUM("MD5SUM"),
  SVF("SVF"),

  var _val : String as Val

  private construct( s : String ) { Val = s }


}