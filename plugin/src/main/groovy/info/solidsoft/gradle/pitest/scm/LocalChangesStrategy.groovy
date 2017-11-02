package info.solidsoft.gradle.pitest.scm

import org.apache.maven.scm.ScmFileSet
import org.apache.maven.scm.command.status.StatusScmResult
import org.apache.maven.scm.manager.NoSuchScmProviderException
import org.apache.maven.scm.manager.ScmManager
import org.apache.maven.scm.repository.ScmRepository
import org.apache.maven.scm.repository.ScmRepositoryException

class LocalChangesStrategy implements ChangeLogStrategy, Serializable {

    ScmFileSet fileSet

    LocalChangesStrategy() {

    }

    LocalChangesStrategy(File fileSetPath) {
        this.fileSet = new ScmFileSet(fileSetPath)
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
            if (includes.contains(changedFile.status.toString())) {
                modifiedFiles.add(changedFile.path)
            }
        }
        return modifiedFiles
    }

    private static ScmRepository getRepository(ScmManager manager, String url) {
        try {
            ScmRepository repository = manager.makeScmRepository(url)
            return repository
        } catch (ScmRepositoryException | NoSuchScmProviderException e) {
            throw new ChangeLogException("An error occurred with repository configuration", e)
        }
    }
}
