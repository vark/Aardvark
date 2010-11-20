package gw.vark.enums

enum Zip_Duplicate{

  Add("add"),
  Preserve("preserve"),
  Fail("fail"),

  var _val : String as Val

  private construct( s : String ) { Val = s }


}