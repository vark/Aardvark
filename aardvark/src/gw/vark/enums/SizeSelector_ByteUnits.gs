package gw.vark.enums

enum SizeSelector_ByteUnits{

  K("K"),
  K("k"),
  Kilo("kilo"),
  KILO("KILO"),
  Ki("Ki"),
  KI("KI"),
  Ki("ki"),
  Kibi("kibi"),
  KIBI("KIBI"),
  M("M"),
  M("m"),
  Mega("mega"),
  MEGA("MEGA"),
  Mi("Mi"),
  MI("MI"),
  Mi("mi"),
  Mebi("mebi"),
  MEBI("MEBI"),
  G("G"),
  G("g"),
  Giga("giga"),
  GIGA("GIGA"),
  Gi("Gi"),
  GI("GI"),
  Gi("gi"),
  Gibi("gibi"),
  GIBI("GIBI"),
  T("T"),
  T("t"),
  Tera("tera"),
  TERA("TERA"),
  Ti("Ti"),
  TI("TI"),
  Ti("ti"),
  Tebi("tebi"),
  TEBI("TEBI"),

  var _val : String as Val

  private construct( s : String ) { Val = s }


}