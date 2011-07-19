package gw.vark.enums

enum MSVSS_WritableFiles{

  Replace("replace"),
  Skip("skip"),
  Fail("fail"),

  property get Instance() : org.apache.tools.ant.taskdefs.optional.vss.MSVSS.WritableFiles {
    return org.apache.tools.ant.types.EnumeratedAttribute.getInstance(org.apache.tools.ant.taskdefs.optional.vss.MSVSS.WritableFiles, Val) as org.apache.tools.ant.taskdefs.optional.vss.MSVSS.WritableFiles
  }

  var _val : String as Val

  private construct( s : String ) { Val = s }

}
