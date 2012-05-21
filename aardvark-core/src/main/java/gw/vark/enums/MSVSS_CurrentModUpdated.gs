package gw.vark.enums

enum MSVSS_CurrentModUpdated{

  Current("current"),
  Modified("modified"),
  Updated("updated"),

  property get Instance() : org.apache.tools.ant.taskdefs.optional.vss.MSVSS.CurrentModUpdated {
    return org.apache.tools.ant.types.EnumeratedAttribute.getInstance(org.apache.tools.ant.taskdefs.optional.vss.MSVSS.CurrentModUpdated, Val) as org.apache.tools.ant.taskdefs.optional.vss.MSVSS.CurrentModUpdated
  }

  var _val : String as Val

  private construct( s : String ) { Val = s }

}
