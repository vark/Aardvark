package gw.vark.enums

enum Jar_StrictMode{

  Fail("fail"),
  Warn("warn"),
  Ignore("ignore"),

  property get Instance() : org.apache.tools.ant.taskdefs.Jar.StrictMode {
    return org.apache.tools.ant.types.EnumeratedAttribute.getInstance(org.apache.tools.ant.taskdefs.Jar.StrictMode, Val) as org.apache.tools.ant.taskdefs.Jar.StrictMode
  }

  var _val : String as Val

  private construct( s : String ) { Val = s }

}
