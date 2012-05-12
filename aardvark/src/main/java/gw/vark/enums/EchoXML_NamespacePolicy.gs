package gw.vark.enums

enum EchoXML_NamespacePolicy{

  Ignore("ignore"),
  ElementsOnly("elementsOnly"),
  All("all"),

  property get Instance() : org.apache.tools.ant.taskdefs.EchoXML.NamespacePolicy {
    return org.apache.tools.ant.types.EnumeratedAttribute.getInstance(org.apache.tools.ant.taskdefs.EchoXML.NamespacePolicy, Val) as org.apache.tools.ant.taskdefs.EchoXML.NamespacePolicy
  }

  var _val : String as Val

  private construct( s : String ) { Val = s }

}
