package gw.vark.enums

enum MSVSSHISTORY_BriefCodediffNofile{

  Brief("brief"),
  Codediff("codediff"),
  Nofile("nofile"),
  Default("default"),

  property get Instance() : org.apache.tools.ant.taskdefs.optional.vss.MSVSSHISTORY.BriefCodediffNofile {
    return org.apache.tools.ant.types.EnumeratedAttribute.getInstance(org.apache.tools.ant.taskdefs.optional.vss.MSVSSHISTORY.BriefCodediffNofile, Val) as org.apache.tools.ant.taskdefs.optional.vss.MSVSSHISTORY.BriefCodediffNofile
  }

  var _val : String as Val

  private construct( s : String ) { Val = s }

}
