package gw.vark.enums

enum PropertyFile_Unit{

  Millisecond("millisecond"),
  Second("second"),
  Minute("minute"),
  Hour("hour"),
  Day("day"),
  Week("week"),
  Month("month"),
  Year("year"),

  property get Instance() : org.apache.tools.ant.taskdefs.optional.PropertyFile.Unit {
    return org.apache.tools.ant.types.EnumeratedAttribute.getInstance(org.apache.tools.ant.taskdefs.optional.PropertyFile.Unit, Val) as org.apache.tools.ant.taskdefs.optional.PropertyFile.Unit
  }

  var _val : String as Val

  private construct( s : String ) { Val = s }

}
