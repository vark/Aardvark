package gw.vark.enums

enum PathConvert_TargetOs{

  Windows("windows"),
  Unix("unix"),
  Netware("netware"),
  Os_2("os/2"),
  Tandem("tandem"),

  property get Instance() : org.apache.tools.ant.taskdefs.PathConvert.TargetOs {
    return org.apache.tools.ant.types.EnumeratedAttribute.getInstance(org.apache.tools.ant.taskdefs.PathConvert.TargetOs, Val) as org.apache.tools.ant.taskdefs.PathConvert.TargetOs
  }

  var _val : String as Val

  private construct( s : String ) { Val = s }

}
