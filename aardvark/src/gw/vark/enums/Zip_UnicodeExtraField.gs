package gw.vark.enums

enum Zip_UnicodeExtraField{

  Never("never"),
  Always("always"),
  Not_encodeable("not-encodeable"),

  var _val : String as Val

  private construct( s : String ) { Val = s }


}