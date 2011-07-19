package gw.vark.enums

enum PresentSelector_FilePresence{

  Srconly("srconly"),
  Both("both"),

  property get Instance() : org.apache.tools.ant.types.selectors.PresentSelector.FilePresence {
    return org.apache.tools.ant.types.EnumeratedAttribute.getInstance(org.apache.tools.ant.types.selectors.PresentSelector.FilePresence, Val) as org.apache.tools.ant.types.selectors.PresentSelector.FilePresence
  }

  var _val : String as Val

  private construct( s : String ) { Val = s }

}
