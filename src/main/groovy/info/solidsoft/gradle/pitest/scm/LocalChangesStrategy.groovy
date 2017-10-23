package info.solidsoft.gradle.pitest.scm

import org.apache.maven.scm.ScmFileSet
import org.apache.maven.scm.command.status.StatusScmResult
import org.apache.maven.scm.manager.ScmManager
import org.apache.maven.scm.repository.ScmRepository

class LocalChangesStrategy implements ChangeLogStrategy {

    ScmFileSet fileSet

    LocalChangesStrategy() {

    }

    LocalChangesStrategy(String fileSetPath) {
        this.fileSet = new ScmFileSet(new File(fileSetPath))
    }

    @Override
    List<String> getModifiedFilenames(ScmManager manager, Set<String> includes, String url) {
        ScmRepository repository = manager.makeScmRepository(url)
        StatusScmResult result = manager.status(repository, fileSet)
        List<String> modifiedFiles = new ArrayList<>()
        result.changedFiles.each { changedFile ->
            if (includes.contains(changedFile.status.toString())) {
                modifiedFiles.add(changedFile.path)
            }
        }
        return modifiedFiles
    }
}
