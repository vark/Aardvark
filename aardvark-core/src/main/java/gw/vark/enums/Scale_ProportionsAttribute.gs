package gw.vark.enums

enum Scale_ProportionsAttribute{

  Ignore("ignore"),
  Width("width"),
  Height("height"),
  Cover("cover"),
  Fit("fit"),

  property get Instance() : org.apache.tools.ant.types.optional.image.Scale.ProportionsAttribute {
    return org.apache.tools.ant.types.EnumeratedAttribute.getInstance(org.apache.tools.ant.types.optional.image.Scale.ProportionsAttribute, Val) as org.apache.tools.ant.types.optional.image.Scale.ProportionsAttribute
  }

  var _val : String as Val

  private construct( s : String ) { Val = s }

}
