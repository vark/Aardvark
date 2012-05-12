package gw.vark.enums

enum Recorder_VerbosityLevelChoices{

  Error("error"),
  Warn("warn"),
  Warning("warning"),
  Info("info"),
  Verbose("verbose"),
  Debug("debug"),

  property get Instance() : org.apache.tools.ant.taskdefs.Recorder.VerbosityLevelChoices {
    return org.apache.tools.ant.types.EnumeratedAttribute.getInstance(org.apache.tools.ant.taskdefs.Recorder.VerbosityLevelChoices, Val) as org.apache.tools.ant.taskdefs.Recorder.VerbosityLevelChoices
  }

  var _val : String as Val

  private construct( s : String ) { Val = s }

}
