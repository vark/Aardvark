package gw.vark.enums

enum FTPTask_Action{

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

  property get Instance() : org.apache.tools.ant.taskdefs.optional.net.FTPTask.Action {
    return org.apache.tools.ant.types.EnumeratedAttribute.getInstance(org.apache.tools.ant.taskdefs.optional.net.FTPTask.Action, Val) as org.apache.tools.ant.taskdefs.optional.net.FTPTask.Action
  }

  var _val : String as Val

  private construct( s : String ) { Val = s }

}
