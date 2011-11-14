package gw.vark.enums

enum Tar_TarCompressionMethod{

  None("none"),
  Gzip("gzip"),
  Bzip2("bzip2"),

  property get Instance() : org.apache.tools.ant.taskdefs.Tar.TarCompressionMethod {
    return org.apache.tools.ant.types.EnumeratedAttribute.getInstance(org.apache.tools.ant.taskdefs.Tar.TarCompressionMethod, Val) as org.apache.tools.ant.taskdefs.Tar.TarCompressionMethod
  }

  var _val : String as Val

  private construct( s : String ) { Val = s }

}
