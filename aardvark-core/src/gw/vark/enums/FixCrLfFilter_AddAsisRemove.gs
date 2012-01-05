package gw.vark.enums

enum FixCrLfFilter_AddAsisRemove{

  Add("add"),
  Asis("asis"),
  Remove("remove"),

  property get Instance() : org.apache.tools.ant.filters.FixCrLfFilter.AddAsisRemove {
    return org.apache.tools.ant.types.EnumeratedAttribute.getInstance(org.apache.tools.ant.filters.FixCrLfFilter.AddAsisRemove, Val) as org.apache.tools.ant.filters.FixCrLfFilter.AddAsisRemove
  }

  var _val : String as Val

  private construct( s : String ) { Val = s }

}
