package gw.vark.enums

enum ModifiedSelector_ComparatorName{

  Equal("equal"),
  Rule("rule"),

  property get Instance() : org.apache.tools.ant.types.selectors.modifiedselector.ModifiedSelector.ComparatorName {
    return org.apache.tools.ant.types.EnumeratedAttribute.getInstance(org.apache.tools.ant.types.selectors.modifiedselector.ModifiedSelector.ComparatorName, Val) as org.apache.tools.ant.types.selectors.modifiedselector.ModifiedSelector.ComparatorName
  }

  var _val : String as Val

  private construct( s : String ) { Val = s }

}
