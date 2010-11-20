package gw.vark.enums

enum FormatterElement_TypeAttribute{

  Plain("plain"),
  Xml("xml"),
  Brief("brief"),
  Failure("failure"),

  var _val : String as Val

  private construct( s : String ) { Val = s }


}