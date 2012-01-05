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

  property get Instance() : org.apache.tools.ant.types.Quantifier {
    return org.apache.tools.ant.types.EnumeratedAttribute.getInstance(org.apache.tools.ant.types.Quantifier, Val) as org.apache.tools.ant.types.Quantifier
  }

  var _val : String as Val

  private construct( s : String ) { Val = s }

}
