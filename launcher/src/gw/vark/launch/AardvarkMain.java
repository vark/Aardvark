package gw.vark.launch;

/**
 * Interface used to bridge to the actual Aardvark class without any
 * messy reflection
 */
public interface AardvarkMain {
  /**
   * Start Aardvark.
   *
   * @param args command line args
   * @return the exit code
   */
  int start(String[] args);
}
