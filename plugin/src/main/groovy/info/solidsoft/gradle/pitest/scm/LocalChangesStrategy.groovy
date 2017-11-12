package info.solidsoft.gradle.pitest.scm

import org.apache.maven.scm.ScmFileSet
import org.apache.maven.scm.command.status.StatusScmResult
import org.apache.maven.scm.manager.NoSuchScmProviderException
import org.apache.maven.scm.manager.ScmManager
import org.apache.maven.scm.repository.ScmRepository
import org.apache.maven.scm.repository.ScmRepositoryException

class LocalChangesStrategy extends AbstractChangeLogStrategy implements Serializable {

    LocalChangesStrategy(File fileSetPath) {
        this.fileSet = new ScmFileSet(fileSetPath)
    }

    LocalChangesStrategy(String path) {
        this(new File(path))
    }

    @Override
    List<String> getModifiedFilenames(ScmManager manager, Set<String> includes, String url) {
        ScmRepository repository = getRepository(manager, url)
        StatusScmResult result = manager.status(repository, fileSet)
        if (!result.isSuccess()) {
            throw new ChangeLogException("Error when executing changelog")
        }
        List<String> modifiedFiles = new ArrayList<>()
        result.changedFiles.each { changedFile ->
            if (containsIgnoreCase(includes, changedFile.status.toString())) {
                def fileNameWithScmRoot = "$fileSet.basedir.absolutePath/$changedFile.path"
                modifiedFiles.add(fileNameWithScmRoot)
            }
        }
        return modifiedFiles
    }
}
