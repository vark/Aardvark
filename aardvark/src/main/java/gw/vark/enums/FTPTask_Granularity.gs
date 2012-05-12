package gw.vark.enums

enum FTPTask_Granularity{

  NoVal("NoVal"),
  MINUTE("MINUTE"),
  NONE("NONE"),

  property get Instance() : org.apache.tools.ant.taskdefs.optional.net.FTPTask.Granularity {
    return org.apache.tools.ant.types.EnumeratedAttribute.getInstance(org.apache.tools.ant.taskdefs.optional.net.FTPTask.Granularity, Val) as org.apache.tools.ant.taskdefs.optional.net.FTPTask.Granularity
  }

  var _val : String as Val

  private construct( s : String ) { Val = s }

}
