package gw.vark.enums

enum FixCrLfFilter_AddAsisRemove{

  Add("add"),
  Asis("asis"),
  Remove("remove"),

  var _val : String as Val

  private construct( s : String ) { Val = s }


}