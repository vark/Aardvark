package gw.vark.enums

enum PropertySet_BuiltinPropertySetName{

  All("all"),
  System("system"),
  Commandline("commandline"),

  var _val : String as Val

  private construct( s : String ) { Val = s }


}