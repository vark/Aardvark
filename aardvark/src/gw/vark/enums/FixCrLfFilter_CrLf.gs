package gw.vark.enums

enum FixCrLfFilter_CrLf{

  Asis("asis"),
  Cr("cr"),
  Lf("lf"),
  Crlf("crlf"),
  Mac("mac"),
  Unix("unix"),
  Dos("dos"),

  var _val : String as Val

  private construct( s : String ) { Val = s }


}