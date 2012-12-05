package gw.vark.idea;

import gw.config.CommonServices;
import gw.lang.parser.IGosuValidator;
import gw.lang.parser.ISymbolTable;
import gw.lang.parser.ITypeUsesMap;
import gw.plugin.ij.lang.psi.impl.AbstractGosuClassFileImpl;
import gw.plugin.ij.lang.psi.impl.IGosuParserConfigurer;

/**
 */
public class AardvarkParserConfigurer implements IGosuParserConfigurer {
  @Override
  public ISymbolTable getSymbolTable(AbstractGosuClassFileImpl abstractGosuClassFile) {
    return null;
  }

  @Override
  public ITypeUsesMap getTypeUsesMap(AbstractGosuClassFileImpl abstractGosuClassFile) {
    boolean vark = AardvarkProgramFileProvider.isProgram(abstractGosuClassFile.getVirtualFile());
    return vark ?
            CommonServices.getGosuIndustrialPark().createTypeUsesMap(AardvarkProgramFileProvider.DEFAULT_USES) :
            null;
  }

  @Override
  public IGosuValidator getValidator(AbstractGosuClassFileImpl abstractGosuClassFile) {
    return null;
  }
}
