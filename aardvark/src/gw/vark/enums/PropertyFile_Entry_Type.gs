package gw.vark.enums

enum PropertyFile_Entry_Type{

  Int("int"),
  Date("date"),
  String("string"),

  var _val : String as Val

  private construct( s : String ) { Val = s }


}