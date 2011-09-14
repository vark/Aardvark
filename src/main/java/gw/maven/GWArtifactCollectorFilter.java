package gw.maven;

import org.apache.maven.artifact.resolver.ResolutionNode;

/**
 */
public class GWArtifactCollectorFilter implements ArtifactCollectorFilter {

  private FilterResolutionNodePredicate parentPredicate;
  private FilterResolutionNodePredicate childPredicate;

  @Override
  public boolean processChildrenForNode(ResolutionNode node) {
    return parentPredicate == null || parentPredicate.accept(node);
  }

  @Override
  public boolean processChild(ResolutionNode child) {
    return childPredicate == null || childPredicate.accept(child);
  }

  @Override
  public void setParentPredicate(FilterResolutionNodePredicate predicate) {
    parentPredicate = predicate;
  }

  @Override
  public FilterResolutionNodePredicate getParentPredicate() {
    return parentPredicate;
  }

  @Override
  public void setChildPredicate(FilterResolutionNodePredicate predicate) {
    childPredicate = predicate;
  }

  @Override
  public FilterResolutionNodePredicate getChildPredicate() {
    return childPredicate;
  }
}
