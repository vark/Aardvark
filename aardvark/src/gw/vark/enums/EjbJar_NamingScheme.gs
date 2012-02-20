package gw.vark.enums

enum EjbJar_NamingScheme{

  Ejb_name("ejb-name"),
  Directory("directory"),
  Descriptor("descriptor"),
  Basejarname("basejarname"),

  property get Instance() : org.apache.tools.ant.taskdefs.optional.ejb.EjbJar.NamingScheme {
    return org.apache.tools.ant.types.EnumeratedAttribute.getInstance(org.apache.tools.ant.taskdefs.optional.ejb.EjbJar.NamingScheme, Val) as org.apache.tools.ant.taskdefs.optional.ejb.EjbJar.NamingScheme
  }

  var _val : String as Val

  private construct( s : String ) { Val = s }

}
