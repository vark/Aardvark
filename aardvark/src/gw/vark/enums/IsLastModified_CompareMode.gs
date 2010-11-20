package gw.vark.enums

enum IsLastModified_CompareMode{

  Equals("equals"),
  Before("before"),
  After("after"),
  Not_before("not-before"),
  Not_after("not-after"),

  var _val : String as Val

  private construct( s : String ) { Val = s }


}