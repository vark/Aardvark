package gw.vark.enums

enum Available_FileDir{

  File("file"),
  Dir("dir"),

  property get Instance() : org.apache.tools.ant.taskdefs.Available.FileDir {
    return org.apache.tools.ant.types.EnumeratedAttribute.getInstance(org.apache.tools.ant.taskdefs.Available.FileDir, Val) as org.apache.tools.ant.taskdefs.Available.FileDir
  }

  var _val : String as Val

  private construct( s : String ) { Val = s }

}
