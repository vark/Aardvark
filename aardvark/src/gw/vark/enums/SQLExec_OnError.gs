package gw.vark.enums

enum SQLExec_OnError{

  Continue("continue"),
  Stop("stop"),
  Abort("abort"),

  property get Instance() : org.apache.tools.ant.taskdefs.SQLExec.OnError {
    return org.apache.tools.ant.types.EnumeratedAttribute.getInstance(org.apache.tools.ant.taskdefs.SQLExec.OnError, Val) as org.apache.tools.ant.taskdefs.SQLExec.OnError
  }

  var _val : String as Val

  private construct( s : String ) { Val = s }

}
