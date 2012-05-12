package gw.vark.enums

enum Definer_OnError{

  Fail("fail"),
  Report("report"),
  Ignore("ignore"),
  Failall("failall"),

  property get Instance() : org.apache.tools.ant.taskdefs.Definer.OnError {
    return org.apache.tools.ant.types.EnumeratedAttribute.getInstance(org.apache.tools.ant.taskdefs.Definer.OnError, Val) as org.apache.tools.ant.taskdefs.Definer.OnError
  }

  var _val : String as Val

  private construct( s : String ) { Val = s }

}
