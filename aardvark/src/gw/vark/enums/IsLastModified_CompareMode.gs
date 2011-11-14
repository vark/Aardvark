package gw.vark.enums

enum IsLastModified_CompareMode{

  Equals("equals"),
  Before("before"),
  After("after"),
  Not_before("not-before"),
  Not_after("not-after"),

  property get Instance() : org.apache.tools.ant.taskdefs.condition.IsLastModified.CompareMode {
    return org.apache.tools.ant.types.EnumeratedAttribute.getInstance(org.apache.tools.ant.taskdefs.condition.IsLastModified.CompareMode, Val) as org.apache.tools.ant.taskdefs.condition.IsLastModified.CompareMode
  }

  var _val : String as Val

  private construct( s : String ) { Val = s }

}
