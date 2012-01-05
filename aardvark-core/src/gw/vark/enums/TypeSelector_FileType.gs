package gw.vark.enums

enum TypeSelector_FileType{

  File("file"),
  Dir("dir"),

  property get Instance() : org.apache.tools.ant.types.selectors.TypeSelector.FileType {
    return org.apache.tools.ant.types.EnumeratedAttribute.getInstance(org.apache.tools.ant.types.selectors.TypeSelector.FileType, Val) as org.apache.tools.ant.types.selectors.TypeSelector.FileType
  }

  var _val : String as Val

  private construct( s : String ) { Val = s }

}
