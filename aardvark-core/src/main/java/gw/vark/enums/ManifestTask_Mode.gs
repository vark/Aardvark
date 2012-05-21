package gw.vark.enums

enum ManifestTask_Mode{

  Update("update"),
  Replace("replace"),

  property get Instance() : org.apache.tools.ant.taskdefs.ManifestTask.Mode {
    return org.apache.tools.ant.types.EnumeratedAttribute.getInstance(org.apache.tools.ant.taskdefs.ManifestTask.Mode, Val) as org.apache.tools.ant.taskdefs.ManifestTask.Mode
  }

  var _val : String as Val

  private construct( s : String ) { Val = s }

}
