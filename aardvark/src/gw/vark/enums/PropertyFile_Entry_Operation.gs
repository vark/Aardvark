package gw.vark.enums

enum PropertyFile_Entry_Operation{

  _+("+"),
  _-("-"),
  _=("="),
  Del("del"),

  var _val : String as Val

  private construct( s : String ) { Val = s }


}