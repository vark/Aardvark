package gw.vark.enums

enum Echo_EchoLevel{

  Error("error"),
  Warn("warn"),
  Warning("warning"),
  Info("info"),
  Verbose("verbose"),
  Debug("debug"),

  property get Instance() : org.apache.tools.ant.taskdefs.Echo.EchoLevel {
    return org.apache.tools.ant.types.EnumeratedAttribute.getInstance(org.apache.tools.ant.taskdefs.Echo.EchoLevel, Val) as org.apache.tools.ant.taskdefs.Echo.EchoLevel
  }

  var _val : String as Val

  private construct( s : String ) { Val = s }

}
