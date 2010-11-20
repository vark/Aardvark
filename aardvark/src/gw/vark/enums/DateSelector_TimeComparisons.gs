package gw.vark.enums

enum DateSelector_TimeComparisons{

  Before("before"),
  After("after"),
  Equal("equal"),

  var _val : String as Val

  private construct( s : String ) { Val = s }


}