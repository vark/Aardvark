package gw.vark.enums

enum MSVSS_WritableFiles{

  Replace("replace"),
  Skip("skip"),
  Fail("fail"),

  var _val : String as Val

  private construct( s : String ) { Val = s }


}