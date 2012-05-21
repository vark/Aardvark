package gw.vark.enums

enum ExecuteOn_FileDirBoth{

  File("file"),
  Dir("dir"),
  Both("both"),

  property get Instance() : org.apache.tools.ant.taskdefs.ExecuteOn.FileDirBoth {
    return org.apache.tools.ant.types.EnumeratedAttribute.getInstance(org.apache.tools.ant.taskdefs.ExecuteOn.FileDirBoth, Val) as org.apache.tools.ant.taskdefs.ExecuteOn.FileDirBoth
  }

  var _val : String as Val

  private construct( s : String ) { Val = s }

}
