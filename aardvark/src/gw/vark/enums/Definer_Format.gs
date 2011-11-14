package gw.vark.enums

enum Definer_Format{

  Properties("properties"),
  Xml("xml"),

  property get Instance() : org.apache.tools.ant.taskdefs.Definer.Format {
    return org.apache.tools.ant.types.EnumeratedAttribute.getInstance(org.apache.tools.ant.taskdefs.Definer.Format, Val) as org.apache.tools.ant.taskdefs.Definer.Format
  }

  var _val : String as Val

  private construct( s : String ) { Val = s }

}
