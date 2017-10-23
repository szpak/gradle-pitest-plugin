package info.solidsoft.gradle.pitest.scm

import org.apache.maven.scm.ScmFileSet
import org.apache.maven.scm.ScmVersion
import org.apache.maven.scm.manager.ScmManager

class CustomChangeLogStrategy implements ChangeLogStrategy {

    ScmFileSet scmFileSet
    ScmVersion endVersion
    ScmVersion startVersion

    CustomChangeLogStrategy() {

    }

    CustomChangeLogStrategy(String fileSetPath, String startVersionType,
                            String startVersion, String endVersionType, String endVersion) {

    }

    @Override
    List<String> getModifiedFilenames(ScmManager manager, Set<String> includes, String url) {
        return null
    }
}
