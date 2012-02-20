package gw.vark.enums

enum Length_FileMode{

  Each("each"),
  All("all"),

  property get Instance() : org.apache.tools.ant.taskdefs.Length.FileMode {
    return org.apache.tools.ant.types.EnumeratedAttribute.getInstance(org.apache.tools.ant.taskdefs.Length.FileMode, Val) as org.apache.tools.ant.taskdefs.Length.FileMode
  }

  var _val : String as Val

  private construct( s : String ) { Val = s }

}
