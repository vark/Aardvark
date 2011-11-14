package gw.vark.enums

enum FilterSet_OnMissing{

  Fail("fail"),
  Warn("warn"),
  Ignore("ignore"),

  property get Instance() : org.apache.tools.ant.types.FilterSet.OnMissing {
    return org.apache.tools.ant.types.EnumeratedAttribute.getInstance(org.apache.tools.ant.types.FilterSet.OnMissing, Val) as org.apache.tools.ant.types.FilterSet.OnMissing
  }

  var _val : String as Val

  private construct( s : String ) { Val = s }

}
