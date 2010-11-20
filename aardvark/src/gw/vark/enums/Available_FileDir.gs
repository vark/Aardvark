package gw.vark.enums

enum Available_FileDir{

  File("file"),
  Dir("dir"),

  var _val : String as Val

  private construct( s : String ) { Val = s }


}