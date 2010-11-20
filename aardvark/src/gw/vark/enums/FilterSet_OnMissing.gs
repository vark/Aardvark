package gw.vark.enums

enum FilterSet_OnMissing{

  Fail("fail"),
  Warn("warn"),
  Ignore("ignore"),

  var _val : String as Val

  private construct( s : String ) { Val = s }


}