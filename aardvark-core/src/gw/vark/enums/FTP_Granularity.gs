package gw.vark.enums

enum FTP_Granularity{

  NoVal("NoVal"),
  MINUTE("MINUTE"),
  NONE("NONE"),

  property get Instance() : org.apache.tools.ant.taskdefs.optional.net.FTP.Granularity {
    return org.apache.tools.ant.types.EnumeratedAttribute.getInstance(org.apache.tools.ant.taskdefs.optional.net.FTP.Granularity, Val) as org.apache.tools.ant.taskdefs.optional.net.FTP.Granularity
  }

  var _val : String as Val

  private construct( s : String ) { Val = s }

}
