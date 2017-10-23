package info.solidsoft.gradle.pitest.scm

import org.apache.maven.scm.manager.ScmManager

interface ChangeLogStrategy {
    List<String> getModifiedFilenames(ScmManager manager, Set<String> includes, String url)
}
