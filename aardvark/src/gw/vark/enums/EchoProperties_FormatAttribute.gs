package gw.vark.enums

enum EchoProperties_FormatAttribute{

  Xml("xml"),
  Text("text"),

  property get Instance() : org.apache.tools.ant.taskdefs.optional.EchoProperties.FormatAttribute {
    return org.apache.tools.ant.types.EnumeratedAttribute.getInstance(org.apache.tools.ant.taskdefs.optional.EchoProperties.FormatAttribute, Val) as org.apache.tools.ant.taskdefs.optional.EchoProperties.FormatAttribute
  }

  var _val : String as Val

  private construct( s : String ) { Val = s }

}
