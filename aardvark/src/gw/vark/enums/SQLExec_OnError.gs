package gw.vark.enums

enum SQLExec_OnError{

  Continue("continue"),
  Stop("stop"),
  Abort("abort"),

  var _val : String as Val

  private construct( s : String ) { Val = s }


}