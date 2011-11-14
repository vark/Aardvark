package gw.vark.enums

enum Tar_TarLongFileMode{

  Warn("warn"),
  Fail("fail"),
  Truncate("truncate"),
  Gnu("gnu"),
  Omit("omit"),

  property get Instance() : org.apache.tools.ant.taskdefs.Tar.TarLongFileMode {
    return org.apache.tools.ant.types.EnumeratedAttribute.getInstance(org.apache.tools.ant.taskdefs.Tar.TarLongFileMode, Val) as org.apache.tools.ant.taskdefs.Tar.TarLongFileMode
  }

  var _val : String as Val

  private construct( s : String ) { Val = s }

}
