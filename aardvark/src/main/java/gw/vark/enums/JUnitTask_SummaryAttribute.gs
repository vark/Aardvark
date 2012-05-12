package gw.vark.enums

enum JUnitTask_SummaryAttribute{

  True("true"),
  Yes("yes"),
  False("false"),
  No("no"),
  On("on"),
  Off("off"),
  WithOutAndErr("withOutAndErr"),

  property get Instance() : org.apache.tools.ant.taskdefs.optional.junit.JUnitTask.SummaryAttribute {
    return org.apache.tools.ant.types.EnumeratedAttribute.getInstance(org.apache.tools.ant.taskdefs.optional.junit.JUnitTask.SummaryAttribute, Val) as org.apache.tools.ant.taskdefs.optional.junit.JUnitTask.SummaryAttribute
  }

  var _val : String as Val

  private construct( s : String ) { Val = s }

}
