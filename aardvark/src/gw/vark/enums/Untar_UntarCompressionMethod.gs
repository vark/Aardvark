package gw.vark.enums

enum Untar_UntarCompressionMethod{

  None("none"),
  Gzip("gzip"),
  Bzip2("bzip2"),

  property get Instance() : org.apache.tools.ant.taskdefs.Untar.UntarCompressionMethod {
    return org.apache.tools.ant.types.EnumeratedAttribute.getInstance(org.apache.tools.ant.taskdefs.Untar.UntarCompressionMethod, Val) as org.apache.tools.ant.taskdefs.Untar.UntarCompressionMethod
  }

  var _val : String as Val

  private construct( s : String ) { Val = s }

}
