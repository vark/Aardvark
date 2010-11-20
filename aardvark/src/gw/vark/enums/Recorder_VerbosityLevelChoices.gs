package gw.vark.enums

enum Recorder_VerbosityLevelChoices{

  Error("error"),
  Warn("warn"),
  Warning("warning"),
  Info("info"),
  Verbose("verbose"),
  Debug("debug"),

  var _val : String as Val

  private construct( s : String ) { Val = s }


}