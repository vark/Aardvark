package gw.vark.enums

enum Type_FileDir{

  File("file"),
  Dir("dir"),
  Any("any"),

  var _val : String as Val

  private construct( s : String ) { Val = s }


}