package gw.vark.enums

enum PropertyFile_Entry_Type{

  Int("int"),
  Date("date"),
  String("string"),

  property get Instance() : org.apache.tools.ant.taskdefs.optional.PropertyFile.Entry.Type {
    return org.apache.tools.ant.types.EnumeratedAttribute.getInstance(org.apache.tools.ant.taskdefs.optional.PropertyFile.Entry.Type, Val) as org.apache.tools.ant.taskdefs.optional.PropertyFile.Entry.Type
  }

  var _val : String as Val

  private construct( s : String ) { Val = s }

}
