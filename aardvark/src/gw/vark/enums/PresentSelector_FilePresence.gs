package gw.vark.enums

enum PresentSelector_FilePresence{

  Srconly("srconly"),
  Both("both"),

  var _val : String as Val

  private construct( s : String ) { Val = s }


}