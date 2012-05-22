package gw.vark.it;

import org.fest.assertions.Assertions;
import org.junit.Test;

import java.io.File;

public class AssemblyStructureITCase extends Assertions {

  @Test
  public void licenseExists() {
    File assemblyDir = ITUtil.getAssemblyDir();
    File licenseFile = new File(assemblyDir, "LICENSE");
    assertThat(licenseFile).exists();
    assertThat(licenseFile).isFile();
  }

}
