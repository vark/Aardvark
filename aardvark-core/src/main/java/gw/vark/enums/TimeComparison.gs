package gw.vark.enums

enum TimeComparison{

  Before("before"),
  After("after"),
  Equal("equal"),

  property get Instance() : org.apache.tools.ant.types.TimeComparison {
    return org.apache.tools.ant.types.EnumeratedAttribute.getInstance(org.apache.tools.ant.types.TimeComparison, Val) as org.apache.tools.ant.types.TimeComparison
  }

  var _val : String as Val

  private construct( s : String ) { Val = s }

}
