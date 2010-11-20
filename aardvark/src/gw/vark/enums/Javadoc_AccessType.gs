package gw.vark.enums

enum Javadoc_AccessType{

  Protected("protected"),
  Public("public"),
  Package("package"),
  Private("private"),

  var _val : String as Val

  private construct( s : String ) { Val = s }


}