package gw.vark.enums

enum FTP_Granularity{

  NoVal("NoVal"),
  MINUTE("MINUTE"),
  NONE("NONE"),

  var _val : String as Val

  private construct( s : String ) { Val = s }


}