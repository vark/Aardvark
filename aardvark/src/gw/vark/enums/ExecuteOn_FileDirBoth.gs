package gw.vark.enums

enum ExecuteOn_FileDirBoth{

  File("file"),
  Dir("dir"),
  Both("both"),

  var _val : String as Val

  private construct( s : String ) { Val = s }


}