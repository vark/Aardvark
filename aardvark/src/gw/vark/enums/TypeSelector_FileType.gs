package gw.vark.enums

enum TypeSelector_FileType{

  File("file"),
  Dir("dir"),

  var _val : String as Val

  private construct( s : String ) { Val = s }


}