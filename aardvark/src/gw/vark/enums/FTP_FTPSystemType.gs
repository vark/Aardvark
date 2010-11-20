package gw.vark.enums

enum FTP_FTPSystemType{

  NoVal("NoVal"),
  UNIX("UNIX"),
  VMS("VMS"),
  WINDOWS("WINDOWS"),
  OS_2("OS/2"),
  OS_400("OS/400"),
  MVS("MVS"),

  var _val : String as Val

  private construct( s : String ) { Val = s }


}