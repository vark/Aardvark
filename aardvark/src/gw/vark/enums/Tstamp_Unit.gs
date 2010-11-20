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

  var _val : String as Val

  private construct( s : String ) { Val = s }


}