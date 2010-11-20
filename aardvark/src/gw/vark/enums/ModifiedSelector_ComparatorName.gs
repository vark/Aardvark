package gw.vark.enums

enum ModifiedSelector_ComparatorName{

  Equal("equal"),
  Rule("rule"),

  var _val : String as Val

  private construct( s : String ) { Val = s }


}