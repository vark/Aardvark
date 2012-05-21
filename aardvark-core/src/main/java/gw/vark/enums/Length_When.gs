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

  property get Instance() : org.apache.tools.ant.taskdefs.Length.When {
    return org.apache.tools.ant.types.EnumeratedAttribute.getInstance(org.apache.tools.ant.taskdefs.Length.When, Val) as org.apache.tools.ant.taskdefs.Length.When
  }

  var _val : String as Val

  private construct( s : String ) { Val = s }

}
