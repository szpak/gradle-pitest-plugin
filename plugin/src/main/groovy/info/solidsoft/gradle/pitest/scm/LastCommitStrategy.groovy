package info.solidsoft.gradle.pitest.scm

import org.apache.log4j.Logger
import org.apache.maven.scm.ChangeSet
import org.apache.maven.scm.ScmFileSet
import org.apache.maven.scm.command.changelog.ChangeLogScmRequest
import org.apache.maven.scm.command.changelog.ChangeLogScmResult
import org.apache.maven.scm.manager.NoSuchScmProviderException
import org.apache.maven.scm.manager.ScmManager
import org.apache.maven.scm.repository.ScmRepository
import org.apache.maven.scm.repository.ScmRepositoryException

class LastCommitStrategy extends AbstractChangeLogStrategy {

    LastCommitStrategy(File root) {
        this.fileSet = new ScmFileSet(root)
    }

    LastCommitStrategy(String path) {
        this(new File(path))
    }

    @Override
    List<String> getModifiedFilenames(ScmManager manager, Set<String> includes, String url) {
        ScmRepository repository = getRepository(manager, url)
        ChangeLogScmRequest request = new ChangeLogScmRequest(repository, fileSet)
        request.setLimit(1)
        ChangeLogScmResult result = manager.changeLog(request)
        if (!result.isSuccess()) {
            throw new ChangeLogException("Error when executing changelog")
        }
        List<String> modifiedFilenames = new ArrayList<>()
        List<ChangeSet> changeSets = result.changeLog.changeSets
        if (changeSets.isEmpty()) {
            return Collections.emptyList()
        }
        changeSets.get(0).files.each { changeFile ->
            String status = changeFile.action.toString()
            if (containsIgnoreCase(includes, status)) {
                def fileNameWithScmRoot = "$fileSet.basedir.absolutePath/$changeFile.name"
                modifiedFilenames.add(fileNameWithScmRoot)
            }
        }
        return modifiedFilenames
    }
}
