package gw.vark.enums

enum SQLExec_DelimiterType{

  Normal("normal"),
  Row("row"),

  property get Instance() : org.apache.tools.ant.taskdefs.SQLExec.DelimiterType {
    return org.apache.tools.ant.types.EnumeratedAttribute.getInstance(org.apache.tools.ant.taskdefs.SQLExec.DelimiterType, Val) as org.apache.tools.ant.taskdefs.SQLExec.DelimiterType
  }

  var _val : String as Val

  private construct( s : String ) { Val = s }

}
