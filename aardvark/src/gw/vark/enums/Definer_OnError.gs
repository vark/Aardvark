package gw.vark.enums

enum Definer_OnError{

  Fail("fail"),
  Report("report"),
  Ignore("ignore"),
  Failall("failall"),

  var _val : String as Val

  private construct( s : String ) { Val = s }


}