package gw.vark.enums

enum FixCRLF_AddAsisRemove{

  Add("add"),
  Asis("asis"),
  Remove("remove"),

  var _val : String as Val

  private construct( s : String ) { Val = s }


}