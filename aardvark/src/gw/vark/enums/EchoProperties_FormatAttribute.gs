package gw.vark.enums

enum EchoProperties_FormatAttribute{

  Xml("xml"),
  Text("text"),

  var _val : String as Val

  private construct( s : String ) { Val = s }


}