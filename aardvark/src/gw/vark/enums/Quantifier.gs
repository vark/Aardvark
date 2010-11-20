package gw.vark.enums

enum Quantifier{

  All("all"),
  Each("each"),
  Every("every"),
  Any("any"),
  Some("some"),
  One("one"),
  Majority("majority"),
  Most("most"),
  None("none"),

  var _val : String as Val

  private construct( s : String ) { Val = s }


}