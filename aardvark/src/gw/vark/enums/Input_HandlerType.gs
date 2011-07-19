package gw.vark.enums

enum Input_HandlerType{

  Default("default"),
  Propertyfile("propertyfile"),
  Greedy("greedy"),
  Secure("secure"),

  property get Instance() : org.apache.tools.ant.taskdefs.Input.HandlerType {
    return org.apache.tools.ant.types.EnumeratedAttribute.getInstance(org.apache.tools.ant.taskdefs.Input.HandlerType, Val) as org.apache.tools.ant.taskdefs.Input.HandlerType
  }

  var _val : String as Val

  private construct( s : String ) { Val = s }

}
