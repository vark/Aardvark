package gw.vark.enums

enum FixCRLF_AddAsisRemove{

  Add("add"),
  Asis("asis"),
  Remove("remove"),

  property get Instance() : org.apache.tools.ant.taskdefs.FixCRLF.AddAsisRemove {
    return org.apache.tools.ant.types.EnumeratedAttribute.getInstance(org.apache.tools.ant.taskdefs.FixCRLF.AddAsisRemove, Val) as org.apache.tools.ant.taskdefs.FixCRLF.AddAsisRemove
  }

  var _val : String as Val

  private construct( s : String ) { Val = s }

}
