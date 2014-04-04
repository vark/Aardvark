package gw.vark.init;

import gw.config.BaseService;
import gw.fs.IDirectory;
import gw.lang.parser.GlobalScope;
import gw.lang.parser.IAttributeSource;
import gw.lang.parser.ILanguageLevel;
import gw.lang.parser.IParseIssue;
import gw.lang.parser.ITypeUsesMap;
import gw.lang.parser.expressions.IQueryExpression;
import gw.lang.parser.expressions.IQueryExpressionEvaluator;
import gw.lang.reflect.IEntityAccess;
import gw.lang.reflect.IGosuClassLoadingObserver;
import gw.lang.reflect.IPropertyInfo;
import gw.lang.reflect.IType;
import gw.lang.reflect.gs.ICompilableType;
import gw.util.IFeatureFilter;
import gw.util.ILogger;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class VarkEntityAccess extends BaseService implements IEntityAccess
{
  private final IEntityAccess _delegate;
  private final ClassLoader _loader;

  public VarkEntityAccess(IEntityAccess delegate, ClassLoader loader) {
    _delegate = delegate;
    _loader = loader;
  }

  @Override
  public ITypeUsesMap getDefaultTypeUses() {
    return _delegate.getDefaultTypeUses();
  }

  @Override
  public boolean isDomainInstance(Object value) {
    return _delegate.isDomainInstance(value);
  }

  @Override
  public boolean isEntityClass(IType type) {
    return _delegate.isEntityClass(type);
  }

  @Override
  public boolean isViewEntityClass(IType type) {
    return _delegate.isViewEntityClass(type);
  }

  @Override
  public IType getPrimaryEntityClass(IType type) {
    return _delegate.getPrimaryEntityClass(type);
  }

  @Override
  public boolean isKeyableEntityClass(IType type) {
    return _delegate.isKeyableEntityClass(type);
  }

  @Override
  public boolean isDomainClass(IType type) {
    return _delegate.isDomainClass(type);
  }

  @Override
  public boolean isTypekey(IType type) {
    return _delegate.isTypekey(type);
  }

  @Override
  public Object getEntityInstanceFrom(Object entity, IType classDomain) {
    return _delegate.getEntityInstanceFrom(entity, classDomain);
  }

  @Override
  public boolean areBeansEqual(Object bean1, Object bean2) {
    return _delegate.areBeansEqual(bean1, bean2);
  }

  @Override
  public boolean verifyValueForType(IType type, Object value) {
    return _delegate.verifyValueForType(type, value);
  }

  @Override
  public String makeStringFrom(Object obj) {
    return _delegate.makeStringFrom(obj);
  }

  @Override
  public long getHashedEntityId(String strId, IType classEntity) {
    return _delegate.getHashedEntityId(strId, classEntity);
  }

  @Override
  public boolean isInternal(IType type) {
    return _delegate.isInternal(type);
  }

  @Override
  public ILogger getLogger() {
    return _delegate.getLogger();
  }

  @Override
  public Locale getLocale() {
    return _delegate.getLocale();
  }

  @Override
  public Date getCurrentTime() {
    return _delegate.getCurrentTime();
  }

  @Override
  public void addEnhancementMethods(IType typeToEnhance, Collection methodsToAddTo) {
    _delegate.addEnhancementMethods(typeToEnhance, methodsToAddTo);
  }

  @Override
  public void addEnhancementProperties(IType typeToEnhance, Map propertyInfosToAddTo, boolean caseSensitive) {
    _delegate.addEnhancementProperties(typeToEnhance, propertyInfosToAddTo, caseSensitive);
  }

  @Override
  public IQueryExpressionEvaluator getQueryExpressionEvaluator(IQueryExpression queryExpression) {
    return _delegate.getQueryExpressionEvaluator(queryExpression);
  }

  @Override
  public IFeatureFilter getQueryExpressionFeatureFilter() {
    return _delegate.getQueryExpressionFeatureFilter();
  }

  @Override
  public ClassLoader getPluginClassLoader() {
    return _loader;
  }

  @Override
  public Object constructObject(Class cls) {
    return _delegate.constructObject(cls);
  }

  @Override
  public IAttributeSource getAttributeSource(GlobalScope scope) {
    return _delegate.getAttributeSource(scope);
  }

  @Override
  public Object[] convertToExternalIfNecessary(Object[] args, Class[] argTypes, Class methodOwner) {
    return _delegate.convertToExternalIfNecessary(args, argTypes, methodOwner);
  }

  @Override
  public Object convertToInternalIfNecessary(Object obj, Class methodOwner) {
    return _delegate.convertToInternalIfNecessary(obj, methodOwner);
  }

  @Override
  public boolean isExternal(Class methodOwner) {
    return _delegate.isExternal(methodOwner);
  }

  @Override
  public StringBuilder getPluginRepositories() {
    return _delegate.getPluginRepositories();
  }

  @Override
  public String getWebServerPaths() {
    return _delegate.getWebServerPaths();
  }

  @Override
  public boolean isUnreachableCodeDetectionOn() {
    return _delegate.isUnreachableCodeDetectionOn();
  }

  @Override
  public boolean isWarnOnImplicitCoercionsOn() {
    return _delegate.isWarnOnImplicitCoercionsOn();
  }

  @Override
  public IType getKeyType() {
    return _delegate.getKeyType();
  }

  @Override
  public IPropertyInfo getEntityIdProperty(IType rootType) {
    return _delegate.getEntityIdProperty(rootType);
  }

  @Override
  public boolean shouldAddWarning(IType type, IParseIssue warning) {
    return _delegate.shouldAddWarning(type, warning);
  }

  @Override
  public boolean isServerMutable() {
    return _delegate.isServerMutable();
  }

  @Override
  public boolean isRetainDebugInfo() {
    return _delegate.isRetainDebugInfo();
  }

  @Override
  public boolean isDevMode() {
    return _delegate.isDevMode();
  }

  @Override
  public ILanguageLevel getLanguageLevel() {
    return _delegate.getLanguageLevel();
  }

  @Override
  public List<IGosuClassLoadingObserver> getGosuClassLoadingObservers() {
    return _delegate.getGosuClassLoadingObservers();
  }

  @Override
  public boolean areUsesStatementsAllowedInStatementLists(ICompilableType iCompilableType) {
    return _delegate.areUsesStatementsAllowedInStatementLists(iCompilableType);
  }

  @Override
  public List<IDirectory> getAdditionalSourceRoots() {
    return _delegate.getAdditionalSourceRoots();
  }

  @Override
  public void reloadedTypes(String[] types) {
    _delegate.reloadedTypes(types);
  }
}
