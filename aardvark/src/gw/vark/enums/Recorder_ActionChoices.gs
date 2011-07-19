package gw.vark.enums

enum Recorder_ActionChoices{

  Start("start"),
  Stop("stop"),

  property get Instance() : org.apache.tools.ant.taskdefs.Recorder.ActionChoices {
    return org.apache.tools.ant.types.EnumeratedAttribute.getInstance(org.apache.tools.ant.taskdefs.Recorder.ActionChoices, Val) as org.apache.tools.ant.taskdefs.Recorder.ActionChoices
  }

  var _val : String as Val

  private construct( s : String ) { Val = s }

}
