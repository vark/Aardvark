package gw.vark.enums

enum Checksum_FormatElement{

  CHECKSUM("CHECKSUM"),
  MD5SUM("MD5SUM"),
  SVF("SVF"),

  property get Instance() : org.apache.tools.ant.taskdefs.Checksum.FormatElement {
    return org.apache.tools.ant.types.EnumeratedAttribute.getInstance(org.apache.tools.ant.taskdefs.Checksum.FormatElement, Val) as org.apache.tools.ant.taskdefs.Checksum.FormatElement
  }

  var _val : String as Val

  private construct( s : String ) { Val = s }

}
