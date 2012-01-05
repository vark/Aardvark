package gw.vark.enums

enum FixCrLfFilter_CrLf{

  Asis("asis"),
  Cr("cr"),
  Lf("lf"),
  Crlf("crlf"),
  Mac("mac"),
  Unix("unix"),
  Dos("dos"),

  property get Instance() : org.apache.tools.ant.filters.FixCrLfFilter.CrLf {
    return org.apache.tools.ant.types.EnumeratedAttribute.getInstance(org.apache.tools.ant.filters.FixCrLfFilter.CrLf, Val) as org.apache.tools.ant.filters.FixCrLfFilter.CrLf
  }

  var _val : String as Val

  private construct( s : String ) { Val = s }

}
