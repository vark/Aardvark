package gw.vark.enums

enum JDependTask_FormatAttribute{

  Xml("xml"),
  Text("text"),

  property get Instance() : org.apache.tools.ant.taskdefs.optional.jdepend.JDependTask.FormatAttribute {
    return org.apache.tools.ant.types.EnumeratedAttribute.getInstance(org.apache.tools.ant.taskdefs.optional.jdepend.JDependTask.FormatAttribute, Val) as org.apache.tools.ant.taskdefs.optional.jdepend.JDependTask.FormatAttribute
  }

  var _val : String as Val

  private construct( s : String ) { Val = s }

}
