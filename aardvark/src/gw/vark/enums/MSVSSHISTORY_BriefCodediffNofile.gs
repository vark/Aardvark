package gw.vark.enums

enum MSVSSHISTORY_BriefCodediffNofile{

  Brief("brief"),
  Codediff("codediff"),
  Nofile("nofile"),
  Default("default"),

  var _val : String as Val

  private construct( s : String ) { Val = s }


}