package gw.vark.enums

enum Javadoc_AccessType{

  Protected("protected"),
  Public("public"),
  Package("package"),
  Private("private"),

  property get Instance() : org.apache.tools.ant.taskdefs.Javadoc.AccessType {
    return org.apache.tools.ant.types.EnumeratedAttribute.getInstance(org.apache.tools.ant.taskdefs.Javadoc.AccessType, Val) as org.apache.tools.ant.taskdefs.Javadoc.AccessType
  }

  var _val : String as Val

  private construct( s : String ) { Val = s }

}
