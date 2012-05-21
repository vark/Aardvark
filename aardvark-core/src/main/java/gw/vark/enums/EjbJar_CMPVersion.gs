package gw.vark.enums

enum EjbJar_CMPVersion{

  _1_0("1.0"),
  _2_0("2.0"),

  property get Instance() : org.apache.tools.ant.taskdefs.optional.ejb.EjbJar.CMPVersion {
    return org.apache.tools.ant.types.EnumeratedAttribute.getInstance(org.apache.tools.ant.taskdefs.optional.ejb.EjbJar.CMPVersion, Val) as org.apache.tools.ant.taskdefs.optional.ejb.EjbJar.CMPVersion
  }

  var _val : String as Val

  private construct( s : String ) { Val = s }

}
