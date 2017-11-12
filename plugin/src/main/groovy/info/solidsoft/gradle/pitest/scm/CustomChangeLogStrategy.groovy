package info.solidsoft.gradle.pitest.scm

import org.apache.maven.scm.ScmBranch
import org.apache.maven.scm.ScmFileSet
import org.apache.maven.scm.ScmRevision
import org.apache.maven.scm.ScmTag
import org.apache.maven.scm.ScmVersion
import org.apache.maven.scm.command.changelog.ChangeLogScmRequest
import org.apache.maven.scm.command.changelog.ChangeLogScmResult
import org.apache.maven.scm.manager.ScmManager
import org.apache.maven.scm.repository.ScmRepository

class CustomChangeLogStrategy extends AbstractChangeLogStrategy {

    String startVersionType
    String startVersion
    String endVersionType
    String endVersion

    static class Builder {
        ScmFileSet scmFileSet
        String startVersionType
        String startVersion
        String endVersionType
        String endVersion

        Builder fileSet(String path) {
            this.scmFileSet = new ScmFileSet(new File(path))
            return this
        }

        Builder startVersionType(String startVersionType) {
            this.startVersionType = startVersionType
            return this
        }

        Builder startVersion(String startVersion) {
            this.startVersion = startVersion
            return this
        }

        Builder endVersionType(String endVersionType) {
            this.endVersionType = endVersionType
            return this
        }

        Builder endVersion(String endVersion) {
            this.endVersion = endVersion
            return this
        }

        CustomChangeLogStrategy build() {
            return new CustomChangeLogStrategy(this)
        }

    }

    private CustomChangeLogStrategy(Builder builder) {
        this.fileSet = builder.scmFileSet
        this.startVersion = builder.startVersion
        this.startVersionType = builder.startVersionType
        this.endVersion = builder.endVersion
        this.endVersionType = builder.endVersionType
    }

    @Override
    List<String> getModifiedFilenames(ScmManager manager, Set<String> includes, String url) {
        validateUrl(manager, url)
        ScmRepository repository = manager.makeScmRepository(url)
        ScmVersion startVersion = getScmVersion(this.startVersionType, this.startVersion)
        ScmVersion endVersion = getScmVersion(this.endVersionType, this.endVersion)
        ChangeLogScmRequest request = new ChangeLogScmRequest(repository, fileSet)
        request.setStartRevision(startVersion)
        request.setEndRevision(endVersion)
        ChangeLogScmResult changeLogResult = manager.changeLog(request)
        if (!changeLogResult.isSuccess()) {
            throw new ChangeLogException("Failed when executing change log")
        }
        List<String> fileNames = new ArrayList<>()
        changeLogResult.changeLog.changeSets.each {
            change ->
                change.files.each {
                    file ->
                        if (containsIgnoreCase(includes, file.action.toString())) {
                            def filePathWithScmRoot = "$fileSet.basedir.absolutePath/$file.name"
                            fileNames.add(filePathWithScmRoot)
                        }
                }
        }
        return fileNames
    }

    private static ScmVersion getScmVersion(String versionType, String version) {
        if (versionType == null) {
            throw new ChangeLogException("Cannot execute changelog on null versionType")
        }
        if (version == null) {
            throw new ChangeLogException("Cannot execute changelog on null version")
        }
        switch (versionType) {
            case 'branch':
                return new ScmBranch(version)
            case 'revision':
                return new ScmRevision(version)
            case 'tag':
                return new ScmTag(version)
            default:
                def supportedVersionTypes = ['branch','revision','tag']
                throw new ChangeLogException("Invalid version type, expected one of $supportedVersionTypes, got $versionType")
        }
    }
}
