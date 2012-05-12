package gw.vark.enums

enum AggregateTransformer_Format{

  Frames("frames"),
  Noframes("noframes"),

  property get Instance() : org.apache.tools.ant.taskdefs.optional.junit.AggregateTransformer.Format {
    return org.apache.tools.ant.types.EnumeratedAttribute.getInstance(org.apache.tools.ant.taskdefs.optional.junit.AggregateTransformer.Format, Val) as org.apache.tools.ant.taskdefs.optional.junit.AggregateTransformer.Format
  }

  var _val : String as Val

  private construct( s : String ) { Val = s }

}
