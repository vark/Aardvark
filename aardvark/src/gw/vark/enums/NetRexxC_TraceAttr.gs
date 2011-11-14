package gw.vark.enums

enum NetRexxC_TraceAttr{

  Trace("trace"),
  Trace1("trace1"),
  Trace2("trace2"),
  Notrace("notrace"),

  property get Instance() : org.apache.tools.ant.taskdefs.optional.NetRexxC.TraceAttr {
    return org.apache.tools.ant.types.EnumeratedAttribute.getInstance(org.apache.tools.ant.taskdefs.optional.NetRexxC.TraceAttr, Val) as org.apache.tools.ant.taskdefs.optional.NetRexxC.TraceAttr
  }

  var _val : String as Val

  private construct( s : String ) { Val = s }

}
