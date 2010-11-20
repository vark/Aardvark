package gw.vark.enums

enum MSVSS_CurrentModUpdated{

  Current("current"),
  Modified("modified"),
  Updated("updated"),

  var _val : String as Val

  private construct( s : String ) { Val = s }


}