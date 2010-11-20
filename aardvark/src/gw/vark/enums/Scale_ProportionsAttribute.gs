package gw.vark.enums

enum Scale_ProportionsAttribute{

  Ignore("ignore"),
  Width("width"),
  Height("height"),
  Cover("cover"),
  Fit("fit"),

  var _val : String as Val

  private construct( s : String ) { Val = s }


}