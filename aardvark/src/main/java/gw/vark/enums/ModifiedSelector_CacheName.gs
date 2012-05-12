package gw.vark.enums

enum ModifiedSelector_CacheName{

  Propertyfile("propertyfile"),

  property get Instance() : org.apache.tools.ant.types.selectors.modifiedselector.ModifiedSelector.CacheName {
    return org.apache.tools.ant.types.EnumeratedAttribute.getInstance(org.apache.tools.ant.types.selectors.modifiedselector.ModifiedSelector.CacheName, Val) as org.apache.tools.ant.types.selectors.modifiedselector.ModifiedSelector.CacheName
  }

  var _val : String as Val

  private construct( s : String ) { Val = s }

}
