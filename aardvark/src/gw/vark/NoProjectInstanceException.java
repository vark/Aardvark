package gw.vark;

/**
 * Created by IntelliJ IDEA.
 * User: bchang
 * Date: 2/21/12
 * Time: 4:33 PM
 * To change this template use File | Settings | File Templates.
 */
public class NoProjectInstanceException extends IllegalStateException {
  public NoProjectInstanceException() {
    super("Aardvark has no project instance");
  }
}
