package gw.vark.enums

enum PropertySet_BuiltinPropertySetName{

  All("all"),
  System("system"),
  Commandline("commandline"),

  property get Instance() : org.apache.tools.ant.types.PropertySet.BuiltinPropertySetName {
    return org.apache.tools.ant.types.EnumeratedAttribute.getInstance(org.apache.tools.ant.types.PropertySet.BuiltinPropertySetName, Val) as org.apache.tools.ant.types.PropertySet.BuiltinPropertySetName
  }

  var _val : String as Val

  private construct( s : String ) { Val = s }

}
