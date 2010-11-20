package gw.vark.enums

enum Length_When{

  Equal("equal"),
  Greater("greater"),
  Less("less"),
  Ne("ne"),
  Ge("ge"),
  Le("le"),
  Eq("eq"),
  Gt("gt"),
  Lt("lt"),
  More("more"),

  var _val : String as Val

  private construct( s : String ) { Val = s }


}