package gw.vark.enums

enum Zip_UnicodeExtraField{

  Never("never"),
  Always("always"),
  Not_encodeable("not-encodeable"),

  property get Instance() : org.apache.tools.ant.taskdefs.Zip.UnicodeExtraField {
    return org.apache.tools.ant.types.EnumeratedAttribute.getInstance(org.apache.tools.ant.taskdefs.Zip.UnicodeExtraField, Val) as org.apache.tools.ant.taskdefs.Zip.UnicodeExtraField
  }

  var _val : String as Val

  private construct( s : String ) { Val = s }

}
