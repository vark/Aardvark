package gw.vark.test;


import com.google.common.base.Joiner;

import java.lang.String;

/**
 */
public class HelloWorld {
  public static void main(String[] args) {
    HelloWorld hw = new HelloWorld();
    System.out.println(hw);
  }

  public String join(String... words) {
    return Joiner.on(" ").join(words);
  }

  public String toString() {
    return "Hello World";
  }
}
