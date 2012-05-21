package gw.vark.enums

enum ModifiedSelector_AlgorithmName{

  Hashvalue("hashvalue"),
  Digest("digest"),
  Checksum("checksum"),

  property get Instance() : org.apache.tools.ant.types.selectors.modifiedselector.ModifiedSelector.AlgorithmName {
    return org.apache.tools.ant.types.EnumeratedAttribute.getInstance(org.apache.tools.ant.types.selectors.modifiedselector.ModifiedSelector.AlgorithmName, Val) as org.apache.tools.ant.types.selectors.modifiedselector.ModifiedSelector.AlgorithmName
  }

  var _val : String as Val

  private construct( s : String ) { Val = s }

}
