package gw.maven;

import org.apache.maven.artifact.resolver.ResolutionNode;

/**
 */
public class GWArtifactCollectorFilter implements ArtifactCollectorFilter {

  private FilterResolutionNodePredicate parentPredicate;

  @Override
  public boolean processChildrenForNode(ResolutionNode node) {
    return parentPredicate == null || parentPredicate.accept(node);
  }

  @Override
  public void setTransitivityPredicate(FilterResolutionNodePredicate predicate) {
    parentPredicate = predicate;
  }

  @Override
  public FilterResolutionNodePredicate getTransitivityPredicate() {
    return parentPredicate;
  }

}
