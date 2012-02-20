package gw.vark.enums

enum Tstamp_Unit{

  Millisecond("millisecond"),
  Second("second"),
  Minute("minute"),
  Hour("hour"),
  Day("day"),
  Week("week"),
  Month("month"),
  Year("year"),

  property get Instance() : org.apache.tools.ant.taskdefs.Tstamp.Unit {
    return org.apache.tools.ant.types.EnumeratedAttribute.getInstance(org.apache.tools.ant.taskdefs.Tstamp.Unit, Val) as org.apache.tools.ant.taskdefs.Tstamp.Unit
  }

  var _val : String as Val

  private construct( s : String ) { Val = s }

}
