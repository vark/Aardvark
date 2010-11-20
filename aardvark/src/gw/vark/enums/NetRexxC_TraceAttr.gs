package gw.vark.enums

enum NetRexxC_TraceAttr{

  Trace("trace"),
  Trace1("trace1"),
  Trace2("trace2"),
  Notrace("notrace"),

  var _val : String as Val

  private construct( s : String ) { Val = s }


}