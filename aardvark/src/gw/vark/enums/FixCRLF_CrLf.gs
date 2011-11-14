package gw.vark.enums

enum FixCRLF_CrLf{

  Asis("asis"),
  Cr("cr"),
  Lf("lf"),
  Crlf("crlf"),
  Mac("mac"),
  Unix("unix"),
  Dos("dos"),

  property get Instance() : org.apache.tools.ant.taskdefs.FixCRLF.CrLf {
    return org.apache.tools.ant.types.EnumeratedAttribute.getInstance(org.apache.tools.ant.taskdefs.FixCRLF.CrLf, Val) as org.apache.tools.ant.taskdefs.FixCRLF.CrLf
  }

  var _val : String as Val

  private construct( s : String ) { Val = s }

}
