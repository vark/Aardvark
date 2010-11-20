package gw.vark.enums

enum EchoXML_NamespacePolicy{

  Ignore("ignore"),
  ElementsOnly("elementsOnly"),
  All("all"),

  var _val : String as Val

  private construct( s : String ) { Val = s }


}