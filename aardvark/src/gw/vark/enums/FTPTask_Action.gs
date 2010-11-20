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

  var _val : String as Val

  private construct( s : String ) { Val = s }


}