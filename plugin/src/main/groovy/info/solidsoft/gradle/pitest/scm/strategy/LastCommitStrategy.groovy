package info.solidsoft.gradle.pitest.scm.strategy

import info.solidsoft.gradle.pitest.exception.ChangeLogException
import org.apache.maven.scm.ChangeSet
import org.apache.maven.scm.ScmFileSet
import org.apache.maven.scm.command.changelog.ChangeLogScmRequest
import org.apache.maven.scm.command.changelog.ChangeLogScmResult
import org.apache.maven.scm.manager.ScmManager
import org.apache.maven.scm.repository.ScmRepository

class LastCommitStrategy extends AbstractChangeLogStrategy {

    LastCommitStrategy(File root) {
        this.fileSet = new ScmFileSet(root)
    }

    LastCommitStrategy(String path) {
        this(new File(path))
    }

    @Override
    List<String> getModifiedFilenames(ScmManager manager, Set<String> includes, String url) {
        validateUrl(manager, url)
        ScmRepository repository = manager.makeScmRepository(url)
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
