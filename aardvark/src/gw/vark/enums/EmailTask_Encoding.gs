package gw.vark.enums

enum EmailTask_Encoding{

  Auto("auto"),
  Mime("mime"),
  Uu("uu"),
  Plain("plain"),

  var _val : String as Val

  private construct( s : String ) { Val = s }


}