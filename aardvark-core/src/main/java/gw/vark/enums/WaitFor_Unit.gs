package gw.vark.enums

enum WaitFor_Unit{

  Millisecond("millisecond"),
  Second("second"),
  Minute("minute"),
  Hour("hour"),
  Day("day"),
  Week("week"),

  property get Instance() : org.apache.tools.ant.taskdefs.WaitFor.Unit {
    return org.apache.tools.ant.types.EnumeratedAttribute.getInstance(org.apache.tools.ant.taskdefs.WaitFor.Unit, Val) as org.apache.tools.ant.taskdefs.WaitFor.Unit
  }

  var _val : String as Val

  private construct( s : String ) { Val = s }

}
