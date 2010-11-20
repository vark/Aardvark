package gw.vark.enums

enum JUnitTask_ForkMode{

  Once("once"),
  PerTest("perTest"),
  PerBatch("perBatch"),

  var _val : String as Val

  private construct( s : String ) { Val = s }


}