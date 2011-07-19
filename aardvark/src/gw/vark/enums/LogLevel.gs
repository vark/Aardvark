package gw.vark.enums

enum LogLevel{

  Error("error"),
  Warn("warn"),
  Warning("warning"),
  Info("info"),
  Verbose("verbose"),
  Debug("debug"),

  property get Instance() : org.apache.tools.ant.types.LogLevel {
    return org.apache.tools.ant.types.EnumeratedAttribute.getInstance(org.apache.tools.ant.types.LogLevel, Val) as org.apache.tools.ant.types.LogLevel
  }

  var _val : String as Val

  private construct( s : String ) { Val = s }

}
