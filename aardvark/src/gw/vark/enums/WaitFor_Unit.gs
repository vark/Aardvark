package gw.vark.enums

enum WaitFor_Unit{

  Millisecond("millisecond"),
  Second("second"),
  Minute("minute"),
  Hour("hour"),
  Day("day"),
  Week("week"),

  var _val : String as Val

  private construct( s : String ) { Val = s }


}