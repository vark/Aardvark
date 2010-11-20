package gw.vark.enums

enum NetRexxC_VerboseAttr{

  Verbose("verbose"),
  Verbose0("verbose0"),
  Verbose1("verbose1"),
  Verbose2("verbose2"),
  Verbose3("verbose3"),
  Verbose4("verbose4"),
  Verbose5("verbose5"),
  Noverbose("noverbose"),

  var _val : String as Val

  private construct( s : String ) { Val = s }


}