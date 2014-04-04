package gw.vark.aether

uses org.apache.maven.model.building.ModelCache
uses org.sonatype.aether.RepositoryCache
uses org.sonatype.aether.RepositorySystemSession

/**
 * Model cache implementation.
 */
class DefaultModelCache implements ModelCache {
  var _session: RepositorySystemSession
  var _cache: RepositoryCache

  static function newInstance(session: RepositorySystemSession): ModelCache {
    return session.Cache == null ? null : new DefaultModelCache(session)
  }

  private construct(session: RepositorySystemSession) {
    _session = session;
    _cache = session.getCache();
  }

  override function put(groupId: String, artifactId: String, version: String, tag: String, data: Object) {
    _cache.put(_session, new Key(groupId, artifactId, version, tag), data);
  }

  override function get(groupId: String, artifactId: String, version: String, tag: String): Object {
    return _cache.get(_session, new Key(groupId, artifactId, version, tag));
  }

  static class Key {
    var _groupId: String
    var _artifactId: String
    var _version: String
    var _tag: String
    var _hash: int
    construct(groupId: String, artifactId: String, version: String, tag: String)
    {
      _groupId = groupId
      _artifactId = artifactId
      _version = version
      _tag = tag
      var h = 17
      h = h * 31 + _groupId.hashCode()
      h = h * 31 + _artifactId.hashCode()
      h = h * 31 + _version.hashCode()
      h = h * 31 + _tag.hashCode()
      _hash = h
    }

    override function equals(obj: Object): boolean {
      if (this === obj) {
        return true
      }
      if (!(obj typeis Key)) {
        return false
      }

      var that = obj as Key
      return _artifactId == that._artifactId
          && _groupId == that._groupId
          && _version == that._version
          && _tag == that._tag
    }

    override function hashCode() : int {
      return _hash
    }
  }
}