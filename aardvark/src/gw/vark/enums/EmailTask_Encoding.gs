package gw.vark.enums

enum EmailTask_Encoding{

  Auto("auto"),
  Mime("mime"),
  Uu("uu"),
  Plain("plain"),

  property get Instance() : org.apache.tools.ant.taskdefs.email.EmailTask.Encoding {
    return org.apache.tools.ant.types.EnumeratedAttribute.getInstance(org.apache.tools.ant.taskdefs.email.EmailTask.Encoding, Val) as org.apache.tools.ant.taskdefs.email.EmailTask.Encoding
  }

  var _val : String as Val

  private construct( s : String ) { Val = s }

}
