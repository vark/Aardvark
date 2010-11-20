package gw.vark.enums

enum EjbJar_NamingScheme{

  Ejb_name("ejb-name"),
  Directory("directory"),
  Descriptor("descriptor"),
  Basejarname("basejarname"),

  var _val : String as Val

  private construct( s : String ) { Val = s }


}