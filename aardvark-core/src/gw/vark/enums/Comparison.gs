package gw.vark.enums

enum Comparison{

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

  property get Instance() : org.apache.tools.ant.types.Comparison {
    return org.apache.tools.ant.types.EnumeratedAttribute.getInstance(org.apache.tools.ant.types.Comparison, Val) as org.apache.tools.ant.types.Comparison
  }

  var _val : String as Val

  private construct( s : String ) { Val = s }

}
