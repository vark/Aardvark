package gw.vark.enums

enum Mapper_MapperType{

  Identity("identity"),
  Flatten("flatten"),
  Glob("glob"),
  Merge("merge"),
  Regexp("regexp"),
  Package("package"),
  Unpackage("unpackage"),

  property get Instance() : org.apache.tools.ant.types.Mapper.MapperType {
    return org.apache.tools.ant.types.EnumeratedAttribute.getInstance(org.apache.tools.ant.types.Mapper.MapperType, Val) as org.apache.tools.ant.types.Mapper.MapperType
  }

  var _val : String as Val

  private construct( s : String ) { Val = s }

}
