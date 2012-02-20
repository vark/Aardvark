package gw.vark.enums

enum FormatterElement_TypeAttribute{

  Plain("plain"),
  Xml("xml"),
  Brief("brief"),
  Failure("failure"),

  property get Instance() : org.apache.tools.ant.taskdefs.optional.junit.FormatterElement.TypeAttribute {
    return org.apache.tools.ant.types.EnumeratedAttribute.getInstance(org.apache.tools.ant.taskdefs.optional.junit.FormatterElement.TypeAttribute, Val) as org.apache.tools.ant.taskdefs.optional.junit.FormatterElement.TypeAttribute
  }

  var _val : String as Val

  private construct( s : String ) { Val = s }

}
