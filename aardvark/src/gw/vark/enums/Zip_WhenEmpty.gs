package gw.vark.enums

enum Zip_WhenEmpty{

  Fail("fail"),
  Skip("skip"),
  Create("create"),

  property get Instance() : org.apache.tools.ant.taskdefs.Zip.WhenEmpty {
    return org.apache.tools.ant.types.EnumeratedAttribute.getInstance(org.apache.tools.ant.taskdefs.Zip.WhenEmpty, Val) as org.apache.tools.ant.taskdefs.Zip.WhenEmpty
  }

  var _val : String as Val

  private construct( s : String ) { Val = s }

}
