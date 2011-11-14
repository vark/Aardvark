package gw.vark.enums

enum Type_FileDir{

  File("file"),
  Dir("dir"),
  Any("any"),

  property get Instance() : org.apache.tools.ant.types.resources.selectors.Type.FileDir {
    return org.apache.tools.ant.types.EnumeratedAttribute.getInstance(org.apache.tools.ant.types.resources.selectors.Type.FileDir, Val) as org.apache.tools.ant.types.resources.selectors.Type.FileDir
  }

  var _val : String as Val

  private construct( s : String ) { Val = s }

}
