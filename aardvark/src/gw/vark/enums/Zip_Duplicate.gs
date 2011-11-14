package gw.vark.enums

enum Zip_Duplicate{

  Add("add"),
  Preserve("preserve"),
  Fail("fail"),

  property get Instance() : org.apache.tools.ant.taskdefs.Zip.Duplicate {
    return org.apache.tools.ant.types.EnumeratedAttribute.getInstance(org.apache.tools.ant.taskdefs.Zip.Duplicate, Val) as org.apache.tools.ant.taskdefs.Zip.Duplicate
  }

  var _val : String as Val

  private construct( s : String ) { Val = s }

}
