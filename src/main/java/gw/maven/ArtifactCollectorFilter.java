package gw.maven;

import org.apache.maven.artifact.resolver.ResolutionNode;

/**
 */
public interface ArtifactCollectorFilter {

  boolean processChildrenForNode(ResolutionNode node);

  void setTransitivityPredicate(FilterResolutionNodePredicate predicate);
  FilterResolutionNodePredicate getTransitivityPredicate();

  interface FilterResolutionNodePredicate {
    boolean accept(ResolutionNode node);
  }
}
