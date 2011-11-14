package gw.vark.enums

enum PropertyFile_Entry_Operation{

  _+("+"),
  _-("-"),
  _=("="),
  Del("del"),

  property get Instance() : org.apache.tools.ant.taskdefs.optional.PropertyFile.Entry.Operation {
    return org.apache.tools.ant.types.EnumeratedAttribute.getInstance(org.apache.tools.ant.taskdefs.optional.PropertyFile.Entry.Operation, Val) as org.apache.tools.ant.taskdefs.optional.PropertyFile.Entry.Operation
  }

  var _val : String as Val

  private construct( s : String ) { Val = s }

}
