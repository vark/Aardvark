package gw.vark.enums

enum SQLExec_DelimiterType{

  Normal("normal"),
  Row("row"),

  var _val : String as Val

  private construct( s : String ) { Val = s }


}