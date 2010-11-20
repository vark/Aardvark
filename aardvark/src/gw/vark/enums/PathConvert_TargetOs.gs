package gw.vark.enums

enum PathConvert_TargetOs{

  Windows("windows"),
  Unix("unix"),
  Netware("netware"),
  Os_2("os/2"),
  Tandem("tandem"),

  var _val : String as Val

  private construct( s : String ) { Val = s }


}