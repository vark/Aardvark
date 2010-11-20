package gw.vark.enums

enum AggregateTransformer_Format{

  Frames("frames"),
  Noframes("noframes"),

  var _val : String as Val

  private construct( s : String ) { Val = s }


}