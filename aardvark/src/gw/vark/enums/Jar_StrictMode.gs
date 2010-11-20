package gw.vark.enums

enum Jar_StrictMode{

  Fail("fail"),
  Warn("warn"),
  Ignore("ignore"),

  var _val : String as Val

  private construct( s : String ) { Val = s }


}