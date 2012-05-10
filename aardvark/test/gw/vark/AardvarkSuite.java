package gw.vark;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
        AardvarkBootstrapTest.class,
        AardvarkOptionsTest.class,
        TestprojectTest.class,
        TestAntProjectTest.class,
        AardvarkProcessTest.class
})
public class AardvarkSuite {
}
