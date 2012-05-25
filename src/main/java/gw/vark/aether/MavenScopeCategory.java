package gw.vark.aether;

/**
 * Created with IntelliJ IDEA.
 * User: bchang
 * Date: 5/25/12
 * Time: 1:05 PM
 * To change this template use File | Settings | File Templates.
 */
public enum MavenScopeCategory {
  COMPILE("compile,system,provided"),
  RUNTIME("compile,runtime"),
  TEST("compile,system,provided,runtime,test"),
  ;

  private String _expanded;
  MavenScopeCategory(String expanded) {
    _expanded = expanded;
  }
  public String getExpanded() {
    return _expanded;
  }
}
