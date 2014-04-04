package gw.vark.aether

uses org.sonatype.aether.collection.DependencyCollectionContext
uses org.sonatype.aether.collection.DependencySelector
uses org.sonatype.aether.graph.Dependency
uses org.sonatype.aether.util.graph.selector.AndDependencySelector
uses org.sonatype.aether.util.graph.selector.ExclusionDependencySelector
uses org.sonatype.aether.util.graph.selector.OptionalDependencySelector
uses org.sonatype.aether.util.graph.selector.StaticDependencySelector

/**
 * Aether DefaultDependencyCollector appreas to always skip first layer of selector. Hence we need this
 * three-layered selector.
 *
 * FIXME: Also selects all IntelliJ dependencies...
 */
class TestDependencySelector {
  static var _testSelector : DependencySelector as Instance =
      new AndDependencySelector(new DependencySelector[] {
        new FirstTestDependencySelector(),
        new OptionalDependencySelector(),
        new ExclusionDependencySelector()
      })
  private static var DIRECT_SELECTOR = new DirectTestDependencySelector()
  private static var INDIRECT_SELECTOR = new IndirectTestDependencySelector()

  static class FirstTestDependencySelector implements DependencySelector {
    override function selectDependency(dependency: Dependency): boolean {
      throw "this does not appear to be called"
    }

    override function deriveChildSelector(context: DependencyCollectionContext): DependencySelector {
      return DIRECT_SELECTOR
    }
  }

  static class DirectTestDependencySelector implements DependencySelector {
    override function selectDependency(dependency: Dependency): boolean {
      // All direct dependencies
      return true
    }

    override function deriveChildSelector(context: DependencyCollectionContext): DependencySelector {
      var dependency = context.Dependency
      if (dependency.Scope == "compile" ||
          dependency.Scope == "runtime" ||
          // FIXME-isd: these have "provided" scope, but we still want to include them for testing purposes.
          // we use "provided" scope for packaging reasons (so plugins are not packaged inside each other).
          // what we really need is "transitive provided" scope.
          dependency.Artifact.GroupId.startsWith("com.jetbrains.intellij") ||
          dependency.Artifact.GroupId.startsWith("com.guidewire.studio")) {
        return INDIRECT_SELECTOR
      }
      return new StaticDependencySelector(false)
    }
  }

  static class IndirectTestDependencySelector implements DependencySelector {
    override function selectDependency(dependency: Dependency): boolean {
      // Only compile & runtime dependencies
      return "compile" == dependency.Scope || "runtime" == dependency.Scope
    }

    override function deriveChildSelector(context: DependencyCollectionContext): DependencySelector {
      return this
    }
  }
}