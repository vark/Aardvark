package gw.vark.enums

enum FTP_Action{

  Send("send"),
  Put("put"),
  Recv("recv"),
  Get("get"),
  Del("del"),
  Delete("delete"),
  List("list"),
  Mkdir("mkdir"),
  Chmod("chmod"),
  Rmdir("rmdir"),
  Site("site"),

  property get Instance() : org.apache.tools.ant.taskdefs.optional.net.FTP.Action {
    return org.apache.tools.ant.types.EnumeratedAttribute.getInstance(org.apache.tools.ant.taskdefs.optional.net.FTP.Action, Val) as org.apache.tools.ant.taskdefs.optional.net.FTP.Action
  }

  var _val : String as Val

  private construct( s : String ) { Val = s }

}
