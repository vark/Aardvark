package gw.vark.enums

enum Zip_WhenEmpty{

  Fail("fail"),
  Skip("skip"),
  Create("create"),

  var _val : String as Val

  private construct( s : String ) { Val = s }


}