package gw.maven;

import org.apache.maven.artifact.resolver.ResolutionNode;

/**
 */
public interface ArtifactCollectorFilter {

  boolean processChildrenForNode(ResolutionNode node);

  boolean processChild(ResolutionNode child);

  void setParentPredicate(FilterResolutionNodePredicate predicate);
  FilterResolutionNodePredicate getParentPredicate();

  void setChildPredicate(FilterResolutionNodePredicate predicate);
  FilterResolutionNodePredicate getChildPredicate();

  interface FilterResolutionNodePredicate {
    boolean accept(ResolutionNode node);
  }
}
