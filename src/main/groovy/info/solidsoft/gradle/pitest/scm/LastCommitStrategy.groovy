package info.solidsoft.gradle.pitest.scm

import org.apache.maven.scm.ChangeSet
import org.apache.maven.scm.ScmFileSet
import org.apache.maven.scm.command.changelog.ChangeLogScmRequest
import org.apache.maven.scm.command.changelog.ChangeLogScmResult
import org.apache.maven.scm.manager.ScmManager
import org.apache.maven.scm.repository.ScmRepository

class LastCommitStrategy implements ChangeLogStrategy {

    ScmFileSet fileSet

    LastCommitStrategy() {

    }

    LastCommitStrategy(String filesetPath) {
        this.fileSet = new ScmFileSet(new File(filesetPath))
    }

    @Override
    List<String> getModifiedFilenames(ScmManager manager, Set<String> includes, String url) {
        ScmRepository repository = manager.makeScmRepository(url)
        ChangeLogScmRequest request = new ChangeLogScmRequest(repository, fileSet)
        request.setLimit(1)
        ChangeLogScmResult result = manager.changeLog(request)
        List<String> modifiedFilenames = new ArrayList<>()
        if (result.isSuccess()) {
            List<ChangeSet> changeSets = result.changeLog.changeSets
            if (changeSets.isEmpty()) {
                return Collections.emptyList()
            }
            changeSets.get(0).files.each { changeFile ->
                String status = changeFile.action.toString()
                if (includes.contains(status)) {
                    modifiedFilenames.add(changeFile.name)
                }
            }
            return modifiedFilenames
        }
    }
}
