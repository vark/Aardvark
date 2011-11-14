package gw.vark.enums

enum DateSelector_TimeComparisons{

  Before("before"),
  After("after"),
  Equal("equal"),

  property get Instance() : org.apache.tools.ant.types.selectors.DateSelector.TimeComparisons {
    return org.apache.tools.ant.types.EnumeratedAttribute.getInstance(org.apache.tools.ant.types.selectors.DateSelector.TimeComparisons, Val) as org.apache.tools.ant.types.selectors.DateSelector.TimeComparisons
  }

  var _val : String as Val

  private construct( s : String ) { Val = s }

}
