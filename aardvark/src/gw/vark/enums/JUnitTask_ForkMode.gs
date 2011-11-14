package gw.vark.enums

enum JUnitTask_ForkMode{

  Once("once"),
  PerTest("perTest"),
  PerBatch("perBatch"),

  property get Instance() : org.apache.tools.ant.taskdefs.optional.junit.JUnitTask.ForkMode {
    return org.apache.tools.ant.types.EnumeratedAttribute.getInstance(org.apache.tools.ant.taskdefs.optional.junit.JUnitTask.ForkMode, Val) as org.apache.tools.ant.taskdefs.optional.junit.JUnitTask.ForkMode
  }

  var _val : String as Val

  private construct( s : String ) { Val = s }

}
