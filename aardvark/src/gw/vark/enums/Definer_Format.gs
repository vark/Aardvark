package gw.vark.enums

enum Definer_Format{

  Properties("properties"),
  Xml("xml"),

  var _val : String as Val

  private construct( s : String ) { Val = s }


}