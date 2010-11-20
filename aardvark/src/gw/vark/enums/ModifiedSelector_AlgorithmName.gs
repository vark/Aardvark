package gw.vark.enums

enum ModifiedSelector_AlgorithmName{

  HashVal("hashVal"),
  Digest("digest"),
  Checksum("checksum"),

  var _val : String as Val

  private construct( s : String ) { Val = s }


}