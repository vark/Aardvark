package gw.vark.enums

enum ManifestTask_Mode{

  Update("update"),
  Replace("replace"),

  var _val : String as Val

  private construct( s : String ) { Val = s }


}