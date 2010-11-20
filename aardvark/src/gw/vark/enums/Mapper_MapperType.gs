package gw.vark.enums

enum Mapper_MapperType{

  Identity("identity"),
  Flatten("flatten"),
  Glob("glob"),
  Merge("merge"),
  Regexp("regexp"),
  Package("package"),
  Unpackage("unpackage"),

  var _val : String as Val

  private construct( s : String ) { Val = s }


}