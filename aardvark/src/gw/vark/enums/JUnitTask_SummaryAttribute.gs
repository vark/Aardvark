package gw.vark.enums

enum JUnitTask_SummaryAttribute{

  True("true"),
  Yes("yes"),
  False("false"),
  No("no"),
  On("on"),
  Off("off"),
  WithOutAndErr("withOutAndErr"),

  var _val : String as Val

  private construct( s : String ) { Val = s }


}