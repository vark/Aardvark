package gw.vark.enums

enum Input_HandlerType{

  Default("default"),
  Propertyfile("propertyfile"),
  Greedy("greedy"),
  Secure("secure"),

  var _val : String as Val

  private construct( s : String ) { Val = s }


}